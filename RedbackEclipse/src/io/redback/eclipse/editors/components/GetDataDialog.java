package io.redback.eclipse.editors.components;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class GetDataDialog extends Dialog implements ModifyListener {

	protected String label;
	protected String value;
	protected Text text;
	protected Listener listener;
	
	public GetDataDialog(String l, Shell parentShell) {
        super(parentShell);
        label = l;
    }
	
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        
        container.setLayout(new RowLayout(SWT.VERTICAL));
		Label lbl = new Label(container, SWT.NONE);
		lbl.setText(label);
		lbl.setLayoutData(new RowData(170, 24));
		text = new Text(container, SWT.BORDER);
		text.setLayoutData(new RowData(300, 24));
		text.addModifyListener(this);
        return container;
    }	
    
    public String openReturnString() {
    	super.open();
    	return value;
    }
    
	public void modifyText(ModifyEvent event) {
		value = text.getText();
	}

} 
