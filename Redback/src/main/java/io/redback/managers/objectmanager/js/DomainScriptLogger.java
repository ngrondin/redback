package io.redback.managers.objectmanager.js;


import java.util.Date;
import java.util.UUID;

import io.firebus.data.DataMap;
import io.firebus.logging.Logger;
import io.redback.client.DataClient;
import io.redback.exceptions.RedbackException;
import io.redback.security.Session;
import io.redback.utils.CollectionConfig;
import io.redback.utils.js.CallableJSWrapper;


public class DomainScriptLogger extends CallableJSWrapper
{
	protected DataClient dataClient;
	protected CollectionConfig collectionConfig;
	protected Session session;
	protected String domain;
	protected String scriptName;
	protected int maxLevel;
	protected StringBuilder builder;
	
	public DomainScriptLogger(DataClient dc, CollectionConfig cc, Session s, String d, String n, String lvl)
	{
		builder = new StringBuilder();
		dataClient = dc;
		collectionConfig = cc;
		session = s;
		domain = d;
		scriptName = n;
		maxLevel = Logger.getLevelFromString(lvl);
	}


	public Object call(Object... arguments) throws RedbackException {
		int entryLevel = Logger.getLevelFromString(arguments.length == 1 ? "info" : arguments[0].toString().toLowerCase());
		Object val = arguments.length == 1 ?  arguments[0] : arguments[1];
		String msg = val != null ? val.toString() : "null";
		if(entryLevel <= maxLevel) {
			builder.append(msg);
			builder.append("\r\n");
		}
		return null;
	}
	
	public void log(String txt) {
		builder.append(txt);
	}
	
	public void commit() {
		try {
			String id = UUID.randomUUID().toString();
			DataMap key = new DataMap("_id", id);
			DataMap log = new DataMap();
			log.put("domain", domain);
			log.put("script", scriptName);
			log.put("entry", builder.toString());
			log.put("username", session.getUserProfile().getUsername());
			log.put("date", new Date());
			dataClient.putData(collectionConfig.getName(), collectionConfig.convertObjectToSpecific(key), collectionConfig.convertObjectToSpecific(log));
		} catch(Exception e) {
			Logger.severe("rb.script.addlog", "Error adding log entry", e);
		}
	}
}
