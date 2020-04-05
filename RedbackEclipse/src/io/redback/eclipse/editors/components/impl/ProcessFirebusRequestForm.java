package io.redback.eclipse.editors.components.impl;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import io.firebus.utils.DataList;
import io.firebus.utils.DataMap;

import io.redback.eclipse.editors.components.Manager;
import io.redback.eclipse.editors.components.MapField;
import io.redback.eclipse.editors.components.SelectField;
import io.redback.eclipse.editors.components.TextField;

public class ProcessFirebusRequestForm extends ProcessForm
{
	public ProcessFirebusRequestForm(DataMap d, DataList n, Manager m, Composite p, int s) 
	{
		super(d, n, m, p, s);
		createUI();
	}
	
	public void createUI() {
		
		new TextField(data, "name", "Name", this, SWT.NONE);
		new TextField(data, "service", "Service", this, SWT.NONE);
		new MapField(data, "data", "Data", this, SWT.NONE);
		new MapField(data, "outmap", "Result Map", this, SWT.NONE);
		new SelectField(data, "nextnode", "Next Node", nodeOptions, nodeOptionLabels, this, SWT.NONE);
	}


}
