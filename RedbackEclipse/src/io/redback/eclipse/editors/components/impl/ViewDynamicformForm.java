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
		new TextField(_data, "valueattribute", "Value Attribute", this, SWT.NONE);
		new TextField(_data, "optionsattribute", "Options Attribute", this, SWT.NONE);
		new TextField(_data, "typeattribute", "Type Attribute", this, SWT.NONE);
		new TextField(_data, "titleattribute", "Title Attribute", this, SWT.NONE);
		new TextField(_data, "detailattribute", "Detail Attribute", this, SWT.NONE);
		new TextField(_data, "labelattribute", "Label Attribute", this, SWT.NONE);
		new TextField(_data, "orderattribute", "Order Attribute", this, SWT.NONE);
		new TextField(_data, "categoryattribute", "Category Attribute", this, SWT.NONE);
		new TextField(_data, "categoryorderattribute", "Category Order Attribute", this, SWT.NONE);
		new TextField(_data, "dependencyattribute", "Dependency Attribute", this, SWT.NONE);
		new TextField(_data, "dependencyvalueattribute", "Dependency Value Attribute", this, SWT.NONE);
	}

	public void onFieldUpdate(String attribute, Object oldValue, Object newValue) {

	}

}
