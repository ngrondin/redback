package io.redback.eclipse.editors.components;

import java.util.Arrays;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import io.firebus.utils.DataMap;

public class ScriptField extends Field implements ModifyListener {

	protected StyledText text;
	protected String oldValue;

	public ScriptField(DataMap m, String a, String l, Composite p, int s) {
		super(m, a, l, p, s);
		createUI();
	}
	
	public void createUI() {
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		setLayout(layout);
		Label lbl = new Label(this, SWT.NONE);		
		lbl.setText(label);
		lbl.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		text = new StyledText(this, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.WRAP);
		text.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		text.setFont(new Font( getDisplay(), new FontData( "Fira Code", 10, SWT.NONE)));
		text.addModifyListener(this);
		if(data != null && data.get(attribute) != null) {
			text.setText(data.getString(attribute));
			processStyle();
		}
	}


	public void modifyText(ModifyEvent event) {
		String newValue = text.getText();
		if(data != null) {
			if(newValue == null && data.get(attribute) != null)
				data.remove(attribute);
			else
				data.put(attribute, newValue);
		}
		form.onFieldUpdate(attribute, oldValue, newValue);
		form.setDataChanged(true);
		processStyle();
		oldValue = newValue;
	}
	
	protected void processStyle() {
		List<Character> limits = Arrays.asList(new Character[] {' ', ';', ':', '(', ')', '{', '}', '\r', '\n', '\t', ',', '.', '!', '=', '\'', '"', '[', ']'});
		String txt = text.getText();
		int mark = 0;
		boolean inQuote = false;
		boolean inDoubleQuote = false;
		String beforeDot = null;
		for(int i = 0; i < txt.length(); i++) {
			char c = txt.charAt(i);
			if(inQuote) {
				if(c == '\'') {
					StyleRange style = new StyleRange();
					style.start = mark - 1;
					style.length = (i - mark + 2);
					style.foreground = new Color(getDisplay(), 130, 130, 130);
					text.setStyleRange(style);
					inQuote = false;
					mark = i + 1;
				}
			} else if(inDoubleQuote) {
				if(c == '"') {
					StyleRange style = new StyleRange();
					style.start = mark - 1;
					style.length = (i - mark + 2);
					style.foreground = new Color(getDisplay(), 130, 130, 130);
					text.setStyleRange(style);
					inDoubleQuote = false;
					mark = i + 1;
				}
			} else if(limits.contains(c)) {
				String token = txt.substring(mark, i);
				Color fontColor = null;
				int fontStyle = -1;
				if(token.equals("for") || token.equals("while") || token.equals("if") || token.equals("new") || token.equals("var")) {
					fontStyle = SWT.BOLD;
					fontColor = getDisplay().getSystemColor(SWT.COLOR_BLUE);;
				} else if(token.equals("self") || token.equals("om") || token.equals("log")) {
					fontStyle = SWT.BOLD;
					fontColor = getDisplay().getSystemColor(SWT.COLOR_RED);
				} else if(beforeDot != null && beforeDot.contentEquals("om")) {
					if(token.equals("getObjectList") || token.equals("createObject")) {
						fontStyle = SWT.NONE;
						fontColor = getDisplay().getSystemColor(SWT.COLOR_DARK_MAGENTA);
					}
				} else if(beforeDot != null && beforeDot.contentEquals("self")) {
					if(token.equals("getRelated")) {
						fontStyle = SWT.NONE;
						fontColor = getDisplay().getSystemColor(SWT.COLOR_DARK_MAGENTA);
					} else {
						fontStyle = SWT.NONE;
						fontColor = getDisplay().getSystemColor(SWT.COLOR_DARK_YELLOW);
					}
				} 
				
				if(fontColor != null) {
					StyleRange style = new StyleRange();
					style.start = mark;
					style.length = (i - mark);
					style.fontStyle = fontStyle;
					style.foreground = fontColor;
					text.setStyleRange(style);
				}
				
				if(c == '.') {
					beforeDot = token;
				} else {
					beforeDot = null;
				}
				
				if(c == '.' || c == '(' || c == ')' ||c == '{' || c == '}') {
					StyleRange style = new StyleRange();
					style.start = i;
					style.length = 1;
					style.fontStyle = SWT.BOLD;
					style.foreground = getDisplay().getSystemColor(SWT.COLOR_BLACK);
					text.setStyleRange(style);
				} else if( c == '"') {
					inDoubleQuote = true;
				} else if(c == '\'') {
					inQuote = true;
				}
				mark = i + 1;
			}
		}
		layout(true, true);
	}

}
