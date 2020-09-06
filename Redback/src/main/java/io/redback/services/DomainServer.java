package io.redback.services;

import java.util.List;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.exceptions.FunctionErrorException;
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

	public Payload authenticatedService(Session session, Payload payload) throws FunctionErrorException {
		try {
			DataMap request = new DataMap(payload.getString());
			String action = request.getString("action");
			String name = request.getString("name");
			String category = request.getString("category");
			if(action != null) {
				if(action.equals("putreport")) {
					putReport(session, name, category, request.getObject("report"));
				} else if(action.equals("putvariable")) {
					putVariable(session, name, category, request.getObject("variable"));
				} else if(action.equals("putfunction")) {
					putFunction(session, name, request.getString("function"));
				} else if(action.equals("getreport")) {
					DataMap reportConfig = getReport(session, name);
					return new Payload(reportConfig.toString());
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
					DataEntity entity = getVariable(session, name);
					DataMap resp = new DataMap("result", entity);
					return new Payload(resp.toString());
				} else if(action.equals("executefunction")) {
					executeFunction(session, name, request.getObject("param"));
				}
			} else {
				throw new RedbackException("The action needs to be specified");
			}
			return null;
		} catch(Exception e) {
			throw new FunctionErrorException("Error executing domain service", e);
		}
	}

	public Payload unAuthenticatedService(Session session, Payload payload) throws FunctionErrorException {
		throw new FunctionErrorException("Domain server only accepts authenticated requests");
	}

	public abstract void putReport(Session session, String name, String category, DataMap report) throws RedbackException;
	
	public abstract void putVariable(Session session, String name, String category, DataEntity var) throws RedbackException;
	
	public abstract void putFunction(Session session, String name, String function) throws RedbackException;
	
	public abstract DataMap getReport(Session session, String name) throws RedbackException;
	
	public abstract List<DataMap> listReports(Session session, String category) throws RedbackException;
	
	public abstract DataEntity getVariable(Session session, String name) throws RedbackException;
	
	public abstract void executeFunction(Session session, String name, DataMap param) throws RedbackException;
}
