package io.redback.tools;

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;

import io.firebus.Firebus;
import io.firebus.data.DataFilter;
import io.firebus.data.DataMap;
import io.firebus.logging.Logger;
import io.redback.client.ObjectClient;
import io.redback.client.RedbackObjectRemote;
import io.redback.security.Session;
import io.redback.security.UserProfile;

public class ImportData extends Thread
{
	protected Firebus firebus;
	protected Session session;
	protected ObjectClient objectClient;
	protected String domain;
	protected String fileurl;
	protected String username;
	protected boolean preLoadTarget = false;
	
	public ImportData(Firebus fb, String t, String os, String d, String fu)
	{
		firebus = fb;
		fileurl = fu;
		DecodedJWT jwt = JWT.decode(t);
		username = jwt.getClaim("email").asString();
		domain = d;
		objectClient = new ObjectClient(firebus, os);
		session = new Session(t, new UserProfile(new DataMap("username", username)));
		session.setDomainLock(domain);
		setName("rbImportThread");
	}
	
	public void setPreLoad(boolean p) 
	{
		preLoadTarget = p;
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
						List<RedbackObjectRemote> existingTargets = preLoadTarget ? objectClient.listAllObjects(session, objectname, new DataMap(), null, false) : null;
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
									List<RedbackObjectRemote> matchingTargets = null;
									if(existingTargets != null) {
										matchingTargets = new ArrayList<RedbackObjectRemote>();
										DataFilter filter = new DataFilter(data);
										for(RedbackObjectRemote ro: existingTargets) 
											if(filter.apply(ro.data.getObject("data")))
												matchingTargets.add(ro);
									} else {
										matchingTargets = objectClient.listObjects(session, objectname, data);
									}
									String newUid = null;
									if(matchingTargets.size() > 0)
									{
										newUid = matchingTargets.get(0).getUid();
										Logger.fine("rb.import.objectexists", new DataMap("object", objectname, "uid", newUid));
									}
									else
									{
										objectClient.createObject(session, objectname, domain, data, false);
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


