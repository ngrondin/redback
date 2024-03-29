package io.redback.eclipse.editors.components.impl;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.widgets.Composite;

import io.firebus.data.DataMap;
import io.redback.eclipse.editors.components.CheckboxField;
import io.redback.eclipse.editors.components.Form;
import io.redback.eclipse.editors.components.Manager;
import io.redback.eclipse.editors.components.ReadOnlyField;
import io.redback.eclipse.editors.components.ScriptField;
import io.redback.eclipse.editors.components.TextField;

public class ObjectHeaderForm extends Form
{
	public ObjectHeaderForm(DataMap d, Manager m, Composite p, int s) 
	{
		super(d, m, p, s);
		createUI();
	}

	public void createUI() {
		new ReadOnlyField(_data, "name", "Name", this, SWT.NONE);
		new TextField(_data, "collection", "DB Collection", this, SWT.NONE);
		new TextField(_data, "uiddbkey", "UID DB Key", this, SWT.NONE);
		new TextField(_data, "domaindbkey", "Domain DB Key", this, SWT.NONE);
		new TextField(_data, "uidgenerator", "UID Generator", this, SWT.NONE);
		new TextField(_data, "group", "Group", this, SWT.NONE);
		new TextField(_data, "candelete", "Can Delete (!)", this, SWT.NONE);
		new CheckboxField(_data, "trace", "Trace", this, SWT.NONE);
		ScriptField sf = new ScriptField(_data, "datagen", "Data Generation (!)", this, SWT.NONE);
	    RowData rowData = new RowData();
	    rowData.width = 500;
	    rowData.height = 300;
		sf.setLayoutData(rowData);
	}

	public void onFieldUpdate(String attribute, Object oldValue, Object newValue) {

	}


}
