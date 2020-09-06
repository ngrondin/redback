package io.redback.services.impl;

import java.util.List;

import io.firebus.Firebus;
import io.firebus.utils.DataEntity;
import io.firebus.utils.DataMap;
import io.redback.RedbackException;
import io.redback.managers.domainmanager.DomainManager;
import io.redback.security.Session;
import io.redback.services.DomainServer;

public class RedbackDomainServer extends DomainServer {

	protected DomainManager domainManager;
	
	public RedbackDomainServer(String n, DataMap c, Firebus f) {
		super(n, c, f);
		domainManager = new DomainManager(firebus, config);
	}

	public void putReport(Session session, String name, String category, DataMap report) throws RedbackException {
		domainManager.putReport(session, name, category, report);
	}

	public void putVariable(Session session, String name, String category, DataEntity var) throws RedbackException {
		domainManager.putVariable(session, name, category, var);
	}

	public void putFunction(Session session, String name, String function) throws RedbackException {
		domainManager.putFunction(session, name, function);
	}

	public DataMap getReport(Session session, String name) throws RedbackException {
		return domainManager.getReport(session, name);
	}

	public List<DataMap> listReports(Session session, String category) throws RedbackException {
		return domainManager.listReports(session, category);
	}

	public DataEntity getVariable(Session session, String name) throws RedbackException {
		return domainManager.getVariable(session, name);
	}

	public void executeFunction(Session session, String name, DataMap param) throws RedbackException {
		
	}

	public void clearCaches() {

	}

}
