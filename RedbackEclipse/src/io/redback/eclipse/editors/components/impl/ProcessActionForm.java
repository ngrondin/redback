package io.redback.eclipse.editors.components.impl;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import io.firebus.data.DataList;
import io.firebus.data.DataMap;

import io.redback.eclipse.editors.components.Manager;
import io.redback.eclipse.editors.components.SelectField;
import io.redback.eclipse.editors.components.TextField;

public class ProcessActionForm extends ProcessForm
{
	public ProcessActionForm(DataMap d, DataList n, Manager m, Composite p, int s) 
	{
		super(d, n, m, p, s);
		createUI();
	}
	
	public void createUI() {
		
		new TextField(_data, "name", "Name", this, SWT.NONE);
		new TextField(_data, "process", "Process", this, SWT.NONE);
		new TextField(_data, "interaction", "Interaction Code", this, SWT.NONE);
		new TextField(_data, "action", "Action", this, SWT.NONE);
		new SelectField(_data, "nextnode", "Next Node", nodeOptions, nodeOptionLabels, this, SWT.NONE);
	}


}
