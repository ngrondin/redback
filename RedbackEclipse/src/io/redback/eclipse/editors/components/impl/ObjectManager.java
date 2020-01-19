package io.redback.eclipse.editors.components.impl;


import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.nic.firebus.utils.DataList;
import com.nic.firebus.utils.DataMap;

import io.redback.eclipse.editors.components.Form;
import io.redback.eclipse.editors.components.Manager;
import io.redback.eclipse.editors.components.GetDataDialog;
import io.redback.eclipse.editors.components.Navigator;
import io.redback.eclipse.editors.components.ScriptForm;

public class ObjectManager extends Manager {
	
	public ObjectManager(DataMap d, Composite parent, int style) {
		super(d, parent, style);
		sashForm.setWeights(new int[] {1, 2});
		if(!data.containsKey("attributes"))
			data.put("attributes", new DataList());
		if(!data.containsKey("scripts"))
			data.put("scripts", new DataMap());
	}
	
	protected Navigator getNavigator() {
		return new ObjectTree(data, this, sashForm, SWT.PUSH);
	}

	protected Form getForm(String type, String name) {
		if(type.equals("general")) {
			return new ObjectHeaderForm(data, this, sashForm, SWT.PUSH);
		} else if(type.equals("attribute")) {
			DataList attributes = data.getList("attributes");
			for(int i = 0; i < attributes.size(); i++)
				if(attributes.getObject(i).getString("name").equals(name))
					return new ObjectAttributeForm(attributes.getObject(i), this, sashForm, SWT.PUSH);	
			return null;
		} else if(type.equals("script")) {
			return new ScriptForm(data.getObject("scripts"), name, this, sashForm, SWT.PUSH);
		} else {
			return null;
		}
	}

	public void createNode(String type, String name) {
		if(type.equals("attribute")) {
			if(name == null) {
				GetDataDialog dialog = new GetDataDialog("Name of the new attribute", getShell());
				name = dialog.openReturnString();
			} 
			data.getList("attributes").add(new DataMap("name", name));
			navigator.refresh();
		} else if(type.equals("script")) {
			if(name == null) {
				GetDataDialog dialog = new GetDataDialog("Name of the new method", getShell());
				name = dialog.openReturnString();	
			} 
			data.getObject("scripts").put(name, "");
			navigator.refresh();
		}
	}

	public void deleteNode(String type, String name) {
		System.out.println("Deleting node " + name);
	}

}
