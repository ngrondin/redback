package io.redback.eclipse.editors.components.impl;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import io.firebus.data.DataList;
import io.firebus.data.DataMap;
import io.redback.eclipse.editors.components.Form;
import io.redback.eclipse.editors.components.Manager;
import io.redback.eclipse.editors.components.TableField;

public class ObjectAttributeCopyForm extends Form
{
	public ObjectAttributeCopyForm(DataMap d, Manager m, Composite p, int s) 
	{
		super(d, m, p, s);
		if(!_data.containsKey("copyto"))
			_data.put("copyto", new DataList());
		createUI();
	}

	public void createUI() {
		new TableField(_data, "copyto", "Copy To", new String[][] {{"relationship", "Relationship"}, {"attribute", "Attribute"}}, this, SWT.NONE);
	}

	public void onFieldUpdate(String attribute, Object oldValue, Object newValue) {

	}

}
