package io.redback.eclipse.editors.components.impl;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;

import io.firebus.data.DataList;
import io.firebus.data.DataMap;
import io.redback.eclipse.editors.components.CheckboxField;
import io.redback.eclipse.editors.components.Form;
import io.redback.eclipse.editors.components.Manager;
import io.redback.eclipse.editors.components.TextField;

public class ObjectAttributeForm extends Form
{
	protected Composite relatedFormContainer;
	protected ObjectAttributeRelatedForm relatedForm;
	
	protected Composite copyFormContainer;
	protected ObjectAttributeCopyForm copyForm;
	
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
		CheckboxField cb1 = new CheckboxField(null, "islink", "Is Relationship", this, SWT.NONE);
		cb1.setChecked(_data.get("relatedobject") != null);
		relatedFormContainer = new Composite(this, SWT.NONE);
		relatedFormContainer.setLayout(new RowLayout(SWT.VERTICAL));
		CheckboxField cb2 = new CheckboxField(null, "docopy", "Copy", this, SWT.NONE);
		cb2.setChecked(_data.get("copyto") != null);
		copyFormContainer = new Composite(this, SWT.NONE);
		copyFormContainer.setLayout(new RowLayout(SWT.VERTICAL));
		refreshRelatedForm();		
		refreshCopyForm();		
	}

	protected void refreshRelatedForm() {
		if(_data.get("relatedobject") != null && relatedForm == null) {
			relatedForm = new ObjectAttributeRelatedForm(_data.getObject("relatedobject"), manager, relatedFormContainer, SWT.NONE);
			layout(true, true);
		} else if(_data.get("relatedobject") == null && relatedForm != null) {
			relatedForm.dispose();
			relatedForm = null;
			layout(true, true);
		}
	}
	
	protected void refreshCopyForm() {
		if(_data.get("copyto") != null && copyForm == null) {
			copyForm = new ObjectAttributeCopyForm(_data, manager, copyFormContainer, SWT.NONE);
			layout(true, true);
		} else if(_data.get("copyto") == null && copyForm != null) {
			copyForm.dispose();
			copyForm = null;
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
		} else if(attribute.equals("docopy")) {
			if(((Boolean)newValue).equals(true)) {
				_data.put("copyto", new DataList());
			} else {
				_data.remove("copyto");
			}
			refreshCopyForm();
		}
	}

}
