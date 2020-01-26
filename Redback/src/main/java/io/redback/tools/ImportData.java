package io.redback.tools;

import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.utils.DataList;
import io.firebus.utils.DataMap;

public class ImportData extends Thread
{
	private Logger logger = Logger.getLogger("io.redback");
	
	protected Firebus firebus;
	protected String token;
	protected String objectService;
	protected String domain;
	protected String filepath;
	
	public ImportData(Firebus fb, String t, String os, String d, String fp)
	{
		firebus = fb;
		token = t;
		objectService = os;
		domain = d;
		filepath = fp;
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
			logger.info("Starting to import " + filepath);
			URL fileUrl = new URL(filepath);
			InputStream is = fileUrl.openStream();
			DataMap input = new DataMap(is);
			is.close();
			boolean skippedAtLeastOne = true;
			while(skippedAtLeastOne)
			{
				Iterator<String> it = input.keySet().iterator();
				while(it.hasNext())
				{
					String objectname = it.next();
					DataMap objects = input.getObject(objectname);
					String[] keys = objects.keySet().toArray(new String[0]);
					for(int i = 0; i < keys.length; i++)
					{
						String oldUid = keys[i];
						boolean skip = false;
						DataMap object = objects.getObject(oldUid);
						Iterator<String> it3 = object.keySet().iterator();
						while(it3.hasNext() && !skip)
						{
							String att = it3.next();
							String valStr = object.getString(att);
							if(valStr != null && valStr.startsWith("#") && valStr.endsWith("#"))
							{
								String oldForeignKey = valStr.substring(1, valStr.length() - 1);
								String newForeignUid = keyMap.get(oldForeignKey);
								if(newForeignUid != null)
									object.put(att, newForeignUid);
								else
									skip = true;
							}
						}
						if(!skip)
						{
							DataMap fbReqmap = new DataMap();
							fbReqmap.put("action", "list");
							fbReqmap.put("object", objectname);
							fbReqmap.put("filter", object);
							Payload request = new Payload(fbReqmap.toString());
							request.metadata.put("token", token);
							Payload response = firebus.requestService(objectService, request);
							DataMap fbRespmap = new DataMap(response.getString());
							DataList list = fbRespmap.getList("list");
							String newUid = null;
							if(list.size() > 0)
							{
								DataMap existing = list.getObject(0);
								newUid = existing.getString("uid");
								logger.fine("Found existing object " + objectname + "." + newUid);
							}
							else
							{
								fbReqmap = new DataMap();
								fbReqmap.put("action", "create");
								fbReqmap.put("object", objectname);
								fbReqmap.put("data", object);
								fbReqmap.put("domain", domain);
								request = new Payload(fbReqmap.toString());
								request.metadata.put("token", token);
								response = firebus.requestService(objectService, request);
								fbRespmap = new DataMap(response.getString());
								newUid = fbRespmap.getString("uid");
								logger.fine("Created object " + objectname + "." + newUid);
							}
							String oldKey = objectname + "." + oldUid;
							keyMap.put(oldKey, newUid);
							objects.remove(oldUid);
						}
						else
						{
							skippedAtLeastOne = true;
							logger.fine("Skipping object " + objectname + "." + oldUid + " for the moment");
						}
					}				
				}
			}
			logger.info("Finished importing " + filepath);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	public static void main(String args[])
	{
		Firebus firebus = null;
		String objectService = null;
		String filepath = null;
		String user = null;
		String domain = null;
		if(args.length == 4)
		{
			firebus = new Firebus();
			objectService = args[0];
			user = args[1];
			domain = args[2];
			filepath = args[3];
		}
		else if(args.length == 6)
		{
			firebus = new Firebus(args[0], args[1]);
			objectService = args[2];
			user = args[3];
			domain = args[4];
			filepath = args[5];
		}
		
	    Algorithm algorithm = Algorithm.HMAC256("secret");
	    String token = JWT.create().withIssuer("io.firebus").withClaim("email", user).withExpiresAt(new Date((new Date()).getTime() + 3600000)).sign(algorithm);

	    ImportData id = new ImportData(firebus, token, objectService, domain, filepath);
		id.importData();
		firebus.close();
	}
}


