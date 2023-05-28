package io.redback.services.impl;

import java.util.List;

import io.firebus.Firebus;
import io.firebus.data.DataEntity;
import io.firebus.data.DataMap;
import io.redback.exceptions.RedbackException;
import io.redback.managers.integrationmanager.IntegrationManager;
import io.redback.security.Session;
import io.redback.services.IntegrationServer;

public class RedbackIntegrationServer extends IntegrationServer {
	
	protected IntegrationManager integrationManager;

	public RedbackIntegrationServer(String n, DataMap c, Firebus f) throws RedbackException {
		super(n, c, f);
		integrationManager= new IntegrationManager(n, c, f);
	}
	
	public void configure() {
		integrationManager.configure();
	}

	protected DataMap get(Session session, String name, String domain, String objectName, String uid, DataMap options) throws RedbackException {
		return integrationManager.get(session, name, domain, objectName, uid, options);
	}

	protected List<DataMap> list(Session session, String name, String domain, String objectName, DataMap filter, DataMap options, int page, int pageSize) throws RedbackException {
		return integrationManager.list(session, name, domain, objectName, filter, options, page, pageSize);
	}

	protected DataMap update(Session session, String name, String domain, String objectName, String uid, DataEntity data, DataMap options) throws RedbackException {
		return integrationManager.update(session, name, domain, objectName, uid, data, options);
	}

	protected DataMap create(Session session, String name, String domain, String objectName, DataEntity data, DataMap options) throws RedbackException {
		return integrationManager.create(session, name, domain, objectName, data, options);
	}

	protected void delete(Session session, String name, String domain, String objectName, String uid, DataMap options) throws RedbackException {
		integrationManager.delete(session, name, domain, objectName, uid, options);
	}
	
	protected Object execute(Session session, String name, String domain, String function, DataEntity data, DataMap options) throws RedbackException {
		return integrationManager.execute(session, name, domain, function, data, options);
	}

	protected String getLoginUrl(Session session, String name, String domain) throws RedbackException {
		return integrationManager.getLoginUrl(session, name, domain);
	}

	protected void exchangeAuthCode(Session session, String name, String domain, String code, String state) throws RedbackException {
		integrationManager.exchangeAuthCode(session, name, domain, code, state);		
	}
	
	protected void clearCachedClientData(Session session, String client, String domain) throws RedbackException {

	}
}
