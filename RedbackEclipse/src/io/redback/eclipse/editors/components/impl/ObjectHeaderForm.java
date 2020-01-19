package io.redback.eclipse.editors.components.impl;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.nic.firebus.utils.DataMap;

import io.redback.eclipse.editors.components.Form;
import io.redback.eclipse.editors.components.Manager;
import io.redback.eclipse.editors.components.TextField;

public class ObjectHeaderForm extends Form
{
	public ObjectHeaderForm(DataMap d, Manager m, Composite p, int s) 
	{
		super(d, m, p, s);
		createUI();
	}

	public void createUI() {
		new TextField(data, "name", "Name", this, SWT.NONE);
		new TextField(data, "collection", "DB Collection", this, SWT.NONE);
		new TextField(data, "uiddbkey", "UID DB Key", this, SWT.NONE);
		new TextField(data, "domaindbkey", "Domain DB Key", this, SWT.NONE);
		new TextField(data, "uidgenerator", "UID Generator", this, SWT.NONE);
		new TextField(data, "group", "Group", this, SWT.NONE);
	}

	public void onFieldUpdate(String attribute, Object oldValue, Object newValue) {

	}


}
