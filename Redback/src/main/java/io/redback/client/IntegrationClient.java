package io.redback.client;

import io.firebus.Firebus;
import io.firebus.utils.DataMap;
import io.redback.RedbackException;
import io.redback.security.Session;

public class IntegrationClient extends Client {

	public IntegrationClient(Firebus fb, String sn) {
		super(fb, sn);
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
		return request(session, req);
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
		return request(session, req);
	}
	
	public DataMap update(Session session, String client, String domain, String objectName, String uid, DataMap data, DataMap options) throws RedbackException {
		DataMap req = new DataMap();
		req.put("client", client);
		req.put("domain", domain);
		req.put("action", "update");
		req.put("object", objectName);
		req.put("uid", uid);
		req.put("data", data);
		if(options != null)
			req.put("options", options);
		return request(session, req);
	}

	public DataMap create(Session session, String client, String domain, String objectName, DataMap data, DataMap options) throws RedbackException {
		DataMap req = new DataMap();
		req.put("client", client);
		req.put("domain", domain);
		req.put("action", "create");
		req.put("object", objectName);
		req.put("data", data);
		if(options != null)
			req.put("options", options);
		return request(session, req);
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
		return request(session, req);
	}
	
	public void clearCachedClientData(Session session, String client, String domain) throws RedbackException {
		DataMap req = new DataMap();
		req.put("client", client);
		req.put("domain", domain);
		req.put("action", "clearcacheddata");
		request(session, req);
	}

	
}
