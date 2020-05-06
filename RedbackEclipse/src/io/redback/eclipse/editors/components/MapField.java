package io.redback.eclipse.editors.components;

import java.util.Iterator;

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
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
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

import io.firebus.utils.DataMap;

public class MapField extends Field implements SelectionListener, ModifyListener, FocusListener, MouseListener {

	protected Table table;
	protected TableColumn keyCol;
	protected TableColumn valueCol;
	protected Button addBut;
	protected Button delBut;
	protected TableEditor editor;
	
	public MapField(DataMap d, String a, String l, Composite p, int s) {
		super(d, a, l, p, s);
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
		
		table = new Table(this, SWT.CHECK | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.FULL_SELECTION);
		table.setLayoutData(new RowData(500, 150));
		table.setHeaderVisible(true);
		keyCol = new TableColumn(table, SWT.NULL);
		keyCol.setText("Keys");
		keyCol.pack();
		keyCol.setWidth(200);
		valueCol = new TableColumn(table, SWT.NULL);
		valueCol.setText("Values");
		valueCol.pack();
		valueCol.setWidth(300);
		
		editor = new TableEditor(table);
		editor.horizontalAlignment = SWT.LEFT;
		editor.grabHorizontal = true;
		editor.minimumWidth = 50;
	    
		table.addMouseListener(this);
		
		if(_data.getObject(attribute) != null) {
			Iterator<String> it = _data.getObject(attribute).keySet().iterator();
			while(it.hasNext()) {
				String key = it.next();
				String val = _data.getObject(attribute).getString(key);		
				TableItem item = new TableItem(table, SWT.NULL);
				item.setText(0, key);
				item.setText(1, val == null ? "" : val);
				item.setData("key", key);
			}	
		}
	}

	
	public void startEditing(TableItem item, int colIndex) {
		closeEditor();
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
				TableItem item = new TableItem(table, SWT.NULL);
				startEditing(item, 0);
			} else if(event.getSource() == delBut) {
				TableItem[] items = table.getSelection();
				for(int i = 0; i < items.length; i++) {
					String key = (String)items[i].getData("key");
					if(key != null)
						_data.getObject(attribute).remove(key);
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
        String key = (String)item.getData("key");
        if(key == null) {
        	String newKey = text.getText();
            item.setText(0, newKey);
            item.setData("key", newKey);
            if(_data.getObject(attribute) == null)
            	_data.put(attribute, new DataMap());
            _data.getObject(attribute).put(newKey, null);
            closeEditor();
        } else {
            item.setText(1, text.getText());
            _data.getObject(attribute).put(key, text.getText());
            closeEditor();
        }
	}

	public void mouseDoubleClick(MouseEvent event) {
		if(event.getSource() instanceof Table) {
			TableItem[] items = ((Table)event.getSource()).getSelection();
			if(items.length == 1) {
				Point pt = new Point(event.x, event.y);
				TableItem item = items[0];
				int colIndex = -1;
				for (int i = 0; i < table.getColumns().length; i++) {
					Rectangle rect = item.getBounds(i);
					if (rect.contains(pt)) {
						colIndex = i;
					}
				}
				if(colIndex > -1 )
					startEditing(item, colIndex);
			}
		}		
	}

	public void mouseDown(MouseEvent event) {
		
	}

	public void mouseUp(MouseEvent event) {
		
	}


}
