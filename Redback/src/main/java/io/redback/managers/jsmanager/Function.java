package io.redback.managers.jsmanager;

import java.util.List;

import io.redback.RedbackException;

public class Function extends Executor {

	public Function(JSManager jsm, String fn, List<String> p, String src) throws RedbackException
	{
		super(jsm, fn, p, "function " + fn + "(" + (p != null ? String.join(",", p) : "") + ") {\r\n" + src + "}");
	}
	

}
