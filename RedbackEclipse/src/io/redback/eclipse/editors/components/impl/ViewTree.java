package io.redback.eclipse.editors.components.impl;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import com.nic.firebus.utils.DataList;
import com.nic.firebus.utils.DataMap;

import io.redback.eclipse.editors.components.Manager;
import io.redback.eclipse.editors.components.Navigator;
import io.redback.eclipse.editors.components.NavigatorAction;

public class ViewTree extends Navigator {

	public ViewTree(DataMap d, Manager m, Composite p, int s) {
		super(d, m, p, s);
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
		
		createRecursiveUI(data, "", tree, rootNode);
		
	}
	
	protected void createRecursiveUI(DataMap parentData, String hierarchy, Tree tree, TreeItem parentItem) {
		if(parentData.containsKey("content")) {
			DataList list = parentData.getList("content");
			for(int i = 0; i < list.size(); i++) {
				DataMap	nodeData = list.getObject(i);
				TreeItem treeNode = new TreeItem(parentItem, 0); 
				String type = nodeData.getString("type");
				String hierarchicalName = (hierarchy.length() > 0 ? hierarchy + "." : "") + "content." + i;
				String displayName = type;
				if(type.equals("view"))
					displayName += " [" + nodeData.getString("name") + "]";
				else if(type.equals("tab"))
					displayName += " [" + nodeData.getString("label") + "]";

				treeNode.setData(new NavigatorAction("select", type, hierarchicalName));
				treeNode.setText(displayName);	
				if(isSelected(type, hierarchicalName))
					tree.setSelection(treeNode);
				if(nodeData.containsKey("content"))
					createRecursiveUI(nodeData, hierarchicalName, tree, treeNode);
			}			
		}
	}

	protected void createContextMenu(Menu menu, String type, String name) {
		MenuItem item = null;
		DataMap menuData = data.getObject(name);
		if(menuData.containsKey("content")) {
			item = new MenuItem(menu, SWT.PUSH);
		    item.setText("Create HSection");
		    item.setData(new NavigatorAction("create", "hsection", name));
		    
			item = new MenuItem(menu, SWT.PUSH);
		    item.setText("Create VSection");
		    item.setData(new NavigatorAction("create", "vsection", name));

			item = new MenuItem(menu, SWT.PUSH);
		    item.setText("Create Tab Section");
		    item.setData(new NavigatorAction("create", "tabsection", name));
		    
			item = new MenuItem(menu, SWT.PUSH);
		    item.setText("Create Tab");
		    item.setData(new NavigatorAction("create", "tab", name));

		    new MenuItem(menu, SWT.SEPARATOR);
		    
			item = new MenuItem(menu, SWT.PUSH);
		    item.setText("Create Data Set");
		    item.setData(new NavigatorAction("create", "dataset", name));

			item = new MenuItem(menu, SWT.PUSH);
		    item.setText("Create Process Set");
		    item.setData(new NavigatorAction("create", "processset", name));

			item = new MenuItem(menu, SWT.PUSH);
		    item.setText("Create List");
		    item.setData(new NavigatorAction("create", "list", name));

			item = new MenuItem(menu, SWT.PUSH);
		    item.setText("Create Search");
		    item.setData(new NavigatorAction("create", "search", name));

			item = new MenuItem(menu, SWT.PUSH);
		    item.setText("Create Map");
		    item.setData(new NavigatorAction("create", "map", name));

		    new MenuItem(menu, SWT.SEPARATOR);
		    
			item = new MenuItem(menu, SWT.PUSH);
		    item.setText("Create Action Group");
		    item.setData(new NavigatorAction("create", "actiongroup", name));

			item = new MenuItem(menu, SWT.PUSH);
		    item.setText("Create Round Button");
		    item.setData(new NavigatorAction("create", "roundbutton", name));

			item = new MenuItem(menu, SWT.PUSH);
		    item.setText("Create Button");
		    item.setData(new NavigatorAction("create", "button", name));

			item = new MenuItem(menu, SWT.PUSH);
		    item.setText("Create Process Action Button");
		    item.setData(new NavigatorAction("create", "processactionbutton", name));

		    new MenuItem(menu, SWT.SEPARATOR);
		    
			item = new MenuItem(menu, SWT.PUSH);
		    item.setText("Create Text Input");
		    item.setData(new NavigatorAction("create", "input", name));

			item = new MenuItem(menu, SWT.PUSH);
		    item.setText("Create Textarea Input");
		    item.setData(new NavigatorAction("create", "textarea", name));

			item = new MenuItem(menu, SWT.PUSH);
		    item.setText("Create Date Input");
		    item.setData(new NavigatorAction("create", "datepicker", name));

			item = new MenuItem(menu, SWT.PUSH);
		    item.setText("Create Related Input");
		    item.setData(new NavigatorAction("create", "relatedinput", name));

			item = new MenuItem(menu, SWT.PUSH);
		    item.setText("Create Duration Input");
		    item.setData(new NavigatorAction("create", "durationinput", name));

			item = new MenuItem(menu, SWT.PUSH);
		    item.setText("Create Switch");
		    item.setData(new NavigatorAction("create", "switch", name));

		    new MenuItem(menu, SWT.SEPARATOR);
		    
			item = new MenuItem(menu, SWT.PUSH);
		    item.setText("Delete");
		    item.setData(new NavigatorAction("delete", type, name));

		}
	}

}
