package io.redback.eclipse.editors.components;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class GetDataDialog extends Dialog implements ModifyListener, SelectionListener {

	protected String label;
	protected String value;
	protected String[] options;
	protected String[] optionLabels;
	protected Text text;
	protected Listener listener;
	
	public GetDataDialog(String l, Shell parentShell) {
        super(parentShell);
        label = l;
    }

	public GetDataDialog(String l, String[][] o, Shell parentShell) {
        super(parentShell);
        label = l;
        options = new String[o.length];
        optionLabels = new String[o.length];
        for(int i = 0; i < o.length; i ++) {
        	options[i] = o[i][0];
        	optionLabels[i] = o[i][1];
        }
    }

	
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        
        container.setLayout(new RowLayout(SWT.VERTICAL));
		Label lbl = new Label(container, SWT.NONE);
		lbl.setText(label);
		lbl.setLayoutData(new RowData(170, 24));
		if(options != null) {
			Combo combo = new Combo(container, SWT.DROP_DOWN);
			combo.setItems(optionLabels);
			combo.addSelectionListener(this);
		} else {
			text = new Text(container, SWT.BORDER);
			text.setLayoutData(new RowData(300, 24));
			text.addModifyListener(this);
		}
        return container;
    }	
    
    public String openReturnString() {
    	super.open();
    	return value;
    }
    
	public void modifyText(ModifyEvent event) {
		value = text.getText();
	}

	public void widgetDefaultSelected(SelectionEvent arg0) {
		
	}

	public void widgetSelected(SelectionEvent event) {
        Combo combo = ((Combo) event.widget);
		String val = combo.getText();
		for(int i = 0; i < options.length; i++)
			if(val.equals(optionLabels[i]))
				value = options[i];
	}

} 
