package io.redback.eclipse.editors.components;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

import io.firebus.data.DataMap;

public class ScriptForm extends Form {

	protected String attribute;
	
	public ScriptForm(DataMap d, String a, Manager m, Composite p, int s) {
		super(d, m, p, s);
		attribute = a;
		createUI();
	}

	public void createUI() {
		setLayout(new FillLayout());
		new ScriptField(_data, attribute, attribute, this, SWT.NONE);
	}

	public void onFieldUpdate(String attribute, Object oldValue, Object newValue) {
		
	}

}
