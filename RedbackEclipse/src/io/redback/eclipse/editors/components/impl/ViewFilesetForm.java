package io.redback.eclipse.editors.components.impl;

import org.eclipse.swt.widgets.Composite;

import io.firebus.utils.DataMap;

import io.redback.eclipse.editors.components.Form;
import io.redback.eclipse.editors.components.Manager;

public class ViewFilesetForm extends Form
{
	public ViewFilesetForm(DataMap d, Manager m, Composite p, int s) 
	{
		super(d, m, p, s);
		createUI();
	}
	
	public void createUI() {
	}

	public void onFieldUpdate(String attribute, Object oldValue, Object newValue) {

	}

}
