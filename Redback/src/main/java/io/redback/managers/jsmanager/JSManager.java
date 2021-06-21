package io.redback.managers.jsmanager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import io.firebus.threads.FirebusThread;
import io.firebus.utils.DataMap;
import io.redback.RedbackException;
import io.redback.utils.js.JSConverter;
import io.redback.utils.js.LoggerJSFunction;
import io.redback.utils.js.RedbackUtilsJSWrapper;

public class JSManager {
	
	protected class SourceEntry {
		public String id;
		public String src;
		public long lastUpdated;
		public SourceEntry(String i, String s, long lu) {
			id = i;
			src = s;
			lastUpdated = lu;
		}
		
		public SourceEntry copy() {
			return new SourceEntry(id, src, lastUpdated);
		}
	}
	
	protected class EngineEntry {
		public long id;
		public ScriptEngine engine;
		public long lastCompiled;
		public EngineEntry(ScriptEngine e, long i, long lc) {
			id = i;
			engine = e;
			lastCompiled = lc;
		}
	}
	
	private Logger logger = Logger.getLogger("io.redback");
	protected String name;
	protected ScriptEngineManager engineManager;
	protected Map<String, SourceEntry> sourceEntries;
	protected Map<Long, EngineEntry> engines;
	protected long lastUpdated;
	protected DataMap globalVariables;
	protected boolean _dropCompilationErrors = false;
	
	public JSManager(String n) {
		name = n;
		sourceEntries = new HashMap<String, SourceEntry>();
		engines = new HashMap<Long, EngineEntry>();
		engineManager = new ScriptEngineManager();
	}
	
	public void setGlobalVariables(DataMap gv) {
		globalVariables = gv;
	}
	
	public void dropCompilationErrors(boolean v) {
		_dropCompilationErrors = v;
	}
	
	public void addSource(String id, String src) {
		synchronized(sourceEntries) {
			long now = System.currentTimeMillis();
			SourceEntry fe = sourceEntries.get(id);
			if(fe != null) {
				fe.src = src;
				fe.lastUpdated = now;
			} else {
				sourceEntries.put(id, new SourceEntry(id, src, now));	
			}	
			lastUpdated = now;
		}
	}
	
	public void removeSource(String id) {
		synchronized(sourceEntries) {
			if(sourceEntries.containsKey(id)) {
				sourceEntries.remove(id);
			}
		}
	}
	
	protected EngineEntry addEngine(Long l) {
		logger.info("Adding new JS engine " + name + "_" + l);
		EngineEntry ee = null;
		synchronized(engines) {
			ScriptEngine engine = engineManager.getEngineByName("graal.js");
			engine.getBindings(ScriptContext.ENGINE_SCOPE).put("log", new LoggerJSFunction());
			engine.getBindings(ScriptContext.ENGINE_SCOPE).put("global", JSConverter.toJS(globalVariables));
			engine.getBindings(ScriptContext.ENGINE_SCOPE).put("rbutils", new RedbackUtilsJSWrapper());
			ee = new EngineEntry(engine, l, 0);
			engines.put(l, ee);
		}
		return ee;
	}
	
	protected EngineEntry getEngine(Long l) {
		EngineEntry ee = null;
		synchronized(engines) {
			ee = engines.get(l);
		}
		return ee;
	}

	protected void compileEngine(EngineEntry engineEntry) throws RedbackException {
		long start = System.currentTimeMillis();
		List<SourceEntry> copiesToCompile = new ArrayList<SourceEntry>();
		try {
			long copystart = 0;
			synchronized(sourceEntries) { // Copy the source to a local variable first so as to release the lock on sourceEntries
				copystart = System.currentTimeMillis();
				Iterator<String> it = sourceEntries.keySet().iterator();
				while(it.hasNext()) {
					SourceEntry sourceEntry = sourceEntries.get(it.next());
					if(sourceEntry.lastUpdated >= engineEntry.lastCompiled)
						copiesToCompile.add(sourceEntry.copy());
				}
			}
			
			long compilestart = System.currentTimeMillis();
			for(SourceEntry copyToCompile: copiesToCompile) {
				try {
					engineEntry.engine.eval(copyToCompile.src);
				} catch(Exception e) {
					if(_dropCompilationErrors) {
						logger.severe("Problem recompiling script [" + copyToCompile.id + "]: " + e.getMessage());
						removeSource(copyToCompile.id);
					} else {
						throw new RedbackException("Problem recompiling script [" + copyToCompile.id + "]", e);
					}
				}
			}

			engineEntry.lastCompiled = copystart;
			long end = System.currentTimeMillis();
			long totalDuration = end - start;
			long compileDuration = end - compilestart;
			logger.info("Compiled " + copiesToCompile.size() + " new functions into engine " + name + "_" + engineEntry.id + " in " + totalDuration + "ms (" + compileDuration + "ms)");
			
		} catch(Exception e) {
			throw new RedbackException("Problem recompiling engine", e);
		}
	}
	
	protected Object execute(String function, Object[] params) throws RedbackException, NoSuchMethodException, ScriptException {
		Long id = ((FirebusThread)Thread.currentThread()).getFunctionExecutionId();
		EngineEntry engineEntry = getEngine(id);
		if(engineEntry == null)
			engineEntry = addEngine(id);
		if(engineEntry.lastCompiled < lastUpdated)
			compileEngine(engineEntry);
		Object o = ((Invocable)engineEntry.engine).invokeFunction(function, params);
		return o;
	}
}
