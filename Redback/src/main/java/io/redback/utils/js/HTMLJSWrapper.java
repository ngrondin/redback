package io.redback.utils.js;

import io.redback.exceptions.RedbackException;
import io.redback.utils.HTML;

public class HTMLJSWrapper extends ObjectJSWrapper
{
	protected HTML html;
	
	public HTMLJSWrapper(HTML h) {
		super(new String[] {"append", "toString"});
		html = h;
	}
	
	public Object get(String key) {
		if(key.equals("append")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					html.append(arguments[0]);
					return null;
				}
			};
		} else if(key.equals("toString")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					return html.toString();
				}
			};
		} else {
			return null;
		}
	}

}
