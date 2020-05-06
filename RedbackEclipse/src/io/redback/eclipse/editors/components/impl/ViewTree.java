package io.redback.eclipse.editors.components.impl;

import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import io.firebus.utils.DataList;
import io.firebus.utils.DataMap;

import io.redback.eclipse.editors.components.Manager;
import io.redback.eclipse.editors.components.Navigator;
import io.redback.eclipse.editors.components.NavigatorAction;

public class ViewTree extends Navigator implements DragSourceListener, DropTargetListener {

	protected String dragNodeName;
	
	public ViewTree(DataMap d, Manager m, Composite p, int s) {
		super(d, m, p, s);
		createUI();
	}

	protected void createUI() {
		setLayout(new FillLayout());

		final Tree tree = new Tree (this, SWT.PUSH);
		tree.addSelectionListener(this);
		tree.addMenuDetectListener(this);
		
		Transfer[] types = new Transfer[] { TextTransfer.getInstance() };
	    int operations = DND.DROP_MOVE;

	    final DragSource source = new DragSource(tree, operations);
	    source.setTransfer(types);
	    source.addDragListener(this);

	    DropTarget target = new DropTarget(tree, operations);
	    target.setTransfer(types);
	    target.addDropListener(this);

		TreeItem rootNode = new TreeItem (tree, 0);
		rootNode.setText (_data.getString("name"));
		rootNode.setData(new NavigatorAction("select", "root", null));
		
		createRecursiveUI(_data, "", tree, rootNode);
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
 		DataMap menuData = name != null ? _data.getObject(name) : _data;
		if(menuData.containsKey("content")) {
			item = new MenuItem(menu, SWT.PUSH);
		    item.setText("Create HSection");
		    item.setData(new NavigatorAction("create", "hsection", name));
		    
			item = new MenuItem(menu, SWT.PUSH);
		    item.setText("Create VSection");
		    item.setData(new NavigatorAction("create", "vsection", name));

			item = new MenuItem(menu, SWT.PUSH);
		    item.setText("Create View");
		    item.setData(new NavigatorAction("create", "view", name));

		    item = new MenuItem(menu, SWT.PUSH);
		    item.setText("Create Tab Section");
		    item.setData(new NavigatorAction("create", "tabsection", name));
		    
			item = new MenuItem(menu, SWT.PUSH);
		    item.setText("Create Tab");
		    item.setData(new NavigatorAction("create", "tab", name));

			item = new MenuItem(menu, SWT.PUSH);
		    item.setText("Create Spacer");
		    item.setData(new NavigatorAction("create", "spacer", name));

		    new MenuItem(menu, SWT.SEPARATOR);
		    
			item = new MenuItem(menu, SWT.PUSH);
		    item.setText("Create Data Set");
		    item.setData(new NavigatorAction("create", "dataset", name));

			item = new MenuItem(menu, SWT.PUSH);
		    item.setText("Create File Set");
		    item.setData(new NavigatorAction("create", "fileset", name));

		    new MenuItem(menu, SWT.SEPARATOR);
		    
			item = new MenuItem(menu, SWT.PUSH);
		    item.setText("Create List3");
		    item.setData(new NavigatorAction("create", "list3", name));

			item = new MenuItem(menu, SWT.PUSH);
		    item.setText("Create Search");
		    item.setData(new NavigatorAction("create", "search", name));

			item = new MenuItem(menu, SWT.PUSH);
		    item.setText("Create Map");
		    item.setData(new NavigatorAction("create", "map", name));

			item = new MenuItem(menu, SWT.PUSH);
		    item.setText("Create Log");
		    item.setData(new NavigatorAction("create", "log", name));

			item = new MenuItem(menu, SWT.PUSH);
		    item.setText("Create File List");
		    item.setData(new NavigatorAction("create", "filelist", name));

			item = new MenuItem(menu, SWT.PUSH);
		    item.setText("Create Dynamic Form");
		    item.setData(new NavigatorAction("create", "dynamicform", name));

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
		    item.setData(new NavigatorAction("create", "processactionsbutton", name));

			item = new MenuItem(menu, SWT.PUSH);
		    item.setText("Create Link");
		    item.setData(new NavigatorAction("create", "link", name));

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

			item = new MenuItem(menu, SWT.PUSH);
		    item.setText("Create File Drop");
		    item.setData(new NavigatorAction("create", "filedrop", name));

		    new MenuItem(menu, SWT.SEPARATOR);
		}
		
		if(name != null) {
			String parentContent = name.substring(0, name.lastIndexOf("."));
			String nextSelectName = null;
			String nextSelectType = null;
			if(parentContent.indexOf(".") > -1) {
				nextSelectName = parentContent.substring(0, parentContent.lastIndexOf("."));
				nextSelectType = _data.getObject(nextSelectName).getString("type");
			}
			
			item = new MenuItem(menu, SWT.PUSH);
		    item.setText("Delete");
		    item.setData(new NavigatorAction("delete", type, name, nextSelectType, nextSelectName));
		}

	}

	public void dragFinished(DragSourceEvent event) {
		refresh();			
		
	}

	public void dragSetData(DragSourceEvent event) {
		event.data = "a";
	}

	public void dragStart(DragSourceEvent event) {
		DragSource source = (DragSource)event.getSource();
		Tree tree = (Tree)source.getControl();
		TreeItem treeItem = tree.getSelection()[0];
		NavigatorAction action = (NavigatorAction)treeItem.getData();
		dragNodeName = action.name;
	}

	public void dragEnter(DropTargetEvent event) {
		
	}

	public void dragLeave(DropTargetEvent event) {
		
	}

	public void dragOperationChanged(DropTargetEvent event) {
		
	}

	public void dragOver(DropTargetEvent event) {
		
	}

	public void drop(DropTargetEvent event) {
		
	}

	public void dropAccept(DropTargetEvent event) {
		TreeItem treeItem = (TreeItem)event.item;
		NavigatorAction action = (NavigatorAction)treeItem.getData();
		String targetNodeName = action.name;
		((ViewManager)manager).moveNode(dragNodeName, targetNodeName);
	}

}
