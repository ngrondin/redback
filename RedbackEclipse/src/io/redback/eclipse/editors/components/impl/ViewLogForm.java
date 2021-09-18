package io.redback.eclipse.editors.components.impl;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import io.firebus.data.DataMap;

import io.redback.eclipse.editors.components.Form;
import io.redback.eclipse.editors.components.Manager;
import io.redback.eclipse.editors.components.TextField;

public class ViewLogForm extends Form
{
	public ViewLogForm(DataMap d, Manager m, Composite p, int s) 
	{
		super(d, m, p, s);
		createUI();
	}
	
	public void createUI() {
		new TextField(_data, "entryattribute", "Entry Attribute", this, SWT.NONE);
		new TextField(_data, "userattribute", "User Attribute", this, SWT.NONE);
		new TextField(_data, "dateattribute", "Date Attribute", this, SWT.NONE);
		new TextField(_data, "categoryattribute", "Category Attribute", this, SWT.NONE);
	}

	public void onFieldUpdate(String attribute, Object oldValue, Object newValue) {

	}

}
