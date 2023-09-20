package io.redback.services.impl;

import java.util.List;

import io.firebus.Firebus;
import io.firebus.data.DataMap;
import io.redback.client.DataClient;
import io.redback.exceptions.RedbackException;
import io.redback.security.Session;
import io.redback.services.UserPreferenceServer;
import io.redback.utils.CollectionConfig;
import io.redback.utils.Convert;

public class RedbackUserPreferenceServer extends UserPreferenceServer {
	protected CollectionConfig collection;
	protected DataClient dataClient;

	public RedbackUserPreferenceServer(String n, DataMap c, Firebus f) {
		super(n, c, f);
		collection = new CollectionConfig(config.getObject("collection"), "rbup_entry");
		dataClient = new DataClient(firebus, config.getString("dataservice"));
	}	
	
	protected DataMap getRoleFilterClause(Session session) {
		List<String> roles = session.getUserProfile().getRoles();
		return new DataMap("$in", Convert.listToDataList(roles));
	}
	
	protected String getDefaultRole(Session session) {
		List<String> roles = session.getUserProfile().getRoles();
		if(roles.size() > 0) 
			return roles.get(0);
		else
			return null;
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
		DataMap key = new DataMap();
		key.put("username", session.getUserProfile().getUsername());
		key.put("name", name);
		return get(key);
	}

	public DataMap getRolePreference(Session session, String name) throws RedbackException {
		DataMap key = new DataMap();
		key.put("role", getRoleFilterClause(session));
		key.put("domain", session.getDomainFilterClause());
		key.put("name", name);
		return get(key);
	}

	public DataMap getDomainPreference(Session session, String name) throws RedbackException {
		DataMap key = new DataMap();
		key.put("domain", session.getDomainFilterClause());
		key.put("name", name);
		return get(key);
	}

	public void putUserPreference(Session session, String name, DataMap value) throws RedbackException {
		DataMap key = new DataMap();
		key.put("username", session.getUserProfile().getUsername());
		key.put("name", name);
		put(key, value);
	}

	public void putRolePreference(Session session, String name, DataMap value) throws RedbackException {
		DataMap key = new DataMap();
		key.put("role", getDefaultRole(session));
		key.put("domain", session.getUserProfile().getDefaultDomain());
		key.put("name", name);		
		put(key, value);
	}

	public void putDomainPreference(Session session, String name, DataMap value) throws RedbackException {
		DataMap key = new DataMap();
		key.put("domain", session.getUserProfile().getDefaultDomain());
		key.put("name", name);
		put(key, value);
	}

}
