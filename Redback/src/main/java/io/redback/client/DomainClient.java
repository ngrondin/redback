package io.redback.client;


import java.util.ArrayList;
import java.util.List;

import io.firebus.Firebus;
import io.firebus.data.DataEntity;
import io.firebus.data.DataList;
import io.firebus.data.DataMap;
import io.redback.exceptions.RedbackException;
import io.redback.security.Session;

public class DomainClient extends Client {

	public DomainClient(Firebus fb, String sn) {
		super(fb, sn);
	}
	
	public void putReport(Session session, String domain, String name, String category, DataMap report) throws RedbackException {
		try {
			DataMap req = new DataMap();
			req.put("action", "putreport");
			req.put("domain", domain);
			req.put("name", name);
			req.put("category", category);
			req.put("report", report);
			requestDataMap(session, req);
		} catch(Exception e) {
			throw new RedbackException("Error putting domain report", e);
		}
	}

	public void putVariable(Session session, String domain, String name, DataEntity var) throws RedbackException {
		try {
			DataMap req = new DataMap();
			req.put("action", "putvariable");
			req.put("domain", domain);
			req.put("name", name);
			req.put("variable", var);
			requestDataMap(session, req);			
		} catch(Exception e) {
			throw new RedbackException("Error putting domain variable", e);
		}
	}

	public void putFunction(Session session, String domain, String name, String function) throws RedbackException {
		try {
			DataMap req = new DataMap();
			req.put("action", "putfunction");
			req.put("domain", domain);
			req.put("name", name);
			req.put("function", function);
			requestDataMap(session, req);
		} catch(Exception e) {
			throw new RedbackException("Error putting domain function", e);
		}
	}

	public DataMap getReport(Session session, String domain, String name) throws RedbackException {
		try {
			DataMap req = new DataMap();
			req.put("action", "getreport");
			req.put("domain", domain);
			req.put("name", name);
			DataMap resp = requestDataMap(session, req);
			return resp;			
		} catch(Exception e) {
			throw new RedbackException("Error getting domain report", e);
		}
	}

	public List<DataMap> listReports(Session session, String category) throws RedbackException {
		try {
			DataMap req = new DataMap();
			req.put("action", "listreport");
			req.put("category", category);
			DataMap resp = requestDataMap(session, req);
			DataList result = resp.getList("result");
			List<DataMap> list = new ArrayList<DataMap>();
			for(int i = 0; i < result.size(); i++) {
				list.add(result.getObject(i));
			}
			return list;
		} catch(Exception e) {
			throw new RedbackException("Error listing domain report", e);
		}
	}

	public DataEntity getVariable(Session session, String domain, String name) throws RedbackException {
		try {
			DataMap req = new DataMap();
			req.put("action", "getvariable");
			req.put("domain", domain);
			req.put("name", name);
			DataMap resp = requestDataMap(session, req);
			return resp.get("result");				
		} catch(Exception e) {
			throw new RedbackException("Error getting domain variable", e);
		}
	}

	public Object executeFunction(Session session, String domain, String name, DataMap param, boolean async) throws RedbackException {
		try {
			DataMap req = new DataMap();
			req.put("action", "execute");
			req.put("domain", domain);
			req.put("name", name);
			req.put("param", param);
			DataMap res = requestDataMap(session, req, async);
			Object respObject = res.get("data");
			return respObject;
		} catch(Exception e) {
			throw new RedbackException("Error executing domain function", e);
		}
	}
	
	public void clearCache(Session session, String domain, String name) throws RedbackException {
		try {
			DataMap req = new DataMap();
			req.put("action", "clearcache");
			req.put("domain", domain);
			req.put("name", name);
			requestDataMap(session, req);
		} catch(Exception e) {
			throw new RedbackException("Error clearing domain cache", e);
		}
	}
}
