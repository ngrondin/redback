package io.redback.eclipse.editors.components.impl;


import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import io.firebus.utils.DataMap;

import io.redback.eclipse.editors.RedbackConfigEditor;
import io.redback.eclipse.editors.components.Form;
import io.redback.eclipse.editors.components.Manager;
import io.redback.eclipse.editors.components.Navigator;

public class AppManager extends Manager {
	
	public AppManager(DataMap d, RedbackConfigEditor e, Composite parent, int style) {
		super(d, e, parent, style);
		createUI();
	}
	
	public void createUI() {
		super.createUI();
		sashForm.setWeights(new int[] {1, 2});
	}
	
	protected Navigator getNavigator() {
		return new AppTree(_data, this, sashForm, SWT.PUSH);
	}

	protected Form getForm(String type, String name) {
		if(type.equals("root")) {
			return new AppHeaderForm(_data, this, sashForm, SWT.PUSH);
		} else {
			return null;
		}
	}

	public String createNode(String type, String name) {
		return null;
	}

	public void deleteNode(String type, String name) {

	}

}
