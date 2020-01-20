package io.redback.eclipse.editors.components.impl;

import java.util.Arrays;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.nic.firebus.utils.DataEntity;
import com.nic.firebus.utils.DataList;
import com.nic.firebus.utils.DataMap;

import io.redback.eclipse.editors.RedbackConfigEditor;
import io.redback.eclipse.editors.components.Form;
import io.redback.eclipse.editors.components.Manager;
import io.redback.eclipse.editors.components.Navigator;

public class ViewManager extends Manager {
	
	protected List<String> typesWithContent = Arrays.asList(new String[] {"hsection", "vsection", "dataset", "tab", "tabsection", "form", "layout"});

	public ViewManager(DataMap d, RedbackConfigEditor e, Composite parent, int style) {
		super(d, e, parent, style);
		if(!data.containsKey("content"))
			data.put("content", new DataList());
		createUI();
	}
	
	public void createUI() {
		super.createUI();
		sashForm.setWeights(new int[] {1, 2});
	}

	protected Navigator getNavigator() {
		return new ViewTree(data, this, sashForm, SWT.PUSH);
	}

	protected Form getForm(String type, String name) {
		if(type.equals("root")) {
			return null;
		} else if(type.equals("view")) {
			return new ViewViewForm(data.getObject(name), this, sashForm, SWT.PUSH);
		} else if(type.equals("dataset")) {
			return new ViewDataSetForm(data.getObject(name), this, sashForm, SWT.PUSH);
		} else if(type.equals("input")) {
			return new ViewInputForm(data.getObject(name), this, sashForm, SWT.PUSH);
		} else if(type.equals("relatedinput")) {
			return new ViewRelatedInputForm(data.getObject(name), this, sashForm, SWT.PUSH);
		} else if(type.equals("datepicker")) {
			return new ViewDateInputForm(data.getObject(name), this, sashForm, SWT.PUSH);
		} else if(type.equals("textarea")) {
			return new ViewInputForm(data.getObject(name), this, sashForm, SWT.PUSH);
		} else if(type.equals("duration")) {
			return new ViewInputForm(data.getObject(name), this, sashForm, SWT.PUSH);
		} else if(type.equals("switch")) {
			return new ViewInputForm(data.getObject(name), this, sashForm, SWT.PUSH);
		} else if(type.equals("actiongroup")) {
			return new ViewActionGroupForm(data.getObject(name), this, sashForm, SWT.PUSH);
		} else {
			return null;
		}
	}

	public String createNode(String type, String name) {
		String newName = null;
		DataMap parentData = data.getObject(name);
		if(parentData.containsKey("content")) {
			DataMap newNode = new DataMap("type", type);
			if(typesWithContent.contains(type))
				newNode.put("content", new DataList());
			DataList list = parentData.getList("content");
			newName = name + ".content." + list.size();
			list.add(newNode);
		}
		return newName;
	}

	public void deleteNode(String type, String name) {
		int pos = name.lastIndexOf(".");
		String parent = name.substring(0, pos);
		String child = name.substring(pos + 1);
		DataEntity entity = data.get(parent);
		if(entity instanceof DataMap)
			((DataMap)entity).remove(child);
		else if(entity instanceof DataList)
			((DataList)entity).remove(Integer.parseInt(child));
	}

}
