package io.redback.utils.stream.js;

import io.redback.exceptions.RedbackException;
import io.redback.utils.js.CallableJSWrapper;
import io.redback.utils.js.ObjectJSWrapper;
import io.redback.utils.stream.DataStream;

public class DataStreamJSWrapper<T> extends ObjectJSWrapper {
	protected DataStream<T> stream;
	protected int i = 0;
	
	public DataStreamJSWrapper(DataStream<T> s) {
		super(new String[] {"send"});
		stream = s;
	}

	public Object get(String key) throws RedbackException {
		if(key.equals("send")) {
			return new CallableJSWrapper() {
				@SuppressWarnings("unchecked")
				public Object call(Object... arguments) throws RedbackException {
					try {
						T data = (T)arguments[0];
						if(data != null) {
							stream.send(data);
						}
						return null;
					} catch(ClassCastException e) {
						throw new RedbackException("Sent the wrong data type to the stream", e);
					}
				}
			};
		} else {
			return null;
		}
	}

}
