package io.redback.eclipse.editors.components.impl;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.nic.firebus.utils.DataMap;

import io.redback.eclipse.editors.components.Form;
import io.redback.eclipse.editors.components.Manager;
import io.redback.eclipse.editors.components.TextField;

public class ViewRelatedInputForm extends Form
{
	public ViewRelatedInputForm(DataMap d, Manager m, Composite p, int s) 
	{
		super(d, m, p, s);
		createUI();
	}
	
	public void createUI() {
		new TextField(data, "label", "Label", this, SWT.NONE);
		new TextField(data, "attribute", "Attribute", this, SWT.NONE);
		new TextField(data, "icon", "Icon", this, SWT.NONE);
		new TextField(data, "size", "Size", this, SWT.NONE);
		new TextField(data, "show", "Show (!)", this, SWT.NONE);
		new TextField(data, "displayattribute", "Display Attribute", this, SWT.NONE);
		new TextField(data, "parentattribute", "Parent Attribute", this, SWT.NONE);
		new TextField(data, "childattribute", "Child Attribute", this, SWT.NONE);
	}

	public void onFieldUpdate(String attribute, Object oldValue, Object newValue) {

	}

}
