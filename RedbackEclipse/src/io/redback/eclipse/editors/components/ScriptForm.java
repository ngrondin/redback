package io.redback.eclipse.editors.components;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

import com.nic.firebus.utils.DataMap;

public class ScriptForm extends Form {

	protected String attribute;
	
	public ScriptForm(DataMap d, String a, Manager m, Composite p, int s) {
		super(d, m, p, s);
		attribute = a;
		createUI();
	}

	public void createUI() {
		setLayout(new FillLayout());
		new ScriptField(data, attribute, this, SWT.NONE);
	}

	public void onFieldUpdate(String attribute, Object oldValue, Object newValue) {
		
	}

}
