package com.nic.redback.tools;

import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.nic.firebus.Firebus;
import com.nic.firebus.Payload;
import com.nic.firebus.utils.DataList;
import com.nic.firebus.utils.DataMap;

public class ImportData 
{
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
	
	public void importData()
	{
		Map<String, String> keyMap = new HashMap<String, String>();
		try
		{
			URL fileUrl = new URL(filepath);
			InputStream is = fileUrl.openStream();
			DataMap input = new DataMap(is);
			is.close();
			boolean skippedAtLeastOne = false;
			while(!skippedAtLeastOne)
			{
				Iterator<String> it = input.keySet().iterator();
				while(it.hasNext())
				{
					String objectname = it.next();
					DataMap objects = input.getObject(objectname);
					int i = 1;
					String oldUid = "" + i;
					while(objects.containsKey(oldUid))
					{
						boolean skip = false;
						DataMap object = objects.getObject(oldUid);
						Iterator<String> it2 = object.keySet().iterator();
						while(it2.hasNext() && !skip)
						{
							String att = it2.next();
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
								//response = firebus.requestService(objectService, request);
								System.out.println("Create " + objectname);
								fbRespmap = new DataMap(response.getString());
								newUid = fbRespmap.getString("uid");
							}
							String oldKey = objectname + "." + oldUid;
							keyMap.put(oldKey, newUid);
							objects.remove(oldUid);
						}
						else
						{
							skippedAtLeastOne = true;
						}
						i++;
						oldUid = "" + i;
					}				
				}
			}
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
		String domain = null;
		if(args.length == 3)
		{
			firebus = new Firebus();
			objectService = args[0];
			domain = args[1];
			filepath = args[2];
		}
		else if(args.length == 5)
		{
			firebus = new Firebus(args[0], args[1]);
			objectService = args[2];
			domain = args[3];
			filepath = args[4];
		}
		
	    Algorithm algorithm = Algorithm.HMAC256("secret");
	    String token = JWT.create().withIssuer("io.firebus").withClaim("email", "julie").withExpiresAt(new Date((new Date()).getTime() + 3600000)).sign(algorithm);

	    ImportData id = new ImportData(firebus, token, objectService, domain, filepath);
		id.importData();
		firebus.close();
	}
}


