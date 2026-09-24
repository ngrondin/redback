package io.redback.utils.js;

import java.io.ByteArrayInputStream;
import java.io.OutputStream;

import io.firebus.data.DataMap;
import io.redback.exceptions.RedbackException;

public class OutputStreamJSWrapper extends ObjectJSWrapper  {
	protected OutputStream outputStream;

	public OutputStreamJSWrapper(OutputStream os) {
		super(new String[] {});
		outputStream = os;
	}

	public Object get(String key) throws RedbackException {
		if(key.equals("getCompletionData")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					if(outputStream instanceof io.firebus.utils.OutputStream) {
						byte[] bytes = ((io.firebus.utils.OutputStream)outputStream).getCompletionBytes();
						if(bytes != null) {
							try {
								DataMap data = new DataMap(new ByteArrayInputStream(bytes));
								return data;
							} catch(Exception e) { }
						}
					}
					return null;
				}
			};
		}
		return null;
	}

	public OutputStream getOutputStream() {
		return outputStream;
	}

}
