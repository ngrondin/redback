package io.redback.eclipse.editors.components.impl;

import java.util.Iterator;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import io.firebus.data.DataMap;
import io.redback.eclipse.editors.components.Manager;
import io.redback.eclipse.editors.components.Navigator;
import io.redback.eclipse.editors.components.NavigatorAction;

public class IntegrationNavigator extends Navigator 
{
	public IntegrationNavigator(DataMap d, Manager m, Composite parent, int style) 
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

		TreeItem headerNode = new TreeItem (rootNode, 1);
		headerNode.setText ("Header");
		headerNode.setData(new NavigatorAction("select", "header", null));

		TreeItem tokenHeaderNode = new TreeItem (rootNode, 1);
		tokenHeaderNode.setText ("Token Header");
		tokenHeaderNode.setData(new NavigatorAction("select", "tokenheader", null));

		TreeItem methodNode = new TreeItem (rootNode, 1);
		methodNode.setText ("Method");
		methodNode.setData(new NavigatorAction("select", "method", null));

		TreeItem urlNode = new TreeItem (rootNode, 1);
		urlNode.setText ("URL");
		urlNode.setData(new NavigatorAction("select", "url", null));

		TreeItem getDomainNode = new TreeItem (rootNode, 1);
		getDomainNode.setText ("Get Domain");
		getDomainNode.setData(new NavigatorAction("select", "getdomain", null));
		
		TreeItem bodyNode = new TreeItem (rootNode, 1);
		bodyNode.setText ("Body");
		bodyNode.setData(new NavigatorAction("select", "body", null));

		TreeItem responseNode = new TreeItem (rootNode, 1);
		responseNode.setText ("response");
		responseNode.setData(new NavigatorAction("select", "response", null));
		
		TreeItem functionsNode = new TreeItem (rootNode, 1);
		functionsNode.setText ("functions");
		functionsNode.setData(new NavigatorAction("select", "functions", null));
		
		if(_data.containsKey("functions")) {
			Iterator<String> it = _data.getObject("functions").keySet().iterator();
			while(it.hasNext()) {
				String key = it.next();
				TreeItem functiontNode = new TreeItem(functionsNode, 0);
				functiontNode.setData(new NavigatorAction("select", "function", key));
				functiontNode.setText(key);	
				if(isSelected("function", key))
					tree.setSelection(functiontNode);
			}
		}

	}

	protected void createContextMenu(Menu menu, String type, String name) {
		if(type.equals("functions")) {
			MenuItem item = new MenuItem(menu, SWT.PUSH);
		    item.setText("Create...");
		    item.setData(new NavigatorAction("create", "functions", null));
		} else if(type.equals("function")) {
			MenuItem item = new MenuItem(menu, SWT.PUSH);
		    item.setText("Delete " + name);
		    item.setData(new NavigatorAction("delete", "function", name));
		}
	}

}
