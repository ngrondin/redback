package io.redback.eclipse.editors.components.impl;

import java.util.Iterator;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import com.nic.firebus.utils.DataList;
import com.nic.firebus.utils.DataMap;

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
		rootNode.setText (data.getString("name"));
		rootNode.setData(new NavigatorAction("select", "root", null));

		TreeItem attributeNode = new TreeItem (rootNode, 1);
		attributeNode.setText ("Attributes");
		attributeNode.setData(new NavigatorAction("select", "attributegroup", null));

		DataList list = data.getList("attributes");
		for(int i = 0; i < list.size(); i++) {
			DataMap	attCfg = list.getObject(i);
			TreeItem attNode = new TreeItem(attributeNode, 0);
			attNode.setData(new NavigatorAction("select", "attribute", attCfg.getString("name")));
			attNode.setText(attCfg.getString("name"));	
			if(isSelected("attribute", attCfg.getString("name")))
				tree.setSelection(attNode);
		}
		
		TreeItem methodNode = new TreeItem (rootNode, 2);
		methodNode.setText ("Methods");
		methodNode.setData(new NavigatorAction("select", "scriptgroup", null));

		if(data.containsKey("scripts")) {
			Iterator<String> it = data.getObject("scripts").keySet().iterator();
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
		    item.setText("Delete " + name);
		    item.setData(new NavigatorAction("delete", "attribute", name));
		} else if(type.equals("scriptgroup")) {
			MenuItem item = new MenuItem(menu, SWT.PUSH);
		    item.setText("Create onSave");
		    item.setData(new NavigatorAction("create", "script", "onSave"));
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
