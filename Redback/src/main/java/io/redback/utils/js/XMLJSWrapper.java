package io.redback.utils.js;

import io.redback.exceptions.RedbackException;
import io.redback.utils.XML;

public class XMLJSWrapper extends ObjectJSWrapper {
	protected XML xml;
	
	public XMLJSWrapper(XML x) {
		super(new String[] {"setAttribute", "createChild", "setText", "toString"});
		xml = x;
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
		} else if(key.equals("createChild")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					String tag = arguments[0].toString();
					XML child = xml.createChild(tag);
					return new XMLJSWrapper(child);
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

}
