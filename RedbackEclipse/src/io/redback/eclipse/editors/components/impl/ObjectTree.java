package io.redback.eclipse.editors.components.impl;

import java.util.Iterator;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import io.firebus.data.DataList;
import io.firebus.data.DataMap;

import io.redback.eclipse.editors.components.NavigatorAction;
import io.redback.eclipse.editors.components.Manager;
import io.redback.eclipse.editors.components.Navigator;

public class ObjectTree extends Navigator 
{
	public ObjectTree(DataMap d, Manager m, Composite parent, int style) 
	{
		super(d, m, parent, style);
		createUI();
	}
	
	protected void createUI() {
		setLayout(new FillLayout());

		final Tree tree = new Tree (this, SWT.PUSH);
		tree.addSelectionListener(this);
		tree.addMenuDetectListener(this);

		TreeItem rootNode = new TreeItem (tree, 0);
		rootNode.setText (_data.getString("name"));
		rootNode.setData(new NavigatorAction("select", "root", null));

		TreeItem attributeNode = new TreeItem (rootNode, 1);
		attributeNode.setText ("Attributes");
		attributeNode.setData(new NavigatorAction("select", "attributegroup", null));

		DataList list = _data.getList("attributes");
		for(int i = 0; i < list.size(); i++) {
			DataMap	attribute = list.getObject(i);
			TreeItem attNode = new TreeItem(attributeNode, 0);
			String atttributeName = attribute.getString("name");
			attNode.setData(new NavigatorAction("select", "attribute", atttributeName));
			attNode.setText(attribute.getString("name"));	
			if(isSelected("attribute", attribute.getString("name")))
				tree.setSelection(attNode);
			if(attribute.containsKey("scripts")) {
				Iterator<String> it = attribute.getObject("scripts").keySet().iterator();
				while(it.hasNext()) {
					String key = it.next();
					String scriptName = atttributeName + "." + key;
					TreeItem scriptNode = new TreeItem(attNode, 0);
					scriptNode.setData(new NavigatorAction("select", "attributescript", scriptName));
					scriptNode.setText(key);	
					if(isSelected("attributescript", scriptName))
						tree.setSelection(scriptNode);
				}
			}		
		}
		
		TreeItem methodNode = new TreeItem (rootNode, 2);
		methodNode.setText ("Methods");
		methodNode.setData(new NavigatorAction("select", "scriptgroup", null));

		if(_data.containsKey("scripts")) {
			Iterator<String> it = _data.getObject("scripts").keySet().iterator();
			while(it.hasNext()) {
				String key = it.next();
				TreeItem scriptNode = new TreeItem(methodNode, 0);
				scriptNode.setData(new NavigatorAction("select", "script", key));
				scriptNode.setText(key);	
				if(isSelected("script", key))
					tree.setSelection(scriptNode);
			}
		}		
	}

	protected void createContextMenu(Menu menu, String type, String name) {
		if(type.equals("attributegroup")) {
			MenuItem item = new MenuItem(menu, SWT.PUSH);
		    item.setText("Create...");
		    item.setData(new NavigatorAction("create", "attribute", null));
		} else if(type.equals("attribute")) {
			MenuItem item = new MenuItem(menu, SWT.PUSH);
		    item.setText("Create onupdate");
		    item.setData(new NavigatorAction("create", "attributescript", name + ".onupdate"));
			item = new MenuItem(menu, SWT.PUSH);
		    item.setText("Create afterupdate");
		    item.setData(new NavigatorAction("create", "attributescript", name + ".afterupdate"));
			item = new MenuItem(menu, SWT.PUSH);
		    item.setText("Delete " + name);
		    item.setData(new NavigatorAction("delete", "attribute", name));
		} else if(type.equals("attributescript")) {
			MenuItem item = new MenuItem(menu, SWT.PUSH);
		    item.setText("Delete " + name);
		    item.setData(new NavigatorAction("delete", "attributescript", name));
		} else if(type.equals("scriptgroup")) {
			MenuItem item = new MenuItem(menu, SWT.PUSH);
		    item.setText("Create onsave");
		    item.setData(new NavigatorAction("create", "script", "onsave"));
			item = new MenuItem(menu, SWT.PUSH);
		    item.setText("Create aftersave");
		    item.setData(new NavigatorAction("create", "script", "aftersave"));
			item = new MenuItem(menu, SWT.PUSH);
		    item.setText("Create...");
		    item.setData(new NavigatorAction("create", "script", null));
		} else if(type.equals("script")) {
			MenuItem item = new MenuItem(menu, SWT.PUSH);
		    item.setText("Delete " + name);
		    item.setData(new NavigatorAction("delete", "script", name));
		}
		
	}

}
