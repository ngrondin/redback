package io.redback.client.js;

import io.firebus.data.DataMap;
import io.redback.client.GatewayClient;
import io.redback.exceptions.RedbackException;
import io.redback.utils.js.CallableJSWrapper;
import io.redback.utils.js.ObjectJSWrapper;

public class GatewayClientJSWrapper extends ObjectJSWrapper {
	
	protected GatewayClient gatewayClient;

	public GatewayClientJSWrapper(GatewayClient gc)
	{
		super(new String[] {"get", "post", "postform", "put", "putform", "patch", "patchform"});
		gatewayClient = gc;
	}
	
	public Object get(String key) {
		if(key.equals("get")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					String url = (String)arguments[0];
					DataMap headers = arguments.length > 1 ? (DataMap)arguments[1] : null;
					DataMap cookie = arguments.length > 2 ? (DataMap)arguments[2] : null;
					return gatewayClient.get(url, headers, cookie);

				}
			};
		} else if(key.equals("post")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					String url = (String)arguments[0];
					Object body = arguments[1];
					DataMap headers = arguments.length > 2 ? (DataMap)arguments[2] : null;
					DataMap cookie = arguments.length > 3 ? (DataMap)arguments[3] : null;
					return gatewayClient.post(url, body, headers, cookie);

				}
			};
		} else if(key.equals("postform")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					String url = (String)arguments[0];
					DataMap form = (DataMap)arguments[1];
					DataMap headers = arguments.length > 2 ? (DataMap)arguments[2] : null;
					DataMap cookie = arguments.length > 3 ? (DataMap)arguments[3] : null;
					return gatewayClient.postForm(url, form, headers, cookie);

				}
			};
		} else if(key.equals("put")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					String url = (String)arguments[0];
					Object body = arguments[1];
					DataMap headers = arguments.length > 2 ? (DataMap)arguments[2] : null;
					DataMap cookie = arguments.length > 3 ? (DataMap)arguments[3] : null;
					return gatewayClient.put(url, body, headers, cookie);

				}
			};
		} else if(key.equals("putform")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					String url = (String)arguments[0];
					DataMap form = (DataMap)arguments[1];
					DataMap headers = arguments.length > 2 ? (DataMap)arguments[2] : null;
					DataMap cookie = arguments.length > 3 ? (DataMap)arguments[3] : null;
					return gatewayClient.putForm(url, form, headers, cookie);

				}
			};
		} else if(key.equals("patch")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					String url = (String)arguments[0];
					Object body = arguments[1];
					DataMap headers = arguments.length > 2 ? (DataMap)arguments[2] : null;
					DataMap cookie = arguments.length > 3 ? (DataMap)arguments[3] : null;
					return gatewayClient.patch(url, body, headers, cookie);

				}
			};
		} else if(key.equals("patchform")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					String url = (String)arguments[0];
					DataMap form = (DataMap)arguments[1];
					DataMap headers = arguments.length > 2 ? (DataMap)arguments[2] : null;
					DataMap cookie = arguments.length > 3 ? (DataMap)arguments[3] : null;
					return gatewayClient.patchForm(url, form, headers, cookie);
				}
			};			
		} else {
			return null;
		}
	}
}
