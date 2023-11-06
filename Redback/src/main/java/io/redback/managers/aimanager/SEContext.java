package io.redback.managers.aimanager;

import java.util.ArrayList;
import java.util.List;

import io.redback.security.Session;

public class SEContext {
	public Session session;
	public List<String> uiActions = new ArrayList<String>();
	public List<SEContextLevel> stack = new ArrayList<SEContextLevel>();
	public StringBuilder textResponse = new StringBuilder();
	
	public SEContext(Session s) {
		session = s;
	}
	
	public void addResponse(String s) {
		if(textResponse.length() > 0) 
			textResponse.append(" ");
		textResponse.append(s.trim());
	}
	
	public void pushContextLevel(SEContextLevel ci) {
		stack.add(ci);
	}

	public SEContextLevel popContextLevel() {
		if(stack.size() > 1) {
			SEContextLevel oc = stack.get(stack.size() - 1);
	 		stack.remove(oc);
	 		return oc;
		} else {
			return null;
		}
	}
	
	public SEContextLevel getContextLevel() {
		return getContextLevel(0);
	}
	
	public SEContextLevel getContextLevel(int l) {
		if(stack.size() > l) {
	 		return stack.get(stack.size() - l - 1);
		} else {
			return null;
		}
	}
}
