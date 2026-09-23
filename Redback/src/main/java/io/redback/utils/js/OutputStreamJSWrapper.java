package io.redback.utils.js;

import java.io.OutputStream;

import io.redback.exceptions.RedbackException;

public class OutputStreamJSWrapper extends ObjectJSWrapper  {
	protected OutputStream outputStream;

	public OutputStreamJSWrapper(OutputStream os) {
		super(new String[] {});
		outputStream = os;
	}

	public Object get(String key) throws RedbackException {
		return null;
	}

	public OutputStream getOutputStream() {
		return outputStream;
	}

}
