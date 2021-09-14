package io.redback.eclipse.editors.components;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

import io.firebus.data.DataMap;

import io.redback.eclipse.editors.RedbackConfigEditor;


public abstract class Manager extends Composite  {

	protected DataMap _data;
	protected RedbackConfigEditor editorPart;
	protected SashForm sashForm;
	protected Navigator navigator;
	protected Form form;
	protected boolean changed;

	public Manager(DataMap d, RedbackConfigEditor e, Composite parent, int style) {
		super(parent, style);
		_data = d;
		editorPart = e;
		changed = false;
	}
	
	protected void createUI() {
		setLayout(new FillLayout());
		getHorizontalBar().setVisible(false);
		sashForm = new SashForm(this, SWT.HORIZONTAL);
	    navigator = getNavigator();
	    form = new EmptyForm(sashForm, SWT.PUSH);
	}
	
	public void refresh() {
		if(navigator != null)
			navigator.refresh();
	}
	
	protected abstract Navigator getNavigator();
	
	protected abstract Form getForm(String type, String name);

	public abstract String createNode(String type, String name);
	
	public abstract void deleteNode(String type, String name);


	public void nodeSelected(String type, String name) {
		int[] weights = sashForm.getWeights();
		
		if(form != null) {
			form.dispose();
			form = null;
		}
		
		if(type != null) {
			form = getForm(type, name);
		}
		
		if(form == null) {
		    form = new EmptyForm(sashForm, SWT.PUSH);
		}

		sashForm.setWeights(weights);
		layout(true, true);		
	}
	
	public void setDataChanged(boolean c) {
		changed = c;
		editorPart.setDirty();
	}
	
	public boolean isDataChanged() {
		return changed;
	}
	
}
