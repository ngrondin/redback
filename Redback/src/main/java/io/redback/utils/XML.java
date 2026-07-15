package io.redback.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class XML {
	protected String tag;
	protected Map<String, String> attributes = new HashMap<String, String>();
	protected List<XML> children = new ArrayList<XML>();
	protected String text;
	
	public XML(String t) {
		tag = t;
	}
	
	public void setAttribute(String k, String v) {
		attributes.put(k, v);
	}
	
	public void addChild(XML child) {
		children.add(child);
	}
	
	public XML createChild(String t) {
		XML child = new XML(t);
		addChild(child);
		return child;
	}
	
	public void setText(String t) {
		text = t;
	}
	
	public void writeToStringBuilder(StringBuilder sb, String indentStr) {
		sb.append(indentStr);
		sb.append("<");
		sb.append(tag);
		for(String key: attributes.keySet()) {
			sb.append(" ");
			sb.append(key);
			sb.append("=\"");
			sb.append(attributes.get(key));
			sb.append("\"");
		}
		sb.append(">");
		if(children.size() > 0) {
			sb.append("\r\n");
			String subIndentStr = indentStr + "  ";
			for(XML child: children) 
				child.writeToStringBuilder(sb, subIndentStr);	
			sb.append(indentStr);			
		} else if(text != null) {
			sb.append(text);
		}
		sb.append("</");
		sb.append(tag);
		sb.append(">");
		sb.append("\r\n");
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		writeToStringBuilder(sb, "");
		return sb.toString();
	}

}
