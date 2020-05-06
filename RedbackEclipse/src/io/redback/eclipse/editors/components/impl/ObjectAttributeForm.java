package io.redback.eclipse.editors.components.impl;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import io.firebus.utils.DataMap;

import io.redback.eclipse.editors.components.CheckboxField;
import io.redback.eclipse.editors.components.Form;
import io.redback.eclipse.editors.components.Manager;
import io.redback.eclipse.editors.components.TextField;

public class ObjectAttributeForm extends Form
{
	protected ObjectAttributeRelatedForm relatedForm;
	
	public ObjectAttributeForm(DataMap d, Manager m, Composite p, int s) 
	{
		super(d, m, p, s);
		createUI();
	}
	
	public void createUI() {
		new TextField(_data, "name", "Name", this, SWT.NONE);
		new TextField(_data, "dbkey", "DB Key", this, SWT.NONE);
		new TextField(_data, "idgenerator", "ID Generator", this, SWT.NONE);
		new TextField(_data, "editable", "Editable (!)", this, SWT.NONE);
		new TextField(_data, "default", "Default (!)", this, SWT.NONE);
		new TextField(_data, "expression", "Expression (!)", this, SWT.NONE);
		new CheckboxField(_data, "search", "Can be Searched", this, SWT.NONE);
		CheckboxField cb = new CheckboxField(null, "islink", "Is Relationship", this, SWT.NONE);
		if(_data.get("relatedobject") != null)
			cb.setChecked(true);
		else
			cb.setChecked(false);
		
		refreshRelatedForm();		
	}


	
	protected void refreshRelatedForm() {
		if(_data.get("relatedobject") != null && relatedForm == null) {
			relatedForm = new ObjectAttributeRelatedForm(_data.getObject("relatedobject"), manager, this, SWT.NONE);
			layout(true, true);
		} else if(_data.get("relatedobject") == null && relatedForm != null) {
			relatedForm.dispose();
			layout(true, true);
		}
	}

	public void onFieldUpdate(String attribute, Object oldValue, Object newValue) {
		if(attribute.equals("islink")) {
			if(((Boolean)newValue).equals(true)) {
				_data.put("relatedobject", new DataMap());
			} else {
				_data.remove("relatedobject");
			}
			refreshRelatedForm();
		}
	}

}
