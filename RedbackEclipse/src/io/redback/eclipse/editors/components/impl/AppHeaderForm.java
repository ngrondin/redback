package io.redback.eclipse.editors.components.impl;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import io.firebus.data.DataMap;

import io.redback.eclipse.editors.components.Form;
import io.redback.eclipse.editors.components.ListField;
import io.redback.eclipse.editors.components.Manager;
import io.redback.eclipse.editors.components.ReadOnlyField;
import io.redback.eclipse.editors.components.TextField;

public class AppHeaderForm extends Form
{
	public AppHeaderForm(DataMap d, Manager m, Composite p, int s) 
	{
		super(d, m, p, s);
		createUI();
	}

	public void createUI() {
		new ReadOnlyField(_data, "name", "Name", this, SWT.NONE);
		new TextField(_data, "label", "Label", this, SWT.NONE);
		new TextField(_data, "page", "Page", this, SWT.NONE);
		new TextField(_data, "initialviewtitle", "Initial Title", this, SWT.NONE);
		new TextField(_data, "defaultview", "Default View", this, SWT.NONE);
		new ListField(_data, "iconsets", "Icon Sets", this, SWT.NONE);
	}

	public void onFieldUpdate(String attribute, Object oldValue, Object newValue) {

	}


}
