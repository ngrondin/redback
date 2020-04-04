package io.redback.eclipse.editors.components.impl;

import org.eclipse.swt.widgets.Composite;

import io.firebus.utils.DataList;
import io.firebus.utils.DataMap;

import io.redback.eclipse.editors.components.Form;
import io.redback.eclipse.editors.components.Manager;

public abstract class ProcessForm extends Form
{
	protected DataList nodes;
	protected String[] nodeOptions;
	protected String[] nodeOptionLabels;
	
	public ProcessForm(DataMap d, DataList n, Manager m, Composite p, int s) 
	{
		super(d, m, p, s);
		nodes = n;
		nodeOptions = new String[nodes.size()];
		nodeOptionLabels = new String[nodes.size()];
		for(int i = 0; i < nodes.size(); i++) {
			String name = nodes.getObject(i).getString("name");
			nodeOptions[i] = nodes.getObject(i).getString("id");
			nodeOptionLabels[i] = name == null ? "No Label" : name;
		}
		
	}
	
	public void onFieldUpdate(String attribute, Object oldValue, Object newValue) {
		if(attribute.equals("nextnode") || attribute.equals("truenode") || attribute.equals("falsenode"))
			manager.refresh();
	}
	
}
