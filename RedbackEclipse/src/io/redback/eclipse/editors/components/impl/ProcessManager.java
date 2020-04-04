package io.redback.eclipse.editors.components.impl;


import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import io.firebus.utils.DataList;
import io.firebus.utils.DataMap;

import io.redback.eclipse.editors.RedbackConfigEditor;
import io.redback.eclipse.editors.components.Form;
import io.redback.eclipse.editors.components.GetDataDialog;
import io.redback.eclipse.editors.components.Manager;
import io.redback.eclipse.editors.components.Navigator;

public class ProcessManager extends Manager {
	
	protected String[][] options = new String[][] {
		{"interaction", "Interaction"}, 
		{"action", "Action"},
		{"condition", "Condition"}, 
		{"rbobjectget", "RbObject Get"}, 
		{"rbobjectexecute", "RbObject Execute"},
		{"rbobjectupdate", "RbObject Update"}, 
		{"script", "Script"},
		{"domainservice", "Domain Service"}
	};
	
	public ProcessManager(DataMap d, RedbackConfigEditor e, Composite parent, int style) {
		super(d, e, parent, style);
		if(!data.containsKey("nodes"))
			data.put("nodes", new DataList());
		createUI();
	}
	
	public void createUI() {
		super.createUI();
		sashForm.setWeights(new int[] {1, 1});
		sashForm.setOrientation(SWT.VERTICAL);
	}

	protected Navigator getNavigator() {
		return new ProcessNavigator(data, this, sashForm, SWT.PUSH);
	}

	protected Form getForm(String type, String name) {
		if(type.equals("header")) {
			return new ProcessHeaderForm(data, data.getList("nodes"), this, sashForm, SWT.PUSH);
		} else if(type.equals("interaction")) {
			return new ProcessInteractionForm(getNodeById(name), data.getList("nodes"), this, sashForm, SWT.PUSH);
		} else if(type.equals("action")) {
			return new ProcessActionForm(getNodeById(name), data.getList("nodes"), this, sashForm, SWT.PUSH);
		} else if(type.equals("condition")) {
			return new ProcessConditionForm(getNodeById(name), data.getList("nodes"), this, sashForm, SWT.PUSH);
		} else if(type.equals("script")) {
			return new ProcessScriptForm(getNodeById(name), data.getList("nodes"), this, sashForm, SWT.PUSH);
		} else if(type.equals("rbobjectget")) {
			return new ProcessRbObjectGetForm(getNodeById(name), data.getList("nodes"), this, sashForm, SWT.PUSH);
		} else if(type.equals("rbobjectupdate")) {
			return new ProcessRbObjectUpdateForm(getNodeById(name), data.getList("nodes"), this, sashForm, SWT.PUSH);
		} else if(type.equals("rbobjectexecute")) {
			return new ProcessRbObjectExecuteForm(getNodeById(name), data.getList("nodes"), this, sashForm, SWT.PUSH);
		} else if(type.equals("domainservice")) {
			return new ProcessDomainServiceForm(getNodeById(name), data.getList("nodes"), this, sashForm, SWT.PUSH);
		} else {
			return null;
		}
	}
	
	private DataMap getNodeById(String id) {
		for(int i = 0; i < data.getList("nodes").size(); i++) {
			DataMap node = data.getList("nodes").getObject(i);
			if(node.getString("id").equals(id))
				return node;
		}
		return null;
	}

	public String createNode(String type, String name) {
		GetDataDialog dialog = new GetDataDialog("Type of node", options, getShell());
		String newType = dialog.openReturnString();
		int id = -1;
		for(int i = 0; i < data.getList("nodes").size(); i++) {
			int nodeId = Integer.parseInt(data.getList("nodes").getObject(i).getString("id"));
			if(id <= nodeId)
				id = nodeId + 1;
		}
		DataMap newNode = new DataMap();
		newNode.put("id", id);
		newNode.put("type", newType);
		newNode.put("position", new DataMap());
		String[] parts = name.split("-");
		newNode.getObject("position").put("x", Integer.parseInt(parts[0]));
		newNode.getObject("position").put("y", Integer.parseInt(parts[1]));
		if(newType.equals("interaction")) {
			newNode.put("actions", new DataList());
			newNode.put("notidication", new DataList());
		}
		data.getList("nodes").add(newNode);
		return "" + id;
	}

	public void deleteNode(String type, String name) {
		DataList nodes = data.getList("nodes");
		for(int i = 0; i < nodes.size(); i++) {
			if(nodes.getObject(i).getString("id").equals(name)) {
				nodes.remove(i);
				break;
			}
		}
	}
	
	public void moveNode(String name, String target) {
	}

}
