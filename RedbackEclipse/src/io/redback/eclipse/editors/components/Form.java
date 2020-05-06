package io.redback.eclipse.editors.components;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import io.firebus.utils.DataMap;

public abstract class Form extends Composite {
	
	protected DataMap _data;
	protected Manager manager;	
	protected Composite parent;
	
	public Form(DataMap d, Manager m, Composite p, int s) {
		super(p, s);
		_data = d;
		manager = m;
		parent = p;
		setLayout(new RowLayout(SWT.VERTICAL));
	}
	
	public void refresh() {
		Control[] children = getChildren();
		for(int i = 0; i < children.length; i++) {
			children[i].dispose();
		}
		createUI();
		layout(true, true);
	}
	
	public void setDataChanged(boolean c) {
		manager.setDataChanged(c);
	}
	
	public abstract void createUI();

	public abstract void onFieldUpdate(String attribute, Object oldValue, Object newValue);
}
