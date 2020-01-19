package io.redback.eclipse.editors.components;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

import com.nic.firebus.utils.DataMap;


public abstract class Manager extends Composite  {

	protected DataMap data; 
	protected SashForm sashForm;
	protected Navigator navigator;
	protected Form form;

	public Manager(DataMap d, Composite parent, int style) {
		super(parent, style);
		data = d;
		setLayout(new FillLayout());
		getHorizontalBar().setVisible(false);

		sashForm = new SashForm(this, SWT.HORIZONTAL);
	    navigator = getNavigator();
	    navigator.createUI();
	    form = new EmptyForm(sashForm, SWT.PUSH);
	}
	
	protected abstract Navigator getNavigator();
	
	protected abstract Form getForm(String type, String name);

	public abstract void createNode(String type, String name);
	
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
	
}
