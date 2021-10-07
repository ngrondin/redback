package io.redback.eclipse.editors.components.impl;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import io.firebus.data.DataMap;
import io.redback.eclipse.editors.components.Form;
import io.redback.eclipse.editors.components.Manager;
import io.redback.eclipse.editors.components.TextField;

public class IntegrationRootForm extends Form
{
	protected String label;
	
	public IntegrationRootForm(DataMap d, Manager m, Composite p, int s) 
	{
		super(d, m, p, s);
		createUI();
	}
	
	public void createUI() {
		new TextField(_data, "name", "Name", this, SWT.NONE);
		new TextField(_data, "clientid", "Client Id", this, SWT.NONE);
		new TextField(_data, "clientsecret", "Client Secret", this, SWT.NONE);
		new TextField(_data, "scope", "Scope", this, SWT.NONE);
		new TextField(_data, "loginurl", "Login URL", this, SWT.NONE);
		new TextField(_data, "tokenurl", "Token URL", this, SWT.NONE);
	}

	public void onFieldUpdate(String attribute, Object oldValue, Object newValue) {
		
	}


}
