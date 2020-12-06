package io.redback.services.impl;

import java.util.List;

import io.firebus.Firebus;
import io.firebus.utils.DataMap;
import io.redback.RedbackException;
import io.redback.client.DataClient;
import io.redback.security.Session;
import io.redback.services.UserPreferenceServer;
import io.redback.utils.CollectionConfig;

public class RedbackUserPreferenceServer extends UserPreferenceServer {
	protected CollectionConfig collection;
	protected DataClient dataClient;

	public RedbackUserPreferenceServer(String n, DataMap c, Firebus f) {
		super(n, c, f);
		collection = new CollectionConfig(config.getObject("collection"), "rbup_entry");
		dataClient = new DataClient(firebus, config.getString("dataservice"));
	}
	
	protected String getUsername(Session session) {
		return session.getUserProfile().getUsername();
	}
	
	protected String getRole(Session session) {
		List<String> roles = session.getUserProfile().getRoles();
		if(roles.size() > 0) {
			return roles.get(0);
		} else {
			return null;
		}
	}
	
	protected String getDomain(Session session) {
		String domain = session.getUserProfile().getAttribute("rb.defaultdomain");
		if(domain == null) {
			List<String> domains = session.getUserProfile().getDomains();
			if(domains.size() > 0) 
				domain = domains.get(0);
		}
		return domain;
	}
	
	protected DataMap getKeyForUser(Session session, String name) {
		DataMap key = new DataMap();
		key.put("username", getUsername(session));
		key.put("name", name);
		return key;
	}

	protected DataMap getKeyForRole(Session session, String name) {
		DataMap key = new DataMap();
		key.put("role", getRole(session));
		key.put("domain", getDomain(session));
		key.put("name", name);
		return key;
	}

	protected DataMap getKeyForDomain(Session session, String name) {
		DataMap key = new DataMap();
		key.put("domain", getDomain(session));
		key.put("name", name);
		return key;
	}
	
	protected DataMap get(DataMap key) throws RedbackException {
		DataMap resp = collection.convertObjectToCanonical(
				dataClient.getData(
						collection.getName(), 
						collection.convertObjectToSpecific(key), 
						null
				)
		);
		if(resp != null && resp.getList("result") != null && resp.getList("result").size() > 0) {
			return resp.getList("result").getObject(0).getObject("value");
		} else {
			return null;
		}
	}
	
	protected void put(DataMap key, DataMap value) throws RedbackException {
		dataClient.putData(
				collection.getName(), 
				collection.convertObjectToSpecific(key),
				collection.convertObjectToSpecific(new DataMap("value", value))				
		);	
	}

	public DataMap getUserPreference(Session session, String name) throws RedbackException {
		return get(getKeyForUser(session, name));
	}

	public DataMap getRolePreference(Session session, String name) throws RedbackException {
		return get(getKeyForRole(session, name));
	}

	public DataMap getDomainPreference(Session session, String name) throws RedbackException {
		return get(getKeyForDomain(session, name));
	}

	public void putUserPreference(Session session, String name, DataMap value) throws RedbackException {
		put(getKeyForUser(session, name), value);
	}

	public void putRolePreference(Session session, String name, DataMap value) throws RedbackException {
		put(getKeyForRole(session, name), value);
	}

	public void putDomainPreference(Session session, String name, DataMap value) throws RedbackException {
		put(getKeyForDomain(session, name), value);
	}

}
