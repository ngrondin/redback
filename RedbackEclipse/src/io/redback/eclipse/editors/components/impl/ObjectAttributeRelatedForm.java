package io.redback.eclipse.editors.components.impl;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.nic.firebus.utils.DataMap;

import io.redback.eclipse.editors.components.Form;
import io.redback.eclipse.editors.components.KeyValueForm;
import io.redback.eclipse.editors.components.Manager;
import io.redback.eclipse.editors.components.TextField;

public class ObjectAttributeRelatedForm extends Form
{
	public ObjectAttributeRelatedForm(DataMap d, Manager m, Composite p, int s) 
	{
		super(d, m, p, s);
		if(!data.containsKey("listfilter"))
			data.put("listfilter", new DataMap());
		createUI();
	}

	public void createUI() {
		new TextField(data, "name", "Object", this, SWT.NONE);
		new TextField(data, "linkattribute", "Object Link", this, SWT.NONE);
		Label l = new Label(this, SWT.NONE);
		l.setText("List Filter");
		new KeyValueForm(data.getObject("listfilter"), manager, this, SWT.NONE);
	}

	public void onFieldUpdate(String attribute, Object oldValue, Object newValue) {

	}


}
