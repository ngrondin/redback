package io.redback.eclipse.editors.components.impl;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import io.firebus.utils.DataMap;

import io.redback.eclipse.editors.components.Form;
import io.redback.eclipse.editors.components.Manager;
import io.redback.eclipse.editors.components.TextField;

public class ViewDynamicformForm extends Form
{
	public ViewDynamicformForm(DataMap d, Manager m, Composite p, int s) 
	{
		super(d, m, p, s);
		createUI();
	}
	
	public void createUI() {
		new TextField(data, "valueattribute", "Value Attribute", this, SWT.NONE);
		new TextField(data, "typeattribute", "Type Attribute", this, SWT.NONE);
		new TextField(data, "titleattribute", "Title Attribute", this, SWT.NONE);
		new TextField(data, "detailattribute", "Detail Attribute", this, SWT.NONE);
		new TextField(data, "labelattribute", "Label Attribute", this, SWT.NONE);
		new TextField(data, "orderattribute", "Order Attribute", this, SWT.NONE);
		new TextField(data, "categoryattribute", "Category Attribute", this, SWT.NONE);
		new TextField(data, "categoryorderattribute", "Category Order Attribute", this, SWT.NONE);
		new TextField(data, "dependencyattribute", "Dependency Attribute", this, SWT.NONE);
		new TextField(data, "dependencyvalueattribute", "Dependency Value Attribute", this, SWT.NONE);
	}

	public void onFieldUpdate(String attribute, Object oldValue, Object newValue) {

	}

}
