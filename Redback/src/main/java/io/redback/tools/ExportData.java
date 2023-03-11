package io.redback.tools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.data.DataList;
import io.firebus.data.DataMap;
import io.firebus.logging.Logger;

public class ExportData extends Thread
{
	protected Firebus firebus;
	protected String token;
	protected String objectService;
	protected String[] objectList;
	protected String domain;
	protected String filepath;
	
	public ExportData(Firebus fb, String t, String os, String[] ol, String d, String fp)
	{
		firebus = fb;
		token = t;
		objectService = os;
		objectList = ol;
		domain = d;
		filepath = fp;
	}
	
	public ExportData(Firebus fb, String t, String os, String set, String d, String fp)
	{
		firebus = fb;
		token = t;
		objectService = os;
		domain = d;
		filepath = fp;
		try {
			InputStream is = getClass().getResourceAsStream("/io/redback/data/_sets.json");
			if(is != null) {
				DataMap sets = new DataMap(is);
				is.close();
				DataList list = sets.getList(set);
				if(list != null) {
					objectList = new String[list.size()];
					for(int i = 0; i < list.size(); i++) 
						objectList[i] = list.getString(i);
				} else {
					objectList = new String[0];
				}
			} else {
				objectList = new String[0];
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
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
				Logger.info("rb.export", new DataMap("object", objectname));
				int newUID = 0;
				boolean hasMore = true;
				DataMap fbReqmap = new DataMap();
				fbReqmap.put("action", "list");
				fbReqmap.put("object", objectname);
				fbReqmap.put("filter", new DataMap("domain", domain));
				fbReqmap.put("options", new DataMap("addvalidation", true));
				fbReqmap.put("pagesize", 50);
				int page = 0;
				int count = 0;
				while(hasMore)
				{
					fbReqmap.put("page", page);
					Payload fbReq = new Payload(fbReqmap);
					fbReq.metadata.put("token", token);
					Payload fbResp = firebus.requestService(objectService, fbReq);
					DataMap dataResult = new DataMap(fbResp.getString());
					if(dataResult.containsKey("list"))
					{
						DataList list = dataResult.getList("list");
						for(int j = 0; j < list.size(); j++)
						{
							DataMap oldObj = list.getObject(j);
							if(domain == null || (domain != null && domain.equals(oldObj.getString("domain")))) {
								oldObjects.add(oldObj);
								keyMap.put(objectname + "." + oldObj.getString("uid"), "" + newUID);
								newUID++;
							}
						}
						count += list.size();
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
				Logger.info("rb.export.count", new DataMap("count", count));
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
								System.err.println("Broken link in " + oldKey + " for attribute " + att);
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
		String domain = null;
		String filepath = null;
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
			if(swt.equals("-ol") && args.length > i + 1) {
				objectList = args[++i];
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
		if(filepath == null)
			filepath = System.getProperty("user.dir") + File.separator + "export.json";
		
		if(token != null && objectService != null && objectList != null) {
			try {
				ExportData ed = new ExportData(firebus, token, objectService, objectList.split(","), domain, filepath);
				Thread.sleep(2000);
				ed.exportData();
			} catch(Exception e) {
				System.err.println("Error " + e.getMessage());
			}
		} else {
			Logger.severe("rb.export.missingparamter");
		}
		firebus.close();
	}
}


