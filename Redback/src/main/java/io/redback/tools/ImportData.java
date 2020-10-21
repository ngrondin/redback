package io.redback.tools;

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.utils.DataList;
import io.firebus.utils.DataMap;

public class ImportData extends Thread
{
	
	protected Firebus firebus;
	protected String token;
	protected String objectService;
	protected String domain;
	protected String fileurl;
	
	public ImportData(Firebus fb, String t, String os, String d, String fu)
	{
		firebus = fb;
		token = t;
		objectService = os;
		domain = d;
		fileurl = fu;
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
			System.out.println("Starting to import " + fileurl);
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
							}
							if(!skip)
							{
								try
								{
									DataMap fbReqmap = new DataMap();
									fbReqmap.put("action", "list");
									fbReqmap.put("object", objectname);
									fbReqmap.put("filter", data);
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
										System.out.println("Found existing object " + objectname + "." + newUid);
									}
									else
									{
										fbReqmap = new DataMap();
										fbReqmap.put("action", "create");
										fbReqmap.put("object", objectname);
										fbReqmap.put("data", data);
										fbReqmap.put("domain", domain);
										request = new Payload(fbReqmap.toString());
										request.metadata.put("token", token);
										response = firebus.requestService(objectService, request);
										fbRespmap = new DataMap(response.getString());
										newUid = fbRespmap.getString("uid");
										System.out.println("Imported object " + objectname + "." + newUid);
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
								System.out.println("Skipping object " + objectname + "." + oldUid + " for the moment");
							}
						}				
					}
				} while(skippedAtLeastOne);
				System.out.println("Finished importing " + fileurl);
			}
			else
			{
				System.err.println("File was not found : " + fileurl);
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
				System.err.println("Error " + e.getMessage());
			}
		} else {
			System.out.println("Some parameters are missing");
		}		
		firebus.close();
		System.out.println("import ended");
	}
}


