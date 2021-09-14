package io.redback.eclipse.editors.components.impl;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import io.firebus.data.DataMap;

import io.redback.eclipse.editors.components.CheckboxField;
import io.redback.eclipse.editors.components.Form;
import io.redback.eclipse.editors.components.JSONField;
import io.redback.eclipse.editors.components.MapField;
import io.redback.eclipse.editors.components.Manager;
import io.redback.eclipse.editors.components.TextField;

public class ViewDataSetForm extends Form
{
	public ViewDataSetForm(DataMap d, Manager m, Composite p, int s) 
	{
		super(d, m, p, s);
		createUI();
	}
	
	public void createUI() {
		new TextField(_data, "object", "Object", this, SWT.NONE);
		CheckboxField cb1 = new CheckboxField(null, "isbasefilter", "Has Base Filter", this, SWT.NONE);
		if(_data.get("basefilter") != null) {
			cb1.setChecked(true);
			new JSONField(_data, "basefilter", "Base Filter", this, SWT.NONE);
		} else {
			cb1.setChecked(false);
		}
		CheckboxField cb2 = new CheckboxField(null, "ismasterfilter", "Has Related Filter", this, SWT.NONE);
		if(_data.get("master") != null) {
			cb2.setChecked(true);
			new TextField(_data, "master.objectname", "Related Object", this, SWT.NONE);
			new JSONField(_data, "master.relationship", "Relationship", this, SWT.NONE);

		} else {
			cb2.setChecked(false);
		}
	}

	public void onFieldUpdate(String attribute, Object oldValue, Object newValue) {
		if(attribute.equals("isbasefilter")) {
			if(((Boolean)newValue).equals(true)) {
				_data.put("basefilter", new DataMap());
			} else {
				_data.remove("basefilter");
			}
			refresh();
		} else if(attribute.equals("ismasterfilter")) {
			if(((Boolean)newValue).equals(true)) {
				DataMap masterMap = new DataMap();
				masterMap.put("relationship", new DataMap());
				_data.put("master", masterMap);
			} else {
				_data.remove("master");
			}
			refresh();
		}
	}

}
