package io.redback.eclipse.editors.components.impl;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import io.firebus.data.DataMap;

import io.redback.eclipse.editors.components.Form;
import io.redback.eclipse.editors.components.Manager;
import io.redback.eclipse.editors.components.TextField;

public class ViewTextAreaForm extends Form
{
	public ViewTextAreaForm(DataMap d, Manager m, Composite p, int s) 
	{
		super(d, m, p, s);
		createUI();
	}
	
	public void createUI() {
		new TextField(_data, "label", "Label", this, SWT.NONE);
		new TextField(_data, "attribute", "Attribute", this, SWT.NONE);
		new TextField(_data, "icon", "Icon", this, SWT.NONE);
		new TextField(_data, "size", "Size", this, SWT.NONE);
		new TextField(_data, "rows", "Rows", this, SWT.NONE);
		new TextField(_data, "show", "Show (!)", this, SWT.NONE);
		new TextField(_data, "grow", "Grow", this, SWT.NONE);
		new TextField(_data, "shrink", "Shrink", this, SWT.NONE);
	}

	public void onFieldUpdate(String attribute, Object oldValue, Object newValue) {

	}

}
