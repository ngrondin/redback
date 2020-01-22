package io.redback.eclipse.editors.components;


import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import com.nic.firebus.utils.DataList;
import com.nic.firebus.utils.DataMap;

public class ListField extends Composite implements SelectionListener, ModifyListener, FocusListener, MouseListener {

	protected DataMap data;
	protected String attribute;
	protected String label;
	protected Form form;
	protected Table table;
	protected Button addBut;
	protected Button delBut;
	protected TableEditor editor;
	
	public ListField(DataMap d, String a, String l, Form f, int s) {
		super(f, s);
		data = d;
		attribute = a;
		label = l;
		form = f;
		createUI();
	}
	
	public void createUI() {
		setLayout(new RowLayout(SWT.HORIZONTAL));
		Label lbl = new Label(this, SWT.NONE);
		lbl.setText(label);
		lbl.setLayoutData(new RowData(170, 24));
		
		Composite controls = new Composite(this, SWT.NONE);
		controls.setLayout(new RowLayout(SWT.VERTICAL));
		
		Composite tools = new Composite(controls, SWT.NONE);
		tools.setLayout(new RowLayout(SWT.HORIZONTAL));
		addBut = new Button(tools, SWT.NONE);
		addBut.setText("Add");
		addBut.addSelectionListener(this);
		delBut = new Button(tools, SWT.NONE);
		delBut.setText("Delete");
		delBut.addSelectionListener(this);
		
		table = new Table(controls, SWT.CHECK | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.FULL_SELECTION);
		table.setLayoutData(new RowData(300, 150));
		table.setHeaderVisible(true);
		
		TableColumn col = new TableColumn(table, SWT.NULL);
		col.setText("Values");
		col.pack();
		col.setWidth(300);

		editor = new TableEditor(table);
		editor.horizontalAlignment = SWT.LEFT;
		editor.grabHorizontal = true;
		editor.minimumWidth = 50;
	    
		table.addMouseListener(this);
		
		refreshTableRows();
	}
	
	public void refreshTableRows() {
		table.removeAll();
		DataList list = data.getList(attribute);
		if(list != null) {
			for(int i = 0; i < list.size(); i++) {
				TableItem item = new TableItem(table, SWT.NULL);
				String colVal = list.getString(i);
				item.setText(0, (colVal != null ? colVal : ""));
				item.setData(i);
			}			
		}
	}

	
	public void startEditing(TableItem item) {
		closeEditor();
        Text newEditor = new Text(table, SWT.NONE);
        newEditor.setText(item.getText(0));
        newEditor.addModifyListener(this);
        newEditor.addFocusListener(this);
        newEditor.selectAll();
        newEditor.setFocus();
        editor.setEditor(newEditor, item, 0);
	}
	
	public void closeEditor() {
		Control oldEditor = editor.getEditor();
        if (oldEditor != null) {
        	oldEditor.dispose();
	  		form.onFieldUpdate(attribute, null, null);
	  		form.setDataChanged(true);
        }
	}

	public void onFieldUpdate(String attribute, Object oldValue, Object newValue) {
		
	}

	public void widgetDefaultSelected(SelectionEvent event) {
		
	}

	public void widgetSelected(SelectionEvent event) {
		if(event.getSource() instanceof Button) {
			if(event.getSource() == addBut) {
				DataMap newData = new DataMap();
				if(data.getList(attribute) == null)
					data.put(attribute, new DataList());
				data.getList(attribute).add(newData);
				refreshTableRows();
			} else if(event.getSource() == delBut) {
				TableItem[] items = table.getSelection();
				for(int i = 0; i < items.length; i++) {
					int index = (Integer)items[i].getData();
					data.getList(attribute).remove(index);
					items[i].dispose();
				}
				layout(true, true);
			}
		}
	}

	public void modifyText(ModifyEvent event) {
	}

	public void focusGained(FocusEvent wvent) {
		
	}

	public void focusLost(FocusEvent event) {
        Text text = (Text)editor.getEditor();
        TableItem item = (TableItem)editor.getItem();
        int rowIndex = (Integer)item.getData();
        String value = text.getText();
        DataList oldList = data.getList(attribute);
        DataList newList = new DataList();
        for(int i = 0; i < oldList.size(); i++) {
        	if(i == rowIndex) {
        		newList.add(value);
        	} else {
        		newList.add(oldList.get(i));
        	}
        }
        data.put(attribute, newList);
        item.setText(0, value);
        closeEditor();
	}

	public void mouseDoubleClick(MouseEvent event) {
		if(event.getSource() instanceof Table) {
			TableItem[] items = ((Table)event.getSource()).getSelection();
			if(items.length == 1) {
				startEditing(items[0]);
			}
		}
	}

	public void mouseDown(MouseEvent event) {
		
	}

	public void mouseUp(MouseEvent event) {
		
	}


}
