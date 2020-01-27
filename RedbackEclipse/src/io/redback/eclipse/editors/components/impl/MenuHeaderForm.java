package io.redback.eclipse.editors.components.impl;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import io.firebus.utils.DataMap;

import io.redback.eclipse.editors.components.Form;
import io.redback.eclipse.editors.components.Manager;
import io.redback.eclipse.editors.components.ReadOnlyField;
import io.redback.eclipse.editors.components.TextField;

public class MenuHeaderForm extends Form
{
	public MenuHeaderForm(DataMap d, Manager m, Composite p, int s) 
	{
		super(d, m, p, s);
		createUI();
	}

	public void createUI() {
		new ReadOnlyField(data, "name", "Name", this, SWT.NONE);
		new TextField(data, "type", "Type", this, SWT.NONE);
		new TextField(data, "label", "Label", this, SWT.NONE);
		new TextField(data, "icon", "Icon", this, SWT.NONE);
		new TextField(data, "group", "Group", this, SWT.NONE);
	}

	public void onFieldUpdate(String attribute, Object oldValue, Object newValue) {

	}


}
