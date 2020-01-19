package io.redback.eclipse.editors.components;

import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MenuListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.Widget;

import com.nic.firebus.utils.DataMap;

public abstract class Navigator extends Composite implements MenuDetectListener, SelectionListener, MenuListener {

	protected DataMap data;
	protected Manager manager;

	public Navigator(DataMap d, Manager m, Composite p, int s) {
		super(p, s);
		data = d;
		manager = m;
	}

	private Widget getEndWidget(Widget source) {
		Widget item = null;
		if(source instanceof Tree) 
			item = ((Tree)source).getSelection()[0];
		else if(source instanceof Table) 
			item = ((Table)source).getSelection()[0];
		else
			item = source;
		return item;		
	}
	
	public void refresh() {
		Control[] children = getChildren();
		for(int i = 0; i < children.length; i++) {
			children[i].dispose();
		}
		createUI();
		layout(true, true);
	}

	// Abstract methods
	
	protected abstract void createUI();
	
	protected abstract void createContextMenu(Menu menu, String type, String name);
	
	//Event Handlers
	
	public void menuDetected(MenuDetectEvent event) {
		Control source = (Control)event.getSource();
		Widget item = getEndWidget(source);
		ItemData data = (ItemData)item.getData();
		Menu menu = new Menu(source);
		createContextMenu(menu, data.type, data.name);
		for(int i = 0; i < menu.getItemCount(); i++) {
			MenuItem menuItem = menu.getItem(i);
			if(menuItem.getData() != null) {
				menuItem.addSelectionListener(this);
			}
		}
		source.setMenu(menu);
	}
	
	public void menuHidden(MenuEvent event) {
		
	}

	public void menuShown(MenuEvent event) {
		
	}

	public void widgetDefaultSelected(SelectionEvent event) {
		
	}

	public void widgetSelected(SelectionEvent event) {
		Widget source = (Widget)event.getSource();
		Widget item = getEndWidget(source);
		ItemData data = (ItemData)item.getData();
		if(data.action.equals("select")) {
			manager.nodeSelected(data.type, data.name);
		} else if(data.action.equals("create")) {
			manager.createNode(data.type, data.name);
		} else if(data.action.equals("delete")) {
			manager.deleteNode(data.type, data.name);
		}

	}
	
}

