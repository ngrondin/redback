package io.redback.eclipse.editors.components;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import io.firebus.utils.DataMap;

public class TextField extends Field implements ModifyListener {
	
	protected Text text;
	protected Object oldValue;
	
	public TextField(DataMap d, String a, String l, Composite p, int s) {
		super(d, a, l, p, s);
		createUI();
	}
	
	public void createUI() {
		setLayout(new RowLayout(SWT.HORIZONTAL));
		Label lbl = new Label(this, SWT.NONE);
		lbl.setText(label);
		lbl.setLayoutData(new RowData(170, 24));
		text = new Text(this, SWT.BORDER);
		text.setLayoutData(new RowData(300, 24));
		if(_data != null && _data.getString(attribute) != null) {
			text.setText(_data.getString(attribute));
			oldValue = _data.getString(attribute);
		}
		text.addModifyListener(this);	
	}

	public void modifyText(ModifyEvent event) {
		Object newValue = null;
		String newString = text.getText();
		if(newString != null && newString.equals("")) {
			newValue = null;
		} else {
			try {
				newValue = Integer.parseInt(newString);
			} catch(Exception e1) {
				try {
					newValue = Double.parseDouble(newString);
				} catch(Exception e2) {
					newValue = newString;
				}
			}
		}
		if(_data != null) {
			if(newValue == null && _data.get(attribute) != null)
				_data.remove(attribute);
			else
				_data.put(attribute, newValue);
		}
		form.onFieldUpdate(attribute, oldValue, newValue);
		form.setDataChanged(true);
		oldValue = newValue;
	}

}
