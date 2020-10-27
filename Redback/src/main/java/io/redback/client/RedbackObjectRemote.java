package io.redback.client;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.utils.DataEntity;
import io.firebus.utils.DataLiteral;
import io.firebus.utils.DataMap;
import io.redback.RedbackException;

public class RedbackObjectRemote {
	public DataMap data;
	protected Firebus firebus;
	protected String objectService;
	protected String token;
	
	public RedbackObjectRemote(Firebus fb, String os, String t, DataMap d) {
		firebus = fb;
		objectService = os;
		token = t;
		data = d;
	}
	
	public String getUid() {
		return data.getString("uid");
	}
	
	public String getDomain() {
		return data.getString("domain");
	}
	
	public String getString(String attribute) {
		if(attribute.equals("uid"))
			return getUid();
		else if(attribute.equals("domain"))
			return getDomain();
		else
			return ((DataLiteral)get(attribute)).getString();
	}
	
	public Number getNumber(String attribute) {
		return ((DataLiteral)get(attribute)).getNumber();
	}
	
	public Date getDate(String attribute) {
		return ((DataLiteral)get(attribute)).getDate();
	}

	public boolean getBool(String attribute) {
		return ((DataLiteral)get(attribute)).getBoolean();
	}

	public DataEntity get(String attribute) {
		if(attribute.indexOf(".") == -1) {
			return data.get("data." + attribute);
		} else {
			String[] parts = attribute.split(".");
			if(data.containsKey("related") && data.containsKey("related." + parts[0])) {				
				return data.get("related." + parts[0] + ".data." + parts[1]);
			} else {
				return null;
			}
		}
	}
	
	public RedbackObjectRemote getRelated(String attribute) {
		if(data.containsKey("related")) {
			DataMap d = data.getObject("related." + attribute);
			if(d != null)
				return new RedbackObjectRemote(firebus, objectService, token, d);
			else
				return null;
		} else {
			return null;
		}
	}
	
	public void set(String attribute, Object value) throws RedbackException {
		set(new DataMap(attribute, value));
	}
	
	public void set(DataMap map) throws RedbackException {
		try {
			DataMap req = new DataMap();
			req.put("action", "update");
			req.put("object", data.getString("objectname"));
			req.put("uid", getUid());
			req.put("data", map);
			Payload reqP = new Payload(req.toString());
			reqP.metadata.put("token", token);
			Payload respP = firebus.requestService(objectService, reqP);
			DataMap resp = new DataMap(respP.getString());
			this.data = resp;
		} catch(Exception e) {
			throw new RedbackException("Error updating object", e);
		}
	}
	
	public List<String> getAttributeNames() {
		List<String> ret = new ArrayList<String>();
		Iterator<String> it = data.getObject("data").keySet().iterator();
		while(it.hasNext()) {
			ret.add(it.next());
		}
		return ret;
	}
	
	public void execute(String function, DataMap param) throws RedbackException {
		try {
			DataMap req = new DataMap();
			req.put("action", "execute");
			req.put("object", data.getString("objectname"));
			req.put("uid", getUid());
			req.put("function", function);
			req.put("data", param);
			Payload reqP = new Payload(req.toString());
			reqP.metadata.put("token", token);
			Payload respP = firebus.requestService(objectService, reqP);
			DataMap resp = new DataMap(respP.getString());
			this.data = resp;
		} catch(Exception e) {
			throw new RedbackException("Error execute function on object", e);
		}		
	}
	
}
