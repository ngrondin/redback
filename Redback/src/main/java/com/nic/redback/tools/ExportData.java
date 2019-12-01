package com.nic.redback.tools;

import java.io.FileOutputStream;
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

public class ExportData extends Thread
{
	protected Firebus firebus;
	protected String token;
	protected String objectService;
	protected String[] objectList;
	protected String filepath;
	
	public ExportData(Firebus fb, String t, String os, String[] ol, String fp)
	{
		firebus = fb;
		token = t;
		objectService = os;
		objectList = ol;
		filepath = fp;
	}
	
	public void exportDataAsync()
	{
		start();
	}
	
	public void exportData()
	{
		run();
	}
	
	public void run()
	{
		Map<String, String> keyMap = new HashMap<String, String>();
		DataList oldObjects = new DataList();
		try
		{
			for(int i = 0; i < objectList.length; i++)
			{
				String objectname = objectList[i];
				int newUID = 0;
				boolean hasMore = true;
				DataMap fbReqmap = new DataMap();
				fbReqmap.put("action", "list");
				fbReqmap.put("object", objectname);
				fbReqmap.put("filter", new DataMap());
				fbReqmap.put("options", new DataMap("addvalidation", true));
				int page = 0;
				while(hasMore)
				{
					fbReqmap.put("page", page);
					Payload fbReq = new Payload(fbReqmap.toString());
					fbReq.metadata.put("token", token);
					Payload fbResp = firebus.requestService(objectService, fbReq);
					DataMap dataResult = new DataMap(fbResp.getString());
					if(dataResult.containsKey("list"))
					{
						DataList list = dataResult.getList("list");
						for(int j = 0; j < list.size(); j++)
						{
							DataMap oldObj = list.getObject(j);
							oldObjects.add(oldObj);
							keyMap.put(objectname + "." + oldObj.getString("uid"), "" + newUID);
							newUID++;
						}
						if(list.size() < 50)
							hasMore = false;
						else
							page++;
					}
					else
					{
						hasMore = false;
					}
				}
			}
			
			DataMap result = new DataMap();
			for(int i = 0; i < oldObjects.size(); i++)
			{
				DataMap oldObj = oldObjects.getObject(i);
				String objectname = oldObj.getString("objectname");
				if(!result.containsKey(objectname))
					result.put(objectname, new DataMap());
				DataMap newObj = new DataMap();
				String oldKey = objectname + "." + oldObj.getString("uid");
				Iterator<String> it = oldObj.getObject("data").keySet().iterator();
				while(it.hasNext())
				{
					String att = it.next();
					if(oldObj.getObject("validation").getObject(att).containsKey("related")  && oldObj.getObject("validation").getObject(att).getObject("related").getString("link").equals("uid"))
					{
						String relatedObj = oldObj.getObject("validation").getObject(att).getObject("related").getString("object");
						String oldForeignUID = oldObj.getObject("data").getString(att);
						if(oldForeignUID != null && !oldForeignUID.equals(""))
						{
							String newForeignUID = keyMap.get(relatedObj + "." + oldForeignUID);
							if(newForeignUID != null)
							{
								String newVal = "#" + relatedObj + "." + newForeignUID + "#";
								newObj.put(att, newVal);
							}
							else
							{
								throw new Exception("Broken link in " + oldKey + " for attribute " + att);
							}
						}
					}
					else if(!att.equals("uid"))
					{
						newObj.put(att, oldObj.getObject("data").get(att));
					}
				}
				result.getObject(objectname).put(keyMap.get(oldKey), newObj);					
			}
			
			FileOutputStream fos = new FileOutputStream(filepath);
			result.write(fos);
			fos.close();
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
		String objectList = null;
		String filepath = null;
		if(args.length == 3)
		{
			firebus = new Firebus();
			objectService = args[0];
			objectList = args[1];
			filepath = args[2];
		}
		else if(args.length == 5)
		{
			firebus = new Firebus(args[0], args[1]);
			objectService = args[2];
			objectList = args[3];
			filepath = args[4];
		}
		
	    Algorithm algorithm = Algorithm.HMAC256("secret");
	    String token = JWT.create().withIssuer("io.firebus").withClaim("email", "ngrondin78@gmail.com").withExpiresAt(new Date((new Date()).getTime() + 3600000)).sign(algorithm);
		
		ExportData ed = new ExportData(firebus, token, objectService, objectList.split(","), filepath);
		ed.exportData();
		firebus.close();
	}
}


