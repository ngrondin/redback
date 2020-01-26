package io.redback.eclipse.editors.components.impl;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.widgets.Composite;

import com.nic.firebus.utils.DataMap;

import io.redback.eclipse.editors.components.CheckboxField;
import io.redback.eclipse.editors.components.Form;
import io.redback.eclipse.editors.components.MapField;
import io.redback.eclipse.editors.components.ScriptField;
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
		CheckboxField cb = new CheckboxField(null, "isscript", "Is Script", this, SWT.NONE);
		if(data.get("listfilter") instanceof DataMap) {
			cb.setChecked(false);
			new MapField(data, "listfilter", "List Filter Map", this, SWT.NONE);
		} else {
			cb.setChecked(true);
			ScriptField sf = new ScriptField(data, "listfilter", "List Filter Script", this, SWT.NONE);
			sf.setLayoutData(new RowData(500, 200));
		}
	}

	public void onFieldUpdate(String attribute, Object oldValue, Object newValue) {
		if(attribute.equals("isscript")) {
			if((boolean)newValue == true) {
				data.put("listfilter", "");
			} else {
				data.put("listfilter", new DataMap());
			}
			refresh();
		}
	}


}
