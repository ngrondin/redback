package io.redback.eclipse.editors.components.impl;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import io.firebus.utils.DataMap;

import io.redback.eclipse.editors.components.Form;
import io.redback.eclipse.editors.components.Manager;
import io.redback.eclipse.editors.components.TextField;

public class ViewMapForm extends Form
{
	public ViewMapForm(DataMap d, Manager m, Composite p, int s) 
	{
		super(d, m, p, s);
		createUI();
	}
	
	public void createUI() {
		new TextField(_data, "geoattribute", "Geometry Attribute", this, SWT.NONE);
		new TextField(_data, "labelattribute", "Label Attribute", this, SWT.NONE);
		new TextField(_data, "descriptionattribute", "Description Attribute", this, SWT.NONE);
	}

	public void onFieldUpdate(String attribute, Object oldValue, Object newValue) {

	}

}
