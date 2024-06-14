package io.redback.eclipse.editors.components.impl;


import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import io.firebus.data.DataMap;
import io.redback.eclipse.editors.RedbackConfigEditor;
import io.redback.eclipse.editors.components.Form;
import io.redback.eclipse.editors.components.GetDataDialog;
import io.redback.eclipse.editors.components.Manager;
import io.redback.eclipse.editors.components.Navigator;
import io.redback.eclipse.editors.components.ScriptForm;

public class IntegrationManager extends Manager {
	
	public IntegrationManager(DataMap d, RedbackConfigEditor e, Composite parent, int style) {
		super(d, e, parent, style);
		createUI();
	}
	
	public void createUI() {
		super.createUI();
		sashForm.setWeights(new int[] {1, 3});
		sashForm.setOrientation(SWT.HORIZONTAL);
	}

	protected Navigator getNavigator() {
		return new IntegrationNavigator(_data, this, sashForm, SWT.PUSH);
	}

	protected Form getForm(String type, String name) {
		if(type.equals("root")) {
			return new IntegrationRootForm(_data, this, sashForm, SWT.PUSH);
		} else if(type.equals("header")) {
			return new ScriptForm(_data, "header", this, sashForm, SWT.PUSH);
		} else if(type.equals("tokenheader")) {
			return new ScriptForm(_data, "tokenheader", this, sashForm, SWT.PUSH);
		} else if(type.equals("method")) {
			return new ScriptForm(_data, "method", this, sashForm, SWT.PUSH);
		} else if(type.equals("url")) {
			return new ScriptForm(_data, "url", this, sashForm, SWT.PUSH);
		} else if(type.equals("getdomain")) {
			return new ScriptForm(_data, "getdomain", this, sashForm, SWT.PUSH);
		} else if(type.equals("body")) {
			return new ScriptForm(_data, "body", this, sashForm, SWT.PUSH);
		} else if(type.equals("response")) {
			return new ScriptForm(_data, "response", this, sashForm, SWT.PUSH);
		} else if(type.equals("include")) {
			return new ScriptForm(_data, "include", this, sashForm, SWT.PUSH);
		} else if(type.equals("function")) {
			return new ScriptForm(_data.getObject("functions"), name, this, sashForm, SWT.PUSH);
		} else {
			return null;
		}
	}

	public String createNode(String type, String name) {
		if(type.equals("functions")) {
			if(name == null) {
				GetDataDialog dialog = new GetDataDialog("Name of the new function", getShell());
				name = dialog.openReturnString();	
			} 
			if(_data.getObject("functions") == null) _data.put("functions", new DataMap());
			_data.getObject("functions").put(name, "");
		}
		setDataChanged(true);
		return name;
	}

	public void deleteNode(String type, String name) {
		if(type.equals("function")) {
			_data.getObject("functions").remove(name);
		}
	}
	

}
