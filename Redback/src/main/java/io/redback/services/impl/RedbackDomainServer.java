package io.redback.services.impl;

import java.util.List;

import io.firebus.Firebus;
import io.firebus.utils.DataEntity;
import io.firebus.utils.DataMap;
import io.redback.RedbackException;
import io.redback.managers.domainmanager.DomainFunctionInfo;
import io.redback.managers.domainmanager.DomainManager;
import io.redback.security.Session;
import io.redback.services.DomainServer;

public class RedbackDomainServer extends DomainServer {

	protected DomainManager domainManager;
	
	public RedbackDomainServer(String n, DataMap c, Firebus f) {
		super(n, c, f);
		domainManager = new DomainManager(firebus, config);
	}

	public void putVariable(Session session, String domain, String name, DataEntity var) throws RedbackException {
		domainManager.putVariable(session, domain, name, var);
	}

	public DataEntity getVariable(Session session, String domain, String name) throws RedbackException {
		return domainManager.getVariable(session, domain, name);
	}

	public void putFunction(Session session, String domain, String name, String function) throws RedbackException {
		domainManager.putFunction(session, domain, name, function);
	}

	public List<DomainFunctionInfo> listFunctions(Session session, String domain, String category) throws RedbackException {
		return domainManager.listFunctions(session, domain, category);
	}

	public Object executeFunction(Session session, String domain, String name, DataMap param, boolean async) throws RedbackException {
		return domainManager.executeFunction(session, domain, name, param, async);
	}

	public void executeFunctionInAllDomains(Session session, String name, DataMap param) {
		domainManager.executeFunctionInAllDomains(session, name, param);
	}

	public void clearCache(Session session, String domain, String name) throws RedbackException {
		domainManager.clearCache(session, domain, name);
	}

	public void configure() {
		
	}

	public void start() {
		
	}	
}
