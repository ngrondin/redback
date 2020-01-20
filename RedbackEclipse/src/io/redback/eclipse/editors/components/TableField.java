package io.redback.eclipse.editors.components;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

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

public class TableField extends Composite implements SelectionListener, ModifyListener, FocusListener, MouseListener {

	protected DataMap data;
	protected String attribute;
	protected String label;
	protected String[][] columns;
	protected Map<String, Integer> colMap;
	protected Form form;
	protected Table table;
	protected Button addBut;
	protected Button delBut;
	protected TableEditor editor;
	
	public TableField(DataMap d, String a, String l, String[][] c, Form f, int s) {
		super(f, s);
		data = d;
		attribute = a;
		label = l;
		form = f;
		columns = c;
		colMap = new HashMap<String, Integer>();
		for(int i = 0; i < columns.length; i++)
			colMap.put(columns[i][0], i);
		createUI();
	}
	
	public void createUI() {
		setLayout(new RowLayout(SWT.VERTICAL));
		Label lbl = new Label(this, SWT.NONE);
		lbl.setText(label);
		Composite tools = new Composite(this, SWT.NONE);
		tools.setLayout(new RowLayout(SWT.HORIZONTAL));
		addBut = new Button(tools, SWT.NONE);
		addBut.setText("Add");
		addBut.addSelectionListener(this);
		delBut = new Button(tools, SWT.NONE);
		delBut.setText("Delete");
		delBut.addSelectionListener(this);
		
		table = new Table(this, SWT.CHECK | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		table.setLayoutData(new RowData(500, 150));
		table.setHeaderVisible(true);
		
		for(int i = 0; i < columns.length; i++) {
			TableColumn col = new TableColumn(table, SWT.NULL);
			col.setText(columns[i][1]);
			col.setData(columns[i][0]);
			col.pack();
			col.setWidth(200);
			
		}

		editor = new TableEditor(table);
		editor.horizontalAlignment = SWT.LEFT;
		editor.grabHorizontal = true;
		editor.minimumWidth = 50;
	    
		table.addMouseListener(this);
		
		refreshTableRows();
	}
	
	public void refreshTableRows() {
		DataList list = data.getList(attribute);
		for(int i = 0; i < list.size(); i++) {
			TableItem item = new TableItem(table, SWT.NULL);
			for(int j = 0; j < columns.length; j++) {
				item.setText(j, list.getObject(i).getString(columns[j][0]));
			}
			item.setData(i);
		}			
	}

	
	public void startEditing(TableItem item) {
		closeEditor();
		int colIndex = item.getData("key") != null ? 1 : 0;
        Text newEditor = new Text(table, SWT.NONE);
        newEditor.setText(item.getText(colIndex));
        newEditor.addModifyListener(this);
        newEditor.addFocusListener(this);
        newEditor.selectAll();
        newEditor.setFocus();
        editor.setEditor(newEditor, item, colIndex);			
	}
	
	public void closeEditor() {
		Control oldEditor = editor.getEditor();
        if (oldEditor != null)
          oldEditor.dispose();
	}

	public void onFieldUpdate(String attribute, Object oldValue, Object newValue) {
		
	}

	public void widgetDefaultSelected(SelectionEvent event) {
		
	}

	public void widgetSelected(SelectionEvent event) {
		if(event.getSource() instanceof Button) {
			if(event.getSource() == addBut) {
				DataMap newData = new DataMap();
				data.getList(attribute).add(newData);
				refreshTableRows();
				//startEditing(item);
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
		form.onFieldUpdate(attribute, null, null);
		form.setDataChanged(true);
	}

	public void focusGained(FocusEvent wvent) {
		
	}

	public void focusLost(FocusEvent event) {
        Text text = (Text)editor.getEditor();
        TableItem item = (TableItem)editor.getItem();
        int index = (Integer)item.getData();
        /*if(key == null) {
        	String newKey = text.getText();
            item.setText(0, newKey);
            item.setData("key", newKey);
            data.put(newKey, null);
            closeEditor();
        } else {
            item.setText(1, text.getText());
            data.put(key, text.getText());
            closeEditor();
        }*/
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
