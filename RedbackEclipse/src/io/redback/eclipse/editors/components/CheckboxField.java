package io.redback.eclipse.editors.components;


import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import io.firebus.data.DataMap;

public class CheckboxField extends Field implements SelectionListener {
	
	protected Button checkbox;
	protected Object oldValue;
	protected boolean asOneAndZero;
	
	public CheckboxField(DataMap d, String a, String l, Composite p, int s) {
		super(d, a, l, p, s);
		createUI();
	}

	public void createUI() {
		setLayout(new RowLayout(SWT.HORIZONTAL));
		Label lbl = new Label(this, SWT.NONE);
		lbl.setText(label);
		lbl.setLayoutData(new RowData(170, 24));
		checkbox = new Button(this, SWT.CHECK);
		if(_data != null && _data.get(attribute) != null) {
			checkbox.setSelection(_data.getBoolean(attribute));
			oldValue = _data.getBoolean(attribute);
		}
		checkbox.addSelectionListener(this);
	}
	
	public void setChecked(boolean checked) {
		checkbox.setSelection(checked);
	}
	
	public void setAsOneAndZero(boolean b) {
		asOneAndZero = b;
	}

	public void widgetDefaultSelected(SelectionEvent event) {
		
	}

	public void widgetSelected(SelectionEvent event) {
		Object newValue = checkbox.getSelection();
		if(asOneAndZero) {
			if((Boolean)newValue == true)
				newValue = 1;
			else
				newValue = 0;
		}
		if(_data != null) {
			_data.put(attribute, newValue);
		}
		form.onFieldUpdate(attribute, oldValue, newValue);
		form.setDataChanged(true);
		oldValue = newValue;
	}



}
