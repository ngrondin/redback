package com.nic.redback.services.impl;

import java.util.ArrayList;
import java.util.UUID;

import com.nic.firebus.Firebus;
import com.nic.firebus.Payload;
import com.nic.firebus.exceptions.FunctionErrorException;
import com.nic.firebus.exceptions.FunctionTimeoutException;
import com.nic.firebus.utils.DataException;
import com.nic.firebus.utils.DataList;
import com.nic.firebus.utils.DataMap;
import com.nic.redback.RedbackException;
import com.nic.redback.services.FileServer;

public class RedbackFileServer extends FileServer
{
	protected ArrayList<String> fileServices = new ArrayList<String>();
	protected String defaultFileService;

	
	public RedbackFileServer(DataMap c, Firebus f) 
	{
		super(c, f);
		DataList list = c.getList("fileadapters");
		fileServices = new ArrayList<String>();
		for(int i = 0; i < list.size(); i++)
			fileServices.add(list.getString(i));
		defaultFileService = config.getString("defaultadapter");
	}

	public Payload getFile(String uid) throws RedbackException
	{
		Payload response = null;
		try
		{
			DataMap resp1 = getData("rbfs_meta", new DataMap("_id", uid));
			if(resp1.getList("result").size() > 0)
			{
				String fileAdapter = resp1.getString("result.0.endpoint");
				String fileName = resp1.getString("result.0.filename");
				response = firebus.requestService(fileAdapter, new Payload(fileName), 10000);
				response.metadata.put("mime", getMimeType(fileName));
			}
			else
			{
				error("File not found " + uid);
			}
		}
		catch(DataException | FunctionTimeoutException | FunctionErrorException e)
		{
			error("General error getting file + uid", e);
		}
		return response;
	}
	
	public Payload putFile(Payload filePayload)
	{
		Payload response = new Payload();
		DataMap respData = new DataMap();
		UUID uid = UUID.randomUUID();
		String filename = uid.toString() + ".jpg";
		filePayload.metadata.put("filename", filename);
		firebus.publish(dataService, new Payload("{object:\"rbfs_meta\", data:{_id:\"" + uid.toString() + "\", endpoint:\"" + defaultFileService + "\", filename:\"" + filename + "\"}}"));
		firebus.publish(defaultFileService, filePayload);
		respData.put("uid", uid.toString());
		respData.put("filename", filename);
		response.setData(respData.toString());
		return response;
	}
	

	public String getMimeType(String filename)
	{
		String type = "";
		if(filename.toLowerCase().endsWith(".jpg"))
			type = "image/jpg";
		return type;
	}

}
