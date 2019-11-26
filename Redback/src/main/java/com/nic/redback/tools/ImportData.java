package com.nic.redback.tools;

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.nic.firebus.Firebus;
import com.nic.firebus.Payload;
import com.nic.firebus.utils.DataList;
import com.nic.firebus.utils.DataMap;

public class ImportData 
{
	protected Firebus firebus;
	protected String token;
	protected String objectService;
	protected String filepath;
	
	public ImportData(Firebus fb, String t, String os, String fp)
	{
		firebus = fb;
		token = t;
		objectService = os;
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
			Iterator<String> it = input.keySet().iterator();
			while(it.hasNext())
			{
				String objectname = it.next();
				DataMap objects = input.getObject(objectname);
				int i = 1;
				String oldUid = "" + i;
				while(objects.containsKey(oldUid))
				{
					DataMap object = objects.getObject(oldUid);
					Iterator<String> it2 = object.keySet().iterator();
					while(it2.hasNext())
					{
						String att = it2.next();
						String valStr = object.getString(att);
						if(valStr.startsWith("#") && valStr.endsWith("#"))
						{
							String oldForeignKey = valStr.substring(1, valStr.length() - 1);
							String newForeignUid = keyMap.get(oldForeignKey);
							object.put(att, newForeignUid);
						}
					}
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
						request = new Payload(fbReqmap.toString());
						request.metadata.put("token", token);
						response = firebus.requestService(objectService, request);
						fbRespmap = new DataMap(response.getString());
						newUid = fbRespmap.getString("uid");
					}
					String oldKey = objectname + "." + oldUid;
					keyMap.put(oldKey, newUid);
					i++;
					oldUid = "" + i;
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
		if(args.length == 2)
		{
			firebus = new Firebus();
			objectService = args[0];
			filepath = args[1];
		}
		else if(args.length == 4)
		{
			firebus = new Firebus(args[0], args[1]);
			objectService = args[2];
			filepath = args[3];
		}
		
		String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyLCJlbWFpbCI6Im5ncm9uZGluNzhAZ21haWwuY29tIiwiZXhwIjoxOTIwNTExOTIyMDAwfQ.zQrN7sheh1PuO4fWru45dTPDtkLAqB9Q0WrwGO6yOeo";
		
		ImportData id = new ImportData(firebus, token, objectService, filepath);
		id.importData();
		firebus.close();
	}
}


