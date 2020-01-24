package io.redback.eclipse.editors.components.impl;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.nic.firebus.utils.DataMap;

import io.redback.eclipse.editors.components.CheckboxField;
import io.redback.eclipse.editors.components.Form;
import io.redback.eclipse.editors.components.Manager;
import io.redback.eclipse.editors.components.TextField;

public class ViewSectionForm extends Form
{
	public ViewSectionForm(DataMap d, Manager m, Composite p, int s) 
	{
		super(d, m, p, s);
		createUI();
	}
	
	public void createUI() {
		new CheckboxField(data, "grow", "Grow", this, SWT.NONE).setAsOneAndZero(true);
	}

	public void onFieldUpdate(String attribute, Object oldValue, Object newValue) {

	}

}
