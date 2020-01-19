package io.redback.eclipse.editors.components;


import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.nic.firebus.utils.DataMap;

public class CheckboxField extends Composite implements SelectionListener {
	
	protected DataMap map;
	protected String attribute;
	protected Form form;
	protected Button checkbox;
	protected Boolean oldValue;
	
	public CheckboxField(DataMap m, String a, String l, Form f, int s) {
		super(f, s);
		map = m;
		attribute = a;
		form = f;
		setLayout(new RowLayout(SWT.HORIZONTAL));
		Label label = new Label(this, SWT.NONE);
		label.setText(l);
		label.setSize(200, 32);
		checkbox = new Button(f, SWT.CHECK);
		if(map != null && map.get(attribute) != null) {
			checkbox.setEnabled(map.getBoolean(attribute));
			oldValue = map.getBoolean(attribute);
		}
		checkbox.addSelectionListener(this);

	}
	
	public void setChecked(boolean checked) {
		checkbox.setSelection(checked);
	}

	public void widgetDefaultSelected(SelectionEvent event) {
		
	}

	public void widgetSelected(SelectionEvent event) {
		Boolean newValue = checkbox.getSelection();
		if(map != null) {
			if(newValue == null && map.get(attribute) != null)
				map.remove(attribute);
			else
				map.put(attribute, newValue);
		}
		form.onFieldUpdate(attribute, oldValue, newValue);
		oldValue = newValue;
	}


}
