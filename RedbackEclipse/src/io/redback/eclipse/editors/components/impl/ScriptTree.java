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

public class ScriptTree extends Navigator {

	public ScriptTree(DataMap d, Manager m, Composite p, int s) {
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
	}
	

	protected void createContextMenu(Menu menu, String type, String name) {

	}

}
