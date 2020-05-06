package io.redback.eclipse.editors.components.impl;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import io.firebus.utils.DataMap;

import io.redback.eclipse.editors.components.Form;
import io.redback.eclipse.editors.components.Manager;
import io.redback.eclipse.editors.components.TableField;
import io.redback.eclipse.editors.components.TextField;

public class ViewActionGroupForm extends Form
{
	public ViewActionGroupForm(DataMap d, Manager m, Composite p, int s) 
	{
		super(d, m, p, s);
		createUI();
	}
	
	public void createUI() {
		new TableField(_data, "actions", "Actions",  new String[][] {{"action", "Action"}, {"label", "Label"}, {"show", "Show"}}, this, SWT.NONE);
	}

	public void onFieldUpdate(String attribute, Object oldValue, Object newValue) {

	}

}
