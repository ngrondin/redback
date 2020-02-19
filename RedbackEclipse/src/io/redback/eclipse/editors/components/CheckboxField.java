package io.redback.eclipse.editors.components;


import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import io.firebus.utils.DataMap;

public class CheckboxField extends Composite implements SelectionListener {
	
	protected DataMap map;
	protected String attribute;
	protected Form form;
	protected Button checkbox;
	protected Object oldValue;
	protected boolean asOneAndZero;
	
	public CheckboxField(DataMap m, String a, String l, Form f, int s) {
		super(f, s);
		map = m;
		attribute = a;
		form = f;
		setLayout(new RowLayout(SWT.HORIZONTAL));
		Label label = new Label(this, SWT.NONE);
		label.setText(l);
		label.setLayoutData(new RowData(170, 24));
		checkbox = new Button(this, SWT.CHECK);
		if(map != null && map.get(attribute) != null) {
			checkbox.setSelection(map.getBoolean(attribute));
			oldValue = map.getBoolean(attribute);
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
		if(map != null) {
			map.put(attribute, newValue);
		}
		form.onFieldUpdate(attribute, oldValue, newValue);
		form.setDataChanged(true);
		oldValue = newValue;
	}


}
