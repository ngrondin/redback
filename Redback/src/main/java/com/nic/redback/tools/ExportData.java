package com.nic.redback.tools;

import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.nic.firebus.Firebus;
import com.nic.firebus.Payload;
import com.nic.firebus.utils.DataList;
import com.nic.firebus.utils.DataMap;

public class ExportData 
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
	
	public void export()
	{
		Map<String, String> keyMap = new HashMap<String, String>();
		DataMap result = new DataMap();
		try
		{
			for(int i = 0; i < objectList.length; i++)
			{
				String objectname = objectList[i];
				DataMap objectResult = new DataMap();
				result.put(objectname, objectResult);
				int newUID = 0;
				boolean hasMore = true;
				while(hasMore)
				{
					DataMap fbReqmap = new DataMap();
					fbReqmap.put("action", "list");
					fbReqmap.put("object", objectname);
					fbReqmap.put("filter", new DataMap());
					fbReqmap.put("options", new DataMap("addvalidation", true));
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
							DataMap newObj = new DataMap();
							newUID++;
							String oldKey = objectname + "." + oldObj.getString("uid");
							String newKey = objectname + "." + newUID;
							keyMap.put(oldKey, newKey);
							Iterator<String> it = oldObj.getObject("data").keySet().iterator();
							while(it.hasNext())
							{
								String att = it.next();
								if(oldObj.getObject("validation").getObject(att).containsKey("related")  && oldObj.getObject("validation").getObject(att).getObject("related").getString("link").equals("uid"))
								{
									String relatedObj = oldObj.getObject("validation").getObject(att).getObject("related").getString("object");
									String foreignUID = oldObj.getObject("data").getString(att);
									String newForeignUID = keyMap.get(relatedObj + "." + foreignUID);
									if(newForeignUID != null)
									{
										String newVal = "#" + newForeignUID + "#";
										newObj.put(att, newVal);
									}
									else
									{
										throw new Exception("Broken link in " + oldKey + " for attribute " + att);
									}
								}
								else if(!att.equals("uid"))
								{
									newObj.put(att, oldObj.getObject("data").get(att));
								}
							}
							objectResult.put("" + newUID, newObj);
						}
						if(list.size() < 50)
							hasMore = false;
					}
					else
					{
						hasMore = false;
					}
				}
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
		
		String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyLCJlbWFpbCI6Im5ncm9uZGluNzhAZ21haWwuY29tIiwiZXhwIjoxOTIwNTExOTIyMDAwfQ.zQrN7sheh1PuO4fWru45dTPDtkLAqB9Q0WrwGO6yOeo";
		
		ExportData ed = new ExportData(firebus, token, objectService, objectList.split(","), filepath);
		ed.export();
		firebus.close();
	}
}


