package io.redback.eclipse.editors.components.impl;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import io.firebus.data.DataList;
import io.firebus.data.DataMap;

import io.redback.eclipse.editors.components.Manager;
import io.redback.eclipse.editors.components.MapField;
import io.redback.eclipse.editors.components.SelectField;
import io.redback.eclipse.editors.components.TextField;

public class ProcessRbObjectUpdateForm extends ProcessForm
{
	public ProcessRbObjectUpdateForm(DataMap d, DataList n, Manager m, Composite p, int s) 
	{
		super(d, n, m, p, s);
		createUI();
	}
	
	public void createUI() {
		
		new TextField(_data, "name", "Name", this, SWT.NONE);
		new TextField(_data, "object", "Object", this, SWT.NONE);
		new TextField(_data, "uid", "Uid (!)", this, SWT.NONE);
		new MapField(_data, "data", "Data", this, SWT.NONE);
		new MapField(_data, "outmap", "Result Map", this, SWT.NONE);
		new SelectField(_data, "nextnode", "Next Node", nodeOptions, nodeOptionLabels, this, SWT.NONE);
	}


}
