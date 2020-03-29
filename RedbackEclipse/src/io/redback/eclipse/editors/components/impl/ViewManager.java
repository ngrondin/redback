package io.redback.eclipse.editors.components.impl;

import java.util.Arrays;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import io.firebus.utils.DataEntity;
import io.firebus.utils.DataList;
import io.firebus.utils.DataMap;

import io.redback.eclipse.editors.RedbackConfigEditor;
import io.redback.eclipse.editors.components.Form;
import io.redback.eclipse.editors.components.Manager;
import io.redback.eclipse.editors.components.Navigator;

public class ViewManager extends Manager {
	
	protected List<String> typesWithContent = Arrays.asList(new String[] {"hsection", "vsection", "dataset", "tab", "tabsection", "form", "layout", "fileset"});

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
			return new ViewHeaderForm(data, this, sashForm, SWT.PUSH);
		} else if(type.equals("vsection")) {
			return new ViewSectionForm(data.getObject(name), this, sashForm, SWT.PUSH);
		} else if(type.equals("hsection")) {
			return new ViewSectionForm(data.getObject(name), this, sashForm, SWT.PUSH);
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
			return new ViewTextAreaForm(data.getObject(name), this, sashForm, SWT.PUSH);
		} else if(type.equals("durationinput")) {
			return new ViewInputForm(data.getObject(name), this, sashForm, SWT.PUSH);
		} else if(type.equals("switch")) {
			return new ViewInputForm(data.getObject(name), this, sashForm, SWT.PUSH);
		} else if(type.equals("actiongroup")) {
			return new ViewActionGroupForm(data.getObject(name), this, sashForm, SWT.PUSH);
		} else if(type.equals("list")) {
			return new ViewListForm(data.getObject(name), this, sashForm, SWT.PUSH);
		} else if(type.equals("list3")) {
			return new ViewList3Form(data.getObject(name), this, sashForm, SWT.PUSH);
		} else if(type.equals("search")) {
			return new ViewSearchForm(data.getObject(name), this, sashForm, SWT.PUSH);
		} else if(type.equals("map")) {
			return new ViewMapForm(data.getObject(name), this, sashForm, SWT.PUSH);
		} else if(type.equals("button")) {
			return new ViewButtonForm(data.getObject(name), this, sashForm, SWT.PUSH);
		} else if(type.equals("roundbutton")) {
			return new ViewButtonForm(data.getObject(name), this, sashForm, SWT.PUSH);
		} else if(type.equals("tab")) {
			return new ViewTabForm(data.getObject(name), this, sashForm, SWT.PUSH);
		} else if(type.equals("spacer")) {
			return new ViewSpacerForm(data.getObject(name), this, sashForm, SWT.PUSH);
		} else if(type.equals("fileset")) {
			return new ViewFilesetForm(data.getObject(name), this, sashForm, SWT.PUSH);
		} else if(type.equals("filelist")) {
			return new ViewFilelistForm(data.getObject(name), this, sashForm, SWT.PUSH);
		} else if(type.equals("filedrop")) {
			return new ViewFiledropForm(data.getObject(name), this, sashForm, SWT.PUSH);
		} else if(type.equals("log")) {
			return new ViewLogForm(data.getObject(name), this, sashForm, SWT.PUSH);
		} else if(type.equals("link")) {
			return new ViewLinkForm(data.getObject(name), this, sashForm, SWT.PUSH);
		} else if(type.equals("dynamicform")) {
			return new ViewDynamicformForm(data.getObject(name), this, sashForm, SWT.PUSH);
		} else {
			return null;
		}
	}

	public String createNode(String type, String name) {
		String newName = null;
		DataMap parentData = name != null ? data.getObject(name) : data;
		if(parentData.containsKey("content")) {
			DataMap newNode = new DataMap("type", type);
			if(typesWithContent.contains(type))
				newNode.put("content", new DataList());
			DataList list = parentData.getList("content");
			newName = (name != null ? name + "." : "") + "content." + list.size();
			list.add(newNode);
			setDataChanged(true);
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
		setDataChanged(true);
	}
	
	public void moveNode(String name, String target) {
		DataMap targetNode = data.getObject(target);
		if(targetNode.containsKey("content")) {
			String sourceContentNodeName = name.substring(0, name.lastIndexOf("."));
			int sourceContentIndex = Integer.parseInt(name.substring(name.lastIndexOf(".") + 1));
			DataMap mapToMove = data.getObject(name);
			DataList sourceList = data.getList(sourceContentNodeName); 
			DataList targetList = data.getList(target + ".content");
			sourceList.remove(sourceContentIndex);
			targetList.add(mapToMove);
			setDataChanged(true);
		}
	}

}
