package io.redback.eclipse.editors.components.impl;


import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import io.firebus.utils.DataList;
import io.firebus.utils.DataMap;

import io.redback.eclipse.editors.RedbackConfigEditor;
import io.redback.eclipse.editors.components.Form;
import io.redback.eclipse.editors.components.Manager;
import io.redback.eclipse.editors.components.GetDataDialog;
import io.redback.eclipse.editors.components.Navigator;
import io.redback.eclipse.editors.components.ScriptForm;

public class ObjectManager extends Manager {
	
	public ObjectManager(DataMap d, RedbackConfigEditor e, Composite parent, int style) {
		super(d, e, parent, style);
		if(!data.containsKey("attributes"))
			data.put("attributes", new DataList());
		if(!data.containsKey("scripts"))
			data.put("scripts", new DataMap());
		createUI();
	}
	
	public void createUI() {
		super.createUI();
		sashForm.setWeights(new int[] {1, 2});
	}
	
	protected Navigator getNavigator() {
		return new ObjectTree(data, this, sashForm, SWT.PUSH);
	}

	private DataMap getAttribute(String name) {
		DataList attributes = data.getList("attributes");
		for(int i = 0; i < attributes.size(); i++)
			if(attributes.getObject(i).getString("name").equals(name))
				return attributes.getObject(i);
		return null;
	}
	
	protected Form getForm(String type, String name) {
		if(type.equals("root")) {
			return new ObjectHeaderForm(data, this, sashForm, SWT.PUSH);
		} else if(type.equals("attribute")) {
			return new ObjectAttributeForm(getAttribute(name), this, sashForm, SWT.PUSH);	
		} else if(type.equals("script")) {
			return new ScriptForm(data.getObject("scripts"), name, this, sashForm, SWT.PUSH);
		} else if(type.equals("attributescript")) {
			String attributeName = name.substring(0, name.indexOf("."));
			String scriptName = name.substring(name.indexOf(".") + 1);
			return new ScriptForm(getAttribute(attributeName).getObject("scripts"), scriptName, this, sashForm, SWT.PUSH);
		} else {
			return null;
		}
	}

	public String createNode(String type, String name) {
		if(type.equals("attribute")) {
			if(name == null) {
				GetDataDialog dialog = new GetDataDialog("Name of the new attribute", getShell());
				name = dialog.openReturnString();
			} 
			data.getList("attributes").add(new DataMap("name", name));
		} else if(type.equals("script")) {
			if(name == null) {
				GetDataDialog dialog = new GetDataDialog("Name of the new method", getShell());
				name = dialog.openReturnString();	
			} 
			data.getObject("scripts").put(name, "");
		} else if(type.equals("attributescript")) {
			String attributeName = name.substring(0, name.indexOf("."));
			String scriptName = name.substring(name.indexOf(".") + 1);
			DataMap attribute = getAttribute(attributeName);
			if(attribute != null) {
				if(!attribute.containsKey("scripts"))
					attribute.put("scripts", new DataMap());
				attribute.getObject("scripts").put(scriptName, "");
			}			
		}
		setDataChanged(true);
		return name;
	}

	public void deleteNode(String type, String name) {
		if(type.equals("attribute")) {
			DataList attributes = data.getList("attributes");
			for(int i = 0; i < attributes.size(); i++) {
				if(attributes.getObject(i).getString("name").equals(name))
					attributes.remove(i);
			}
		} else if(type.equals("scripts")) {
			data.getObject("scripts").remove(name);
		} else if(type.equals("attributescript")) {
			String attributeName = name.substring(0, name.indexOf("."));
			String scriptName = name.substring(name.indexOf(".") + 1);
			DataMap attribute = getAttribute(attributeName);
			attribute.getObject("scripts").remove(scriptName);
			if(attribute.getObject("scripts").keySet().size() == 0)
				attribute.remove("scripts");
		}
	}

}
