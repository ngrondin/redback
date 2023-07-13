package io.redback.eclipse.editors.components.impl;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import io.firebus.data.DataList;
import io.firebus.data.DataMap;

import io.redback.eclipse.editors.components.Manager;
import io.redback.eclipse.editors.components.Navigator;
import io.redback.eclipse.editors.components.NavigatorAction;

public class PackTree extends Navigator {

	public PackTree(DataMap d, Manager m, Composite p, int s) {
		super(d, m, p, s);
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
		
		if(_data.containsKey("queries")) {
			DataList list = _data.getList("queries");
			for(int i = 0; i < list.size(); i++) {
				DataMap query = list.getObject(i);
				String objectName = query.getString("object");
				TreeItem queryNode = new TreeItem (rootNode, 0);
				queryNode.setText (objectName);
				queryNode.setData(new NavigatorAction("select", "query", objectName));
				if(isSelected("query", objectName))
					tree.setSelection(queryNode);
			}
		}
	}
	
	protected void createContextMenu(Menu menu, String type, String name) {
		if(type.equals("root") && !_data.containsKey("script")) {
			MenuItem item = new MenuItem(menu, SWT.PUSH);
		    item.setText("Create query");
		    item.setData(new NavigatorAction("create", "query", null));
		} else if(type.equals("query")) {
			MenuItem item = new MenuItem(menu, SWT.PUSH);
		    item.setText("Delete " + name);
		    item.setData(new NavigatorAction("delete", "query", name));
		}
	}

}
