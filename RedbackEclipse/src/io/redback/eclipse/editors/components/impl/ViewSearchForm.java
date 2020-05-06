package io.redback.eclipse.editors.components.impl;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import io.firebus.utils.DataList;
import io.firebus.utils.DataMap;

import io.redback.eclipse.editors.components.CheckboxField;
import io.redback.eclipse.editors.components.Form;
import io.redback.eclipse.editors.components.TableField;
import io.redback.eclipse.editors.components.Manager;

public class ViewSearchForm extends Form
{
	public ViewSearchForm(DataMap d, Manager m, Composite p, int s) 
	{
		super(d, m, p, s);
		createUI();
	}
	
	public void createUI() {
		CheckboxField cb1 = new CheckboxField(null, "hasfilter", "Has Filter?", this, SWT.NONE);
		if(_data.get("filter") != null) {
			cb1.setChecked(true);
			new TableField(_data, "filter.attributes", "Filter Fields", new String[][] {{"attribute", "Attribute"}, {"label", "Label"}, {"type", "Type"}}, this, SWT.NONE);

		} else {
			cb1.setChecked(false);
		}
	}

	public void onFieldUpdate(String attribute, Object oldValue, Object newValue) {
		if(attribute.equals("hasfilter")) {
			if(((Boolean)newValue).equals(true)) {
				_data.put("filter", new DataMap("attributes", new DataList()));
			} else {
				_data.remove("filter");
			}
			refresh();
		} 
	}

}
