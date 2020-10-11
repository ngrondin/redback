package io.redback.services;

import java.util.List;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.information.ServiceInformation;
import io.firebus.utils.DataEntity;
import io.firebus.utils.DataList;
import io.firebus.utils.DataMap;
import io.redback.RedbackException;
import io.redback.security.Session;

public abstract class DomainServer extends AuthenticatedServiceProvider {

	public DomainServer(String n, DataMap c, Firebus f) {
		super(n, c, f);
	}

	public ServiceInformation getServiceInformation() {
		return null;
	}

	public Payload authenticatedService(Session session, Payload payload) throws RedbackException {
		try {
			DataMap request = new DataMap(payload.getString());
			String action = request.getString("action");
			String domain = request.getString("domain");
			String name = request.getString("name");
			String category = request.getString("category");
			if(action != null) {
				if(action.equals("putreport")) {
					putReport(session, domain, name, category, request.getObject("report"));
				} else if(action.equals("putvariable")) {
					putVariable(session, domain, name, request.getObject("variable"));
				} else if(action.equals("putfunction")) {
					putFunction(session, domain, name, request.getString("function"));
				} else if(action.equals("getreport")) {
					DataMap reportConfig = getReport(session, domain, name);
					if(reportConfig != null) {
						return new Payload(reportConfig.toString());
					} else {
						return new Payload();
					}
				} else if(action.equals("listreport")) {
					List<DataMap> list = listReports(session, category);
					DataMap resp = new DataMap();
					DataList result = new DataList();
					for(DataMap map : list) {
						result.add(map);
					}
					resp.put("result", result);
					return new Payload(resp.toString());
				} else if(action.equals("getvariable")) {
					DataEntity entity = getVariable(session, domain, name);
					if(entity != null) {
						DataMap resp = new DataMap("result", entity);
						return new Payload(resp.toString());
					} else {
						return new Payload();
					}
				} else if(action.equals("execute")) {
					Object ret = executeFunction(session, domain, name, request.getObject("param"));
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

	public Payload unAuthenticatedService(Session session, Payload payload) throws RedbackException {
		throw new RedbackException("Domain server only accepts authenticated requests");
	}

	public abstract void putReport(Session session, String domain, String name, String category, DataMap report) throws RedbackException;
	
	public abstract void putVariable(Session session, String domain, String name, DataEntity var) throws RedbackException;
	
	public abstract void putFunction(Session session, String domain, String name, String function) throws RedbackException;
	
	public abstract DataMap getReport(Session session, String domain, String name) throws RedbackException;
	
	public abstract List<DataMap> listReports(Session session, String category) throws RedbackException;
	
	public abstract DataEntity getVariable(Session session, String domain, String name) throws RedbackException;
	
	public abstract Object executeFunction(Session session, String domain, String name, DataMap param) throws RedbackException;

	public abstract void executeFunctionInAllDomains(Session session, String name, DataMap param);

	public abstract void clearCache(Session session, String domain, String name) throws RedbackException;
}
