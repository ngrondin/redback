package io.redback.eclipse.editors.components.impl;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.nic.firebus.utils.DataMap;

import io.redback.eclipse.editors.components.CheckboxField;
import io.redback.eclipse.editors.components.Form;
import io.redback.eclipse.editors.components.KeyValueForm;
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
		new TextField(data, "object", "Object", this, SWT.NONE);
		CheckboxField cb1 = new CheckboxField(null, "isbasefilter", "Base Filter", this, SWT.NONE);
		if(data.get("basefilter") != null) {
			cb1.setChecked(true);
			new KeyValueForm(data.getObject("basefilter"), manager, this, SWT.NONE);
		} else {
			cb1.setChecked(false);
		}
		CheckboxField cb2 = new CheckboxField(null, "ismasterfilter", "Related Filter", this, SWT.NONE);
		if(data.get("master") != null) {
			cb2.setChecked(true);
			new TextField(data, "master.objectname", "Related Object", this, SWT.NONE);
			new KeyValueForm(data.getObject("master.relationship"), manager, this, SWT.NONE);

		} else {
			cb2.setChecked(false);
		}
	}

	public void onFieldUpdate(String attribute, Object oldValue, Object newValue) {
		if(attribute.equals("isbasefilter")) {
			if(((Boolean)newValue).equals(true)) {
				data.put("basefilter", new DataMap());
			} else {
				data.remove("basefilter");
			}
			refresh();
		} else if(attribute.equals("ismasterfilter")) {
			if(((Boolean)newValue).equals(true)) {
				DataMap masterMap = new DataMap();
				masterMap.put("relationship", new DataMap());
				data.put("master", masterMap);
			} else {
				data.remove("master");
			}
			refresh();
		}
	}

}
