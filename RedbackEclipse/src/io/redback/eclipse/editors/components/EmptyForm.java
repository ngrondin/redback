package io.redback.eclipse.editors.components;

import org.eclipse.swt.widgets.Composite;

public class EmptyForm extends Form {

	public EmptyForm(Composite p, int s) {
		super(null, null, p, s);
	}

	public void createUI() {
		
	}

	public void onFieldUpdate(String attribute, Object oldValue, Object newValue) {
		
	}


}
