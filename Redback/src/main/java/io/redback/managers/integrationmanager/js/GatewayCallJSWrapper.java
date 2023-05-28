package io.redback.managers.integrationmanager.js;

import io.firebus.data.DataMap;
import io.redback.exceptions.RedbackException;
import io.redback.managers.integrationmanager.Client;
import io.redback.utils.js.CallableJSWrapper;

public class GatewayCallJSWrapper extends CallableJSWrapper {
	protected Client client;
	
	public GatewayCallJSWrapper(Client c) {
		client = c;
	}
	
	public Object call(Object... arguments) throws RedbackException {
		String method = arguments.length > 0 && arguments[0] != null ? arguments[0].toString() : null;
		String url = arguments.length > 1 && arguments[1] != null ? arguments[1].toString() : null;
		Object body = arguments.length > 2 && arguments[2] != null ? arguments[2] : null;
		DataMap headers = arguments.length > 3 && arguments[3] != null && arguments[3] instanceof DataMap ? (DataMap)arguments[3] : null;
		DataMap resp = client.call(method, url, body, headers);
		return resp;
	}
}