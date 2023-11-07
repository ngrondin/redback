package io.redback.client;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import io.firebus.data.DataEntity;
import io.firebus.data.DataLiteral;
import io.firebus.data.DataMap;
import io.redback.exceptions.RedbackException;
import io.redback.security.Session;

public class RedbackObjectRemote {
	public DataMap data;
	protected ObjectClient objectClient;
	protected Session session;
	protected Map<String, RedbackObjectRemote> related = new HashMap<String, RedbackObjectRemote>();
	protected String objectName;
	protected String uid;
	protected String domain;
	
	public RedbackObjectRemote(Session s, ObjectClient oc, DataMap d) {
		session = s;
		objectClient = oc;
		data = d;
		objectName = data.getString("objectname");
		uid = data.getString("uid");
		domain = data.getString("domain");
		if(data.containsKey("related")) {
			DataMap relatedData = data.getObject("related"); 
			for(String key: relatedData.keySet())
				related.put(key, new RedbackObjectRemote(session, objectClient, relatedData.getObject(key)));
		}
	}
	
	public String getObjectName() {
		return objectName;
	}
	
	public String getUid() {
		return uid;
	}
	
	public String getDomain() {
		return domain;
	}
	
	public DataMap getData() {
		return data.getObject("data");
	}
	
	public boolean hasAttribute(String attribute) throws RedbackException {
		return get(attribute) != null;
	}
	
	public String getString(String attribute) throws RedbackException {
		if(attribute.equals("uid"))
			return getUid();
		else if(attribute.equals("domain"))
			return getDomain();
		else {
			DataEntity entity = get(attribute);
			if(entity instanceof DataLiteral)
				return ((DataLiteral)entity).getString();
			else
				return entity.toString();
		}
	}
	
	public Number getNumber(String attribute) throws RedbackException {
		return ((DataLiteral)get(attribute)).getNumber();
	}
	
	public Date getDate(String attribute) throws RedbackException {
		return ((DataLiteral)get(attribute)).getDate();
	}

	public boolean getBool(String attribute) throws RedbackException {
		return ((DataLiteral)get(attribute)).getBoolean();
	}
	
	public Object getObject(String attribute) throws RedbackException {
		DataEntity entity = get(attribute);
		if(entity instanceof DataLiteral)
			return ((DataLiteral)entity).getObject();
		else
			return entity;
	}

	protected DataEntity get(String attribute) throws RedbackException {
		if(attribute.indexOf(".") == -1) {
			return data.getObject("data").get(attribute);
		} else {
			String[] parts = attribute.split("\\.");
			String rel = parts[0];
			String rest = attribute.substring(parts[0].length() + 1);
			RedbackObjectRemote rror = getRelated(rel);
			if(rror != null) {
				return rror.get(rest);
			} else {
				return null;
			}
		}
	}
	
	public RedbackObjectRemote getRelated(String attribute, boolean resolveIfMissing) throws RedbackException {
		RedbackObjectRemote rror = null;
		DataEntity val = get(attribute);
		if(!val.equals(null)) { 
			rror = related.get(attribute);
			if(rror == null && resolveIfMissing) {
				rror = objectClient.getRelatedObject(session, getObjectName(), getUid(), attribute);
			}
		}
		return rror;
	}
	
	public RedbackObjectRemote getRelated(String attribute) throws RedbackException {
		return getRelated(attribute, true);
	}
	
	public void set(String attribute, Object value) throws RedbackException {
		set(new DataMap(attribute, value));
	}
	
	public void set(String attribute, RedbackObjectRemote ror) throws RedbackException {
		DataMap val = getValidation(attribute);
		if(val != null && val.containsKey("related") && val.getString("related.object").equals(ror.getObjectName())) {
			String linkValue = ror.getString(val.getString("related.link"));
			set(new DataMap(attribute, linkValue));
			setRelated(attribute, ror);
		}
	}
	
	public void set(DataMap map) throws RedbackException {
		objectClient.updateObject(session, getObjectName(), getUid(), map, false);
	}
	
	public void setRelated(String attribute, RedbackObjectRemote ror) {
		related.put(attribute, ror);
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
		objectClient.execute(session, getObjectName(), getUid(), function, param);
	}
	
	public DataMap getValidation(String attribute) {
		if(data.containsKey("validation"))
			return data.getObject("validation").getObject(attribute);
		else 
			return null;
	}
	
	public String toString() {
		return data.toString();
	}
}
