package io.redback.eclipse.editors.components.impl;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
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

		TreeItem methodNode = new TreeItem (rootNode, 1);
		methodNode.setText ("Method");
		methodNode.setData(new NavigatorAction("select", "method", null));

		TreeItem urlNode = new TreeItem (rootNode, 1);
		urlNode.setText ("URL");
		urlNode.setData(new NavigatorAction("select", "url", null));

		TreeItem bodyNode = new TreeItem (rootNode, 1);
		bodyNode.setText ("Body");
		bodyNode.setData(new NavigatorAction("select", "body", null));

		TreeItem responseNode = new TreeItem (rootNode, 1);
		responseNode.setText ("response");
		responseNode.setData(new NavigatorAction("select", "response", null));

	}

	protected void createContextMenu(Menu menu, String type, String name) {
		
	}

}
