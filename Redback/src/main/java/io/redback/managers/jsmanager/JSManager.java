package io.redback.managers.jsmanager;

import java.util.HashMap;
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
		public String src;
		public long lastUpdated;
		public SourceEntry(String s, long lu) {
			src = s;
			lastUpdated = lu;
		}
	}
	
	protected class EngineEntry {
		public ScriptEngine engine;
		public long lastCompiled;
		public EngineEntry(ScriptEngine e, long lc) {
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
				sourceEntries.put(id, new SourceEntry(src, now));	
			}	
			lastUpdated = now;
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
			ee = new EngineEntry(engine, 0);
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
		try {
			synchronized(sourceEntries) {
				long now = System.currentTimeMillis();
				String[] functionIds = sourceEntries.keySet().toArray(new String[0]); // Done so the map can be modified during recompilation
				for(int i = 0; i < functionIds.length; i++) {
					String functionId = functionIds[i];
					SourceEntry sourceEntry = sourceEntries.get(functionId);
					if(sourceEntry.lastUpdated >= engineEntry.lastCompiled) {
						try {
							engineEntry.engine.eval(sourceEntry.src);
						} catch(Exception e) {
							if(_dropCompilationErrors) {
								logger.severe("Problem recompiling script [" + functionId + "]: " + e.getMessage());
								sourceEntries.remove(functionId);
							} else {
								throw new RedbackException("Problem recompiling script [" + functionId + "]", e);
							}
						}
					}
				}
				engineEntry.lastCompiled = now;
			}
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
