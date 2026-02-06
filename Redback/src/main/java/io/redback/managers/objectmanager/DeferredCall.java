package io.redback.managers.objectmanager;

import io.firebus.script.Function;

public class DeferredCall {
	public String scriptName;
	public Function function;
	
	public DeferredCall(String sn, Function f) {
		scriptName = sn;
		function = f;
	}
}
