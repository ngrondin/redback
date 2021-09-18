package io.redback.eclipse.editors.components;

import org.eclipse.swt.widgets.Composite;

import io.firebus.data.DataMap;

public abstract class Field extends Composite {

	protected DataMap _data;
	protected String attribute;
	protected String label;
	protected Composite parent;
	protected Form form;

	
	Field(DataMap d, String a, String l, Composite p, int s) {
		super(p, s);
		_data = d;
		attribute = a;
		label = l;
		parent = p;
		Composite c = p;
		while(c != null && !(c instanceof Form))
			c = c.getParent();
		if(c != null)
			form = (Form)c;
	}
	
	public abstract void createUI();

}
