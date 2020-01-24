package io.redback.eclipse.editors.components;

public class NavigatorAction {

	public String action;
	public String type;
	public String name;
	public String nextSelectType;
	public String nextSelectName;

	public NavigatorAction(String a, String t, String n) {
		action = a;
		type = t;
		name = n;
	}
	
	public NavigatorAction(String a, String t, String n, String nst, String nsn) {
		action = a;
		type = t;
		name = n;
		nextSelectType = nst;
		nextSelectName = nsn;
	}
}
