package io.redback.eclipse.editors.components.impl;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import io.firebus.utils.DataMap;

import io.redback.eclipse.editors.components.Form;
import io.redback.eclipse.editors.components.Manager;
import io.redback.eclipse.editors.components.TextField;

public class ViewViewForm extends Form
{
	protected ObjectAttributeRelatedForm relatedForm;
	
	public ViewViewForm(DataMap d, Manager m, Composite p, int s) 
	{
		super(d, m, p, s);
		createUI();
	}
	
	public void createUI() {
		new TextField(data, "name", "Name", this, SWT.NONE);
	}


	public void onFieldUpdate(String attribute, Object oldValue, Object newValue) {
		
	}

}
