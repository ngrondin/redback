package io.redback.services;

import java.util.List;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.information.ServiceInformation;
import io.firebus.data.DataEntity;
import io.firebus.data.DataList;
import io.firebus.data.DataMap;
import io.redback.exceptions.RedbackException;
import io.redback.managers.domainmanager.DomainFunctionInfo;
import io.redback.security.Session;
import io.redback.services.common.AuthenticatedServiceProvider;

public abstract class DomainServer extends AuthenticatedServiceProvider {

	public DomainServer(String n, DataMap c, Firebus f) {
		super(n, c, f);
	}

	public ServiceInformation getServiceInformation() {
		return null;
	}

	public Payload redbackAuthenticatedService(Session session, Payload payload) throws RedbackException {
		try {
			DataMap request = new DataMap(payload.getString());
			String action = request.getString("action");
			String domain = request.getString("domain");
			String name = request.getString("name");
			String category = request.getString("category");
			if(action != null) {
				if(action.equals("putvariable")) {
					putVariable(session, domain, name, request.getObject("variable"));
				} else if(action.equals("putfunction")) {
					putFunction(session, domain, name, request.getString("function"));
				} else if(action.equals("listfunctions")) {
					List<DomainFunctionInfo> list = listFunctions(session, domain, category);
					DataMap resp = new DataMap();
					DataList result = new DataList();
					for(DomainFunctionInfo dfi : list) {
						DataMap map = new DataMap();
						map.put("name", dfi.name);
						map.put("description", dfi.description);
						if(dfi.timeout > -1) map.put("timeout", dfi.timeout);
						result.add(map);
					}
					resp.put("result", result);
					return new Payload(resp.toString());
				} else if(action.equals("getvariable")) {
					DataEntity entity = getVariable(session, domain, name);
					return new Payload((new DataMap("result", entity)).toString());
				} else if(action.equals("execute")) {
					boolean async = request.getBoolean("async");
					DataMap param = request.getObject("param");
					Object ret = executeFunction(session, domain, name, param, async);
					return new Payload(ret != null ? ret.toString() : new DataMap("result", "ok").toString());
				} else if(action.equals("executeinalldomains")) {
					executeFunctionInAllDomains(session, name, request.getObject("param"));
					return new Payload(new DataMap("result", "ok").toString());
				} else if(action.equals("clearcache")) {
					clearCache(session, domain, name);
					return new Payload(new DataMap("result", "ok").toString());
				} else {
					throw new RedbackException("The action '" + action + "' is not valid");
				}
			} else {
				throw new RedbackException("The action needs to be provided");
			}
			return null;
		} catch(Exception e) {
			throw new RedbackException("Error executing domain service", e);
		}
	}

	public Payload redbackUnauthenticatedService(Session session, Payload payload) throws RedbackException {
		throw new RedbackException("Domain server only accepts authenticated requests");
	}

	public abstract void putVariable(Session session, String domain, String name, DataEntity var) throws RedbackException;
	
	public abstract DataEntity getVariable(Session session, String domain, String name) throws RedbackException;	
	
	public abstract void putFunction(Session session, String domain, String name, String function) throws RedbackException;
	
	public abstract List<DomainFunctionInfo> listFunctions(Session session, String domain, String category) throws RedbackException;
	
	public abstract Object executeFunction(Session session, String domain, String name, DataMap param, boolean async) throws RedbackException;

	public abstract void executeFunctionInAllDomains(Session session, String name, DataMap param);

	public abstract void clearCache(Session session, String domain, String name) throws RedbackException;
}
