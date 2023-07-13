package io.redback.eclipse.editors.components.impl;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.widgets.Composite;

import io.firebus.data.DataMap;
import io.redback.eclipse.editors.components.CheckboxField;
import io.redback.eclipse.editors.components.Form;
import io.redback.eclipse.editors.components.Manager;
import io.redback.eclipse.editors.components.ScriptField;
import io.redback.eclipse.editors.components.TextField;

public class PackForm extends Form
{
	protected String label;
	
	public PackForm(DataMap d, Manager m, Composite p, int s) 
	{
		super(d, m, p, s);
		createUI();
	}
	
	public void createUI() {
		new TextField(_data, "accesscat", "Access Category", this, SWT.NONE);
		if(!_data.containsKey("queries") || (_data.containsKey("queries") && _data.getList("queries").size() == 0)) {
			CheckboxField cb = new CheckboxField(null, "isscript", "Is Script", this, SWT.NONE);
			if(_data.containsKey("script")) {
				cb.setChecked(true);
				ScriptField sf = new ScriptField(_data, "script", "Script", this, SWT.NONE);
				sf.setLayoutData(new RowData(600, 600));
			}
		}
	}

	public void onFieldUpdate(String attribute, Object oldValue, Object newValue) {
		if(attribute.equals("isscript")) {
			if((boolean)newValue == true) {
				_data.put("script", "");
			} else {
				_data.remove("script");
			}
			refresh();
		}
	}


}
