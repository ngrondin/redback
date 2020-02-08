package io.redback.services.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.exceptions.FunctionErrorException;
import io.firebus.exceptions.FunctionTimeoutException;
import io.firebus.utils.DataException;
import io.firebus.utils.DataList;
import io.firebus.utils.DataMap;
import io.redback.RedbackException;
import io.redback.services.FileServer;
import io.redback.utils.RedbackFile;

public class RedbackFileServer extends FileServer
{
	protected ArrayList<String> fileServices = new ArrayList<String>();
	protected String defaultFileService;
	protected String idGeneratorService; 
	protected String idName; 
	protected DataMap collection;
	
	public RedbackFileServer(String n, DataMap c, Firebus f) 
	{
		super(n, c, f);
		idGeneratorService = c.getString("idgeneratorservice");
		idName = c.getString("idname");
		DataList list = c.getList("fileadapters");
		fileServices = new ArrayList<String>();
		for(int i = 0; i < list.size(); i++)
			fileServices.add(list.getString(i));
		defaultFileService = config.getString("defaultadapter");
		collection = c.getObject("collection");
	}

	public RedbackFile getFile(String fileUid) throws DataException, RedbackException, FunctionErrorException, FunctionTimeoutException
	{
		RedbackFile file = null;
		DataMap resp = getData(collection.getString("name"), new DataMap(collection.getString("map.uid"), fileUid));
		if(resp.getList("result").size() > 0)
		{
			String fileName = resp.getString("result.0." + collection.getString("map.filename"));
			String mime = resp.getString("result.0." + collection.getString("map.mime"));
			String username = resp.getString("result.0." + collection.getString("map.user"));
			Date date = resp.getDate("result.0." + collection.getString("map.date"));
			String fileAdapter = resp.getString("result.0." + collection.getString("map.endpoint"));
			if(fileAdapter == null)
				fileAdapter = defaultFileService;
			Payload filePayload = firebus.requestService(fileAdapter, new Payload(fileUid), 10000);
			file = new RedbackFile(fileUid, fileName, mime, username, date, filePayload.getBytes());
		}
		else
		{
			error("File not found " + fileUid);
		}
		return file;
	}
	
	public List<RedbackFile> listFilesFor(String object, String uid) throws DataException, RedbackException, FunctionErrorException, FunctionTimeoutException
	{
		DataMap filter = new DataMap();
		filter.put(collection.getString("map.relatedobject"), object);
		filter.put(collection.getString("map.relateduid"), uid);
		DataMap resp = getData(collection.getString("name"), filter);
		List<RedbackFile> list = new ArrayList<RedbackFile>();
		for(int i = 0; i < resp.getList("result").size(); i++) 
		{
			DataMap fileInfo = resp.getList("result").getObject(i);
			String fileUid = fileInfo.getString(collection.getString("map.uid"));
			String fileName = fileInfo.getString(collection.getString("map.filename"));
			String mime = fileInfo.getString(collection.getString("map.mime"));
			String username = fileInfo.getString(collection.getString("map.user"));
			Date date = fileInfo.getDate(collection.getString("map.date"));
			RedbackFile file = new RedbackFile(fileUid, fileName, mime, username, date, null);
			list.add(file);
		}
		return list;
	}

	public String putFile(String object, String uid, String fileName, String mime, String username, byte[] bytes) throws RedbackException, FunctionErrorException, FunctionTimeoutException
	{
		String fileUid = firebus.requestService(idGeneratorService, new Payload(idName)).getString();
		DataMap data = new DataMap();
		data.put(collection.getString("map.filename"), fileName);
		data.put(collection.getString("map.relatedobject"), object);
		data.put(collection.getString("map.relateduid"), uid);
		data.put(collection.getString("map.mime"), mime);
		data.put(collection.getString("map.user"), username);
		data.put(collection.getString("map.date"), new Date());
		publishData(collection.getString("name"), new DataMap(collection.getString("map.uid"), fileUid), data);

		Payload filePayload = new Payload(bytes);
		filePayload.metadata.put("filename", fileUid);
		firebus.publish(defaultFileService, filePayload);
		
		return fileUid;
	}


	public String getMimeType(String filename)
	{
		String type = "";
		if(filename.toLowerCase().endsWith(".jpg"))
			type = "image/jpg";
		return type;
	}


}
