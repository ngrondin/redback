package io.redback.eclipse.editors.components;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import io.firebus.data.DataMap;

public class TextAreaField extends Field implements ModifyListener {
	
	protected Text text;
	protected String oldValue;
	
	public TextAreaField(DataMap d, String a, String l, Composite p, int s) {
		super(d, a, l, p, s);
		createUI();
	}
	
	public void createUI() {
		setLayout(new RowLayout(SWT.VERTICAL));
		Label lbl = new Label(this, SWT.NONE);
		lbl.setText(label);
		lbl.setLayoutData(new RowData(170, 24));
		text = new Text(this, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		text.setLayoutData(new RowData(500, 300));
		if(_data != null && _data.getString(attribute) != null) {
			text.setText(_data.getString(attribute));
			oldValue = _data.getString(attribute);
		}
		text.addModifyListener(this);
	}

	public void modifyText(ModifyEvent event) {
		String newValue = text.getText();
		if(newValue != null && newValue.equals(""))
			newValue = null;
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
