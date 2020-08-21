package io.redback.managers.jsmanager;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import io.firebus.utils.DataMap;
import io.redback.RedbackException;
import io.redback.utils.js.JSConverter;
import io.redback.utils.js.LoggerJSFunction;

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
	
	protected ScriptEngineManager engineManager;
	protected Map<String, SourceEntry> sourceEntries;
	protected Map<Long, EngineEntry> engines;
	protected long lastUpdated;
	protected DataMap globalVariables;
	
	public JSManager() {
		sourceEntries = new HashMap<String, SourceEntry>();
		engines = new HashMap<Long, EngineEntry>();
		engineManager = new ScriptEngineManager();
	}
	
	public void setGlobalVariables(DataMap gv) {
		globalVariables = gv;
	}
	
	public void addSource(String id, String src) {
		synchronized(sourceEntries) {
			long now = System.currentTimeMillis();
			SourceEntry fe = sourceEntries.get(id);
			if(fe != null) {
				fe.src = src;
				fe.lastUpdated = lastUpdated;
			} else {
				sourceEntries.put(id, new SourceEntry(src, now));	
			}	
			lastUpdated = now;
		}
	}
	
	protected EngineEntry addEngine(Long l) {
		EngineEntry ee = null;
		synchronized(engines) {
			ScriptEngine engine = engineManager.getEngineByName("graal.js");
			engine.getBindings(ScriptContext.ENGINE_SCOPE).put("log", new LoggerJSFunction());
			engine.getBindings(ScriptContext.ENGINE_SCOPE).put("global", JSConverter.toJS(globalVariables));
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
				Iterator<String> it = sourceEntries.keySet().iterator();
				while(it.hasNext()) {
					String functionId = it.next();
					SourceEntry sourceEntry = sourceEntries.get(functionId);
					if(sourceEntry.lastUpdated >= engineEntry.lastCompiled)
						engineEntry.engine.eval(sourceEntry.src);
				}
				engineEntry.lastCompiled = System.currentTimeMillis();
			}
		} catch(Exception e) {
			throw new RedbackException("Problem recompiling engine", e);
		}
	}
	
	protected Object execute(String function, Object[] params) throws RedbackException, NoSuchMethodException, ScriptException {
		Long id = Thread.currentThread().getId();
		EngineEntry engineEntry = getEngine(id);
		if(engineEntry == null)
			engineEntry = addEngine(id);
		if(engineEntry.lastCompiled < lastUpdated)
			compileEngine(engineEntry);
		Object o = ((Invocable)engineEntry.engine).invokeFunction(function, params);
		return o;
	}
}
