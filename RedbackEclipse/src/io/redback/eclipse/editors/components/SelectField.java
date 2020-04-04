package io.redback.eclipse.editors.components;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import io.firebus.utils.DataMap;

public class SelectField extends Field implements SelectionListener {
	
	protected Combo combo;
	protected Object oldValue;
	protected String[] options;
	protected String[] optionLabels;
	
	public SelectField(DataMap d, String a, String l, String[] ov, String[] ol, Composite p, int s) {
		super(d, a, l, p, s);
		options = ov;
		optionLabels = ol;
		createUI();
	}
	
	public void createUI() {
		setLayout(new RowLayout(SWT.HORIZONTAL));
		Label lbl = new Label(this, SWT.NONE);
		lbl.setText(label);
		lbl.setLayoutData(new RowData(170, 24));
		combo = new Combo(this, SWT.NONE);
		combo.setLayoutData(new RowData(170, 24));
		combo.setItems(optionLabels);
		combo.addSelectionListener(this);
		if(data != null && data.getString(attribute) != null) {
			setValue(data.getString(attribute));
			oldValue = data.getString(attribute);
		}
	}


	public String getSelectedValue() {
		String val = combo.getText();
		for(int i = 0; i < options.length; i++)
			if(val.equals(optionLabels[i]))
				return options[i];
		return null;
	}
	
	public void setValue(String val) {
		for(int i = 0; i < options.length; i++)
			if(val.equals(options[i]))
				combo.select(i);
	}

	public void widgetDefaultSelected(SelectionEvent event) {
		
	}

	public void widgetSelected(SelectionEvent event) {
		Object newValue = null;
		String newString = getSelectedValue();
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
		if(data != null) {
			if(newValue == null && data.get(attribute) != null)
				data.remove(attribute);
			else
				data.put(attribute, newValue);
		}
		form.onFieldUpdate(attribute, oldValue, newValue);
		form.setDataChanged(true);
		oldValue = newValue;		
	}

}
