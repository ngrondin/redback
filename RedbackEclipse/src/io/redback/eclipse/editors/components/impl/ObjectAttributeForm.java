package io.redback.eclipse.editors.components.impl;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.nic.firebus.utils.DataMap;

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
		new TextField(data, "name", "Name", this, SWT.NONE);
		new TextField(data, "dbkey", "DB Key", this, SWT.NONE);
		new TextField(data, "idgenerator", "ID Generator", this, SWT.NONE);
		new TextField(data, "editable", "Editable", this, SWT.NONE);
		new TextField(data, "default", "Default", this, SWT.NONE);
		new TextField(data, "expression", "Expression", this, SWT.NONE);
		CheckboxField cb = new CheckboxField(null, "islink", "Is Relationship", this, SWT.NONE);
		if(data.get("relatedobject") != null)
			cb.setChecked(true);
		else
			cb.setChecked(false);
		
		refreshRelatedForm();		
	}


	
	protected void refreshRelatedForm() {
		if(data.get("relatedobject") != null && relatedForm == null) {
			relatedForm = new ObjectAttributeRelatedForm(data.getObject("relatedobject"), manager, this, SWT.NONE);
			layout(true, true);
		} else if(data.get("relatedobject") == null && relatedForm != null) {
			relatedForm.dispose();
			layout(true, true);
		}
	}

	public void onFieldUpdate(String attribute, Object oldValue, Object newValue) {
		if(attribute.equals("islink")) {
			if(((Boolean)newValue).equals(true)) {
				data.put("relatedobject", new DataMap());
			} else {
				data.remove("relatedobject");
			}
			refreshRelatedForm();
		}
	}

}
