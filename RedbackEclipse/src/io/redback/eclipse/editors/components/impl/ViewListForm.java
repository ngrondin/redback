package io.redback.eclipse.editors.components.impl;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.nic.firebus.utils.DataMap;

import io.redback.eclipse.editors.components.Form;
import io.redback.eclipse.editors.components.Manager;
import io.redback.eclipse.editors.components.TextField;

public class ViewListForm extends Form
{
	public ViewListForm(DataMap d, Manager m, Composite p, int s) 
	{
		super(d, m, p, s);
		createUI();
	}
	
	public void createUI() {
		new TextField(data, "line1", "Line 1", this, SWT.NONE);
		new TextField(data, "line2", "Line 2", this, SWT.NONE);
	}

	public void onFieldUpdate(String attribute, Object oldValue, Object newValue) {

	}

}
