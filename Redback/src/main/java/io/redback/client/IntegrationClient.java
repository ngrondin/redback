package io.redback.client;

import io.firebus.Firebus;
import io.firebus.data.DataMap;
import io.redback.exceptions.RedbackException;
import io.redback.security.Session;

public class IntegrationClient extends Client {

	public IntegrationClient(Firebus fb, String sn) {
		super(fb, sn);
		setTimeout(60000);
	} 

	public DataMap get(Session session, String client, String domain, String objectName, String uid, DataMap options) throws RedbackException {
		DataMap req = new DataMap();
		req.put("client", client);
		req.put("domain", domain);
		req.put("action", "get");
		req.put("object", objectName);
		req.put("uid", uid);
		if(options != null)
			req.put("options", options);
		return requestDataMap(session, req);
	}
	
	public DataMap list(Session session, String client, String domain, String objectName, DataMap filter, DataMap options) throws RedbackException {
		DataMap req = new DataMap();
		req.put("client", client);
		req.put("domain", domain);
		req.put("action", "list");
		req.put("object", objectName);
		req.put("filter", filter);
		if(options != null)
			req.put("options", options);
		return requestDataMap(session, req);
	}
	
	public DataMap update(Session session, String client, String domain, String objectName, String uid, Object data, DataMap options) throws RedbackException {
		DataMap req = new DataMap();
		req.put("client", client);
		req.put("domain", domain);
		req.put("action", "update");
		req.put("object", objectName);
		req.put("uid", uid);
		req.put("data", data);
		if(options != null)
			req.put("options", options);
		return requestDataMap(session, req);
	}

	public DataMap create(Session session, String client, String domain, String objectName, Object data, DataMap options) throws RedbackException {
		DataMap req = new DataMap();
		req.put("client", client);
		req.put("domain", domain);
		req.put("action", "create");
		req.put("object", objectName);
		req.put("data", data);
		if(options != null)
			req.put("options", options);
		return requestDataMap(session, req);
	}

	public DataMap delete(Session session, String client, String domain, String objectName, String uid, DataMap options) throws RedbackException {
		DataMap req = new DataMap();
		req.put("client", client);
		req.put("domain", domain);
		req.put("action", "delete");
		req.put("object", objectName);
		req.put("uid", uid);
		if(options != null)
			req.put("options", options);
		return requestDataMap(session, req);
	}
	
	public void clearCachedClientData(Session session, String client, String domain) throws RedbackException {
		DataMap req = new DataMap();
		req.put("client", client);
		req.put("domain", domain);
		req.put("action", "clearcacheddata");
		requestDataMap(session, req);
	}

	
}
