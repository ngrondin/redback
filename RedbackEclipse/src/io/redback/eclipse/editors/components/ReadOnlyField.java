package io.redback.eclipse.editors.components;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import io.firebus.utils.DataMap;

public class ReadOnlyField extends Field  {
	
	protected Text text;
	protected String oldValue;
	
	public ReadOnlyField(DataMap d, String a, String l, Composite p, int s) {
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
		if(data != null && data.getString(attribute) != null) {
			text.setText(data.getString(attribute));
			oldValue = data.getString(attribute);
		}
		text.setEditable(false);
	}


}
