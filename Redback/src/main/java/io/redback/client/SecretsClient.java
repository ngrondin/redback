package io.redback.client;

import io.firebus.Firebus;
import io.firebus.data.DataMap;
import io.redback.exceptions.RedbackException;
import io.redback.security.Session;

/**
 * Reaches the secrets service over firebus. The service checks the session of every call:
 * - get answers the system user only
 * - put answers the system user or a user with rights
 */
public class SecretsClient extends Client
{
	public SecretsClient(Firebus fb, String sn)
	{
		super(fb, sn);
	}

	public boolean isEnabled(Session session) throws RedbackException
	{
		DataMap resp = requestDataMap(session, new DataMap("action", "enabled"));
		return resp != null && resp.getBoolean("enabled");
	}

	public String get(Session session, String domain, String name) throws RedbackException
	{
		DataMap resp = requestDataMap(session, request("get", domain, name));
		return resp != null ? resp.getString("value") : null;
	}

	public void put(Session session, String domain, String name, String value) throws RedbackException
	{
		DataMap req = request("put", domain, name);
		req.put("value", value);
		requestDataMap(session, req);
	}

	protected DataMap request(String action, String domain, String name)
	{
		DataMap req = new DataMap();
		req.put("action", action);
		req.put("domain", domain);
		req.put("name", name);
		return req;
	}
}
