package io.redback.client;

import java.util.Date;

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
	
	public String getString(String attribute) {
		if(attribute.equals("uid"))
			return getUid();
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
	
}
