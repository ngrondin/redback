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

import io.redback.eclipse.editors.components.ItemData;
import io.redback.eclipse.editors.components.Manager;
import io.redback.eclipse.editors.components.Navigator;

public class ObjectTree extends Navigator 
{
	public ObjectTree(DataMap d, Manager m, Composite parent, int style) 
	{
		super(d, m, parent, style);
	}
	
	protected void createUI() {
		setLayout(new FillLayout());

		final Tree tree = new Tree (this, SWT.PUSH);
		tree.addSelectionListener(this);
		tree.addMenuDetectListener(this);

		TreeItem generalNode = new TreeItem (tree, 0);
		generalNode.setText ("General");
		generalNode.setData(new ItemData("select", "general", null));

		TreeItem attributeNode = new TreeItem (tree, 1);
		attributeNode.setText ("Attributes");
		attributeNode.setData(new ItemData("select", "attributegroup", null));

		DataList list = data.getList("attributes");
		for(int i = 0; i < list.size(); i++) {
			DataMap	attCfg = list.getObject(i);
			TreeItem attNode = new TreeItem(attributeNode, 0);
			attNode.setData(new ItemData("select", "attribute", attCfg.getString("name")));
			attNode.setText(attCfg.getString("name"));	
		}
		
		TreeItem methodNode = new TreeItem (tree, 2);
		methodNode.setText ("Methods");
		methodNode.setData(new ItemData("select", "scriptgroup", null));

		if(data.containsKey("scripts")) {
			Iterator<String> it = data.getObject("scripts").keySet().iterator();
			while(it.hasNext()) {
				String key = it.next();
				TreeItem attNode = new TreeItem(methodNode, 0);
				attNode.setData(new ItemData("select", "script", key));
				attNode.setText(key);	
			}
		}		
	}

	protected void createContextMenu(Menu menu, String type, String name) {
		if(type.equals("attributegroup")) {
			MenuItem item = new MenuItem(menu, SWT.PUSH);
		    item.setText("Create...");
		    item.setData(new ItemData("create", "attribute", null));
		} else if(type.equals("attribute")) {
			MenuItem item = new MenuItem(menu, SWT.PUSH);
		    item.setText("Delete " + name);
		    item.setData(new ItemData("delete", "attribute", name));
		} else if(type.equals("scriptgroup")) {
			MenuItem item = new MenuItem(menu, SWT.PUSH);
		    item.setText("Create onSave");
		    item.setData(new ItemData("create", "script", "onSave"));
			item = new MenuItem(menu, SWT.PUSH);
		    item.setText("Create...");
		    item.setData(new ItemData("create", "script", null));
		} else if(type.equals("script")) {
			MenuItem item = new MenuItem(menu, SWT.PUSH);
		    item.setText("Delete " + name);
		    item.setData(new ItemData("delete", "script", name));
		}
		
	}

}
