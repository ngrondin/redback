package io.redback.utils.js;

import java.util.ArrayList;
import java.util.List;

import io.redback.exceptions.RedbackException;
import io.redback.utils.XML;

public class XMLJSWrapper extends ObjectJSWrapper {
	protected XML xml;
	
	public XMLJSWrapper(XML x) {
		super(new String[] {"setAttribute", "getAttribute", "createChild", "setText", "getText", "toString"});
		xml = x;
	}

	public XML getXML() {
		return xml;
	}
	
	public Object get(String key) throws RedbackException {
		if(key.equals("setAttribute")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					String key = arguments[0].toString();
					String val = arguments[1].toString();
					xml.setAttribute(key, val);
					return null;
				}
			};
		} else 	if(key.equals("getAttribute")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					String key = arguments[0].toString();
					return xml.getAttribute(key);
				}
			};
		} else if(key.equals("createChild")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					String tag = arguments[0].toString();
					XML child = xml.createChild(tag);
					return new XMLJSWrapper(child);
				}
			};
		} else if(key.equals("getChildren")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					List<XML> children = xml.getChildren();
					return convertList(children);
				}
			};	
		} else if(key.equals("getChildrenByTagName")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					String tag = arguments[0].toString();
					List<XML> children = xml.getChildrenByTagName(tag);
					return convertList(children);
				}
			};	
		} else if(key.equals("getFirstChildByTagName")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					String tag = arguments[0].toString();
					XML child = xml.getFirstChildByTagName(tag);
					return child != null ? new XMLJSWrapper(child) : null;
				}
			};				
		} else if(key.equals("setText")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					String val = arguments.length > 0 && arguments[0] != null ? arguments[0].toString() : "";
					xml.setText(val);
					return null;
				}
			};
		} else if(key.equals("getText")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					return xml.getText();
				}
			};			
		} else if(key.equals("toString")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					return xml.toString();
				}
			};
		}  
		return null;
	}
	
	public String toString() {
		return xml.toString();
	}

	private List<XMLJSWrapper> convertList(List<XML> list) {
		List<XMLJSWrapper> retList = new ArrayList<XMLJSWrapper>();
		for(XML child: list) 
			retList.add(new XMLJSWrapper(child));
		return retList;
	}
}
