package io.redback.eclipse.editors.components;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.nic.firebus.utils.DataMap;

public class ReadOnlyField extends Composite  {
	
	protected DataMap map;
	protected String attribute;
	protected Form form;
	protected Text text;
	protected String oldValue;
	
	public ReadOnlyField(DataMap m, String a, String l, Form f, int s) {
		super(f, s);
		map = m;
		attribute = a;
		form = f;
		setLayout(new RowLayout(SWT.HORIZONTAL));
		Label label = new Label(this, SWT.NONE);
		label.setText(l);
		label.setLayoutData(new RowData(170, 24));
		text = new Text(this, SWT.BORDER);
		text.setLayoutData(new RowData(300, 24));
		if(map != null && map.getString(attribute) != null) {
			text.setText(map.getString(attribute));
			oldValue = map.getString(attribute);
		}
		text.setEditable(false);
	}


}
