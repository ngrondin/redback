package io.redback.eclipse.editors.components.impl;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import io.firebus.data.DataMap;

import io.redback.eclipse.editors.components.Form;
import io.redback.eclipse.editors.components.Manager;
import io.redback.eclipse.editors.components.ReadOnlyField;
import io.redback.eclipse.editors.components.TextAreaField;
import io.redback.eclipse.editors.components.TextField;

public class ResourceHeaderForm extends Form
{
	public ResourceHeaderForm(DataMap d, Manager m, Composite p, int s) 
	{
		super(d, m, p, s);
		createUI();
	}

	public void createUI() {
		new ReadOnlyField(_data, "name", "Name", this, SWT.NONE);
		new TextField(_data, "type", "type", this, SWT.NONE);
		new TextAreaField(_data, "content", "Content", this, SWT.NONE);
	}

	public void onFieldUpdate(String attribute, Object oldValue, Object newValue) {

	}


}
