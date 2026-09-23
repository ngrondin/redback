package io.redback.utils.js;

import java.io.InputStream;

import io.redback.exceptions.RedbackException;

public class InputStreamJSWrapper extends ObjectJSWrapper {
	protected InputStream inputStream;
	
	public InputStreamJSWrapper(InputStream is) {
		super(new String[] {});
		inputStream = is;
	}
	
	public Object get(String key) throws RedbackException {
		return null;
	}
	
	public InputStream getInputStream() {
		return inputStream;
	}


}
