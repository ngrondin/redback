package io.redback.eclipse.editors.components.impl;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

import io.firebus.data.DataList;
import io.firebus.data.DataMap;
import io.redback.eclipse.editors.components.CheckboxField;
import io.redback.eclipse.editors.components.Manager;
import io.redback.eclipse.editors.components.NavigatorAction;
import io.redback.eclipse.editors.components.SelectField;
import io.redback.eclipse.editors.components.TextField;

public class ProcessInteractionForm extends ProcessForm implements SelectionListener
{
	protected String[] assigneeTypeOptions = new String[] {"user", "group", "process"};
	protected String[] assigneeTypeOptionLabels = new String[] {"User", "Group", "Process"};

	public ProcessInteractionForm(DataMap d, DataList n, Manager m, Composite p, int s) 
	{
		super(d, n, m, p, s);
		if(_data.getList("assignees") == null)
			_data.put("assignees", new DataList());
		if(_data.getList("actions") == null)
			_data.put("actions", new DataList());
		if(_data.getObject("notification") == null)
			_data.put("notification", new DataMap());
		createUI();
	}
	
	public void createUI() {
		setLayout(new RowLayout(SWT.HORIZONTAL));
		Composite col1 = new Composite(this, SWT.NONE);
		col1.setLayout(new RowLayout(SWT.VERTICAL));
		new TextField(_data, "name", "Name", col1, SWT.NONE);
		new TextField(_data, "notification.code", "Interaction Code", col1, SWT.NONE);
		new SelectField(_data, "notification.type", "Type", new String[] {"exception", "operation", "option", "notification"}, new String[] {"Exception", "Operation", "Option", "Notification"}, col1, SWT.NONE);
		new TextField(_data, "notification.label", "Label (!)", col1, SWT.NONE);
		new TextField(_data, "notification.message", "Message (!)", col1, SWT.NONE);

		Composite col2 = new Composite(this, SWT.NONE);
		col2.setLayout(new RowLayout(SWT.VERTICAL));
		DataList list = _data.getList("assignees");
		for(int i = 0; i < list.size(); i++) {
			DataMap assignee = list.getObject(i);
			Group group = new Group(col2, SWT.NONE);
			group.setLayout(new RowLayout(SWT.VERTICAL));
			group.setText("Assignee " + (i + 1));
			new TextField(assignee, "id", "Id (!)", group, SWT.NONE);
			new SelectField(assignee, "type", "Type", assigneeTypeOptions, assigneeTypeOptionLabels, group, SWT.NONE);
			Button del = new Button(group, SWT.NONE);
			del.setText("Remove");
			del.setData(new NavigatorAction("delete", "assignee", "" + i));
			del.addSelectionListener(this);
		}
		  
		Button add = new Button(col2, SWT.NONE);
		add.setText("Add");
		add.setData(new NavigatorAction("create", "assignee", null));
		add.addSelectionListener(this);

		Composite col3 = new Composite(this, SWT.NONE);
		col3.setLayout(new RowLayout(SWT.VERTICAL));
		new SelectField(_data, "interruption", "Interruption", nodeOptions, nodeOptionLabels, col3, SWT.NONE);
		list = _data.getList("actions");
		for(int i = 0; i < list.size(); i++) {
			DataMap action = list.getObject(i);
			Group group = new Group(col3, SWT.NONE);
			group.setLayout(new RowLayout(SWT.VERTICAL));
			group.setText("Action " + (i + 1));
			new TextField(action, "action", "Action", group, SWT.NONE);
			new TextField(action, "description", "Description", group, SWT.NONE);
			new TextField(action, "exclusive", "Exclusive (!)", group, SWT.NONE);
			new SelectField(action, "nextnode", "Next Node", nodeOptions, nodeOptionLabels, group, SWT.NONE);
			new CheckboxField(action, "main", "Main", group, SWT.NONE);
			Button del = new Button(group, SWT.NONE);
			del.setText("Remove");
			del.setData(new NavigatorAction("delete", "action", "" + i));
			del.addSelectionListener(this);
		}
		add = new Button(col3, SWT.NONE);
		add.setText("Add");
		add.setData(new NavigatorAction("create", "action", null));
		add.addSelectionListener(this);
	}

	public void widgetDefaultSelected(SelectionEvent event) {
	}

	public void widgetSelected(SelectionEvent event) {
		NavigatorAction action = (NavigatorAction)event.widget.getData();
		if(action.action.equals("create")) {
			if(action.type.equals("assignee")) {
				_data.getList("assignees").add(new DataMap());
			} else if(action.type.equals("action")) {
				_data.getList("actions").add(new DataMap());
			}
		} if(action.action.equals("delete")) {
			if(action.type.equals("assignee")) {
				_data.getList("assignees").remove(Integer.parseInt(action.name));
			} else if(action.type.equals("action")) {
				_data.getList("actions").remove(Integer.parseInt(action.name));
			}
			
		}
		refresh();
	}

}
