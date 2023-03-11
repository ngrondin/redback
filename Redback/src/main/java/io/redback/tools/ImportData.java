package io.redback.tools;

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.data.DataList;
import io.firebus.data.DataMap;
import io.firebus.logging.Logger;
import io.redback.security.Session;

public class ImportData extends Thread
{
	protected Firebus firebus;
	protected String token;
	protected String objectService;
	protected String domain;
	protected String fileurl;
	protected String username;
	
	public ImportData(Firebus fb, String t, String os, String d, String fu)
	{
		firebus = fb;
		token = t;
		objectService = os;
		domain = d;
		fileurl = fu;
		DecodedJWT jwt = JWT.decode(token);
		username = jwt.getClaim("email").asString();
	}
	
	public void importDataAsync()
	{
		start();
	}
	
	public void importData()
	{
		run();
	}
	
	public void run()
	{
		Session session = new Session();
		Map<String, String> keyMap = new HashMap<String, String>();
		try
		{
			InputStream is = null;
			Logger.info("rb.import.start", new DataMap("file", fileurl));
			if(fileurl.startsWith("classpath:")) {
				URL url = this.getClass().getClassLoader().getResource(fileurl.substring(11));
				is = url.openStream();
			} else {
				is = new FileInputStream(fileurl);
			}
			if(is != null) 
			{
				DataMap input = new DataMap(is);
				is.close();
				boolean skippedAtLeastOne;
				do
				{
					skippedAtLeastOne = false;
					Iterator<String> it1 = input.keySet().iterator();
					while(it1.hasNext())
					{
						String objectname = it1.next();
						DataMap objects = input.getObject(objectname);
						String[] oldUids = objects.keySet().toArray(new String [0]);
						for(int i = 0; i < oldUids.length; i++)
						{
							String oldUid = oldUids[i];
							boolean skip = false;
							DataMap data = objects.getObject(oldUid);
							Iterator<String> it3 = data.keySet().iterator();
							while(it3.hasNext() && !skip)
							{
								String att = it3.next();
								String valStr = data.getString(att);
								if(valStr != null && valStr.startsWith("#") && valStr.endsWith("#"))
								{
									String oldForeignKey = valStr.substring(1, valStr.length() - 1);
									String newForeignUid = keyMap.get(oldForeignKey);
									if(newForeignUid != null)
										data.put(att, newForeignUid);
									else
										skip = true;
								}
								else if(valStr != null && valStr.startsWith("=") && valStr.endsWith("="))
								{
									String val = valStr.substring(1, valStr.length() - 1);
									if(val.equals("username"))
									{
										data.put(att, username);
									}
									else if(val.startsWith("date-")) 
									{
										long ms = Long.parseLong(val.substring(5)) * 86400000;
										Date date = new Date(System.currentTimeMillis() - ms);
										data.put(att, date);
									}
								}
							}
							if(!skip)
							{
								try
								{
									DataMap fbReqmap = new DataMap();
									DataMap filter = (DataMap)data.getCopy(); 
									filter.put("domain", domain);
									fbReqmap.put("action", "list");
									fbReqmap.put("object", objectname);
									fbReqmap.put("filter", filter);
									Payload request = new Payload(fbReqmap);
									request.metadata.put("token", token);
									request.metadata.put("session", session.getId());
									request.metadata.put("mime", "application/json");
									Payload response = firebus.requestService(objectService, request);
									DataMap fbRespmap = new DataMap(response.getString());
									DataList list = fbRespmap.getList("list");
									String newUid = null;
									if(list.size() > 0)
									{
										DataMap existing = list.getObject(0);
										newUid = existing.getString("uid");
										Logger.fine("rb.import.objectexists", new DataMap("object", objectname, "uid", newUid));
									}
									else
									{
										fbReqmap = new DataMap();
										fbReqmap.put("action", "create");
										fbReqmap.put("object", objectname);
										fbReqmap.put("data", data);
										fbReqmap.put("domain", domain);
										request = new Payload(fbReqmap);
										request.metadata.put("token", token);
										response = firebus.requestService(objectService, request);
										fbRespmap = new DataMap(response.getString());
										newUid = fbRespmap.getString("uid");
										Logger.fine("rb.import.objectimported", new DataMap("object", objectname, "uid", newUid));
									}
									String oldKey = objectname + "." + oldUid;
									keyMap.put(oldKey, newUid);
									objects.remove(oldUid);
								}
								catch(Exception e)
								{
									System.err.println("Error creating object : " + e.getMessage());
								}
							}
							else
							{
								skippedAtLeastOne = true;
								Logger.fine("rb.import.skipping", new DataMap("object", objectname, "uid", oldUid));
							}
						}				
					}
				} while(skippedAtLeastOne);
				Logger.info("rb.import.finish", new DataMap("file", fileurl));
			}
			else
			{
				Logger.severe("rb.import.filenotfound", new DataMap("file", fileurl), null);
			}
		}
		catch(Exception e)
		{
			Logger.severe("rb.import", new DataMap("file", fileurl), e);
		}
	}

	public static void main(String args[])
	{
		Firebus firebus = null;
		String objectService = null;
		String filepath = null;
		String domain = null;
		String token = null;
		
		for(int i = 0; i < args.length; i++) {
			String swt = args[i];
			if(swt.equals("-fb") && args.length > i + 1) {
				String param = args[++i];
				String[] parts = param.split("/");
				firebus = new Firebus(parts[0], parts[1]);
			}
			if(swt.equals("-os") && args.length > i + 1) {
				objectService = args[++i];
			}
			if(swt.equals("-d") && args.length > i + 1) {
				domain = args[++i];
			}
			if(swt.equals("-f") && args.length > i + 1) {
				filepath = args[++i];
			}
			if(swt.equals("-t") && args.length > i + 1) {
				token = args[++i];
			}
		}
		
		if(firebus == null)
			firebus = new Firebus();
		
		if(token != null && objectService != null && domain != null && filepath != null) {
			try {
			    ImportData id = new ImportData(firebus, token, objectService, domain, filepath);
				Thread.sleep(5000);
				id.importData();
			} catch(Exception e) {
				Logger.severe("rb.import.error", e);
			}
		} else {
			Logger.severe("rb.import.missingparams");
		}		
		firebus.close();
		Logger.info("rb.import.complete");
	}
}


