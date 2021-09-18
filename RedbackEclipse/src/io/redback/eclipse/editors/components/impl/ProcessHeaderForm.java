package io.redback.eclipse.editors.components.impl;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import io.firebus.data.DataList;
import io.firebus.data.DataMap;

import io.redback.eclipse.editors.components.Manager;
import io.redback.eclipse.editors.components.SelectField;
import io.redback.eclipse.editors.components.TextField;

public class ProcessHeaderForm extends ProcessForm
{
	public ProcessHeaderForm(DataMap d, DataList n, Manager m, Composite p, int s) 
	{
		super(d, n, m, p, s);
		createUI();
	}
	
	public void createUI() {
		new TextField(_data, "name", "Name", this, SWT.NONE);
		new TextField(_data, "version", "Version", this, SWT.NONE);
		new SelectField(_data, "startnode", "Start Node", nodeOptions, nodeOptionLabels, this, SWT.NONE);
	}


}
