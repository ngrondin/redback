package io.redback.eclipse.editors.components.impl;


import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import io.firebus.data.DataList;
import io.firebus.data.DataMap;
import io.redback.eclipse.editors.RedbackConfigEditor;
import io.redback.eclipse.editors.components.Form;
import io.redback.eclipse.editors.components.GetDataDialog;
import io.redback.eclipse.editors.components.Manager;
import io.redback.eclipse.editors.components.Navigator;

public class PackManager extends Manager {
	
	public PackManager(DataMap d, RedbackConfigEditor e, Composite parent, int style) {
		super(d, e, parent, style);
		createUI();
	}
	
	public void createUI() {
		super.createUI();
		sashForm.setWeights(new int[] {1, 2});
	}
	
	protected Navigator getNavigator() {
		return new PackTree(_data, this, sashForm, SWT.PUSH);
	}

	protected Form getForm(String type, String name) {
		if(type.equals("root")) {
			return new PackForm(_data, this, sashForm, SWT.PUSH);
		} else if (type.equals("query")) {
			DataList list = _data.getList("queries");
			DataMap query = null;
			for(int i = 0; i < list.size(); i++)
				if(list.getObject(i).getString("object").equals(name))
					query = list.getObject(i);
			return new PackQueryForm(query, this, sashForm, SWT.PUSH);
		} else {
			return null;
		}
	}

	public String createNode(String type, String name) {
		if(type.equals("query")) {
			if(name == null) {
				GetDataDialog dialog = new GetDataDialog("Object to query", getShell());
				name = dialog.openReturnString();
			} 
			_data.getList("queries").add(new DataMap("object", name));
		} else {
			return null;
		}
		return name;
	}

	public void deleteNode(String type, String name) {
		if(type.equals("query")) {
			DataList list = _data.getList("queries");
			for(int i = 0; i < list.size(); i++) {
				if(list.getObject(i).getString("object").equals(name))
					list.remove(i);
			}
		}
	}

}
