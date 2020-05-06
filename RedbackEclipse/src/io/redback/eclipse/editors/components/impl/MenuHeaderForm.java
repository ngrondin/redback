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
		new ReadOnlyField(_data, "name", "Name", this, SWT.NONE);
		new TextField(_data, "type", "Type", this, SWT.NONE);
		new TextField(_data, "label", "Label", this, SWT.NONE);
		new TextField(_data, "icon", "Icon", this, SWT.NONE);
		new TextField(_data, "group", "Group", this, SWT.NONE);
		new TextField(_data, "order", "Order", this, SWT.NONE);
		new TextField(_data, "view", "View", this, SWT.NONE);
	}

	public void onFieldUpdate(String attribute, Object oldValue, Object newValue) {

	}


}
