package io.redback.eclipse.editors.components;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.nic.firebus.utils.DataMap;

public class TextAreaField extends Composite implements ModifyListener {
	
	protected DataMap map;
	protected String attribute;
	protected Form form;
	protected Text text;
	protected String oldValue;
	
	public TextAreaField(DataMap m, String a, String l, Form f, int s) {
		super(f, s);
		map = m;
		attribute = a;
		form = f;
		setLayout(new RowLayout(SWT.VERTICAL));
		Label label = new Label(this, SWT.NONE);
		label.setText(l);
		label.setLayoutData(new RowData(170, 24));
		text = new Text(this, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		text.setLayoutData(new RowData(500, 300));
		if(map != null && map.getString(attribute) != null) {
			text.setText(map.getString(attribute));
			oldValue = map.getString(attribute);
		}
		text.addModifyListener(this);
	}

	public void modifyText(ModifyEvent event) {
		String newValue = text.getText();
		if(map != null) {
			if(newValue == null && map.get(attribute) != null)
				map.remove(attribute);
			else
				map.put(attribute, newValue);
		}
		form.onFieldUpdate(attribute, oldValue, newValue);
		form.setDataChanged(true);
		oldValue = newValue;
	}

}
