package io.redback.eclipse.editors.components.impl;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import io.firebus.utils.DataMap;

import io.redback.eclipse.editors.components.Form;
import io.redback.eclipse.editors.components.Manager;
import io.redback.eclipse.editors.components.TextField;

public class ViewList3Form extends Form
{
	public ViewList3Form(DataMap d, Manager m, Composite p, int s) 
	{
		super(d, m, p, s);
		createUI();
	}
	
	public void createUI() {
		new TextField(_data, "headerattribute", "Header Attribute", this, SWT.NONE);
		new TextField(_data, "subheadattribute", "Subhead Attribute", this, SWT.NONE);
		new TextField(_data, "supptextattribute", "Support Text Attribute", this, SWT.NONE);
		new TextField(_data, "grow", "Grow", this, SWT.NONE);
		new TextField(_data, "shrink", "Shrink", this, SWT.NONE);
	}

	public void onFieldUpdate(String attribute, Object oldValue, Object newValue) {

	}

}
