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
import io.redback.client.DataClient;
import io.redback.services.FileServer;
import io.redback.utils.CollectionConfig;
import io.redback.utils.RedbackFile;

public class RedbackFileServer extends FileServer 
{
	protected ArrayList<String> fileServices = new ArrayList<String>();
	protected String defaultFileService;
	protected String idGeneratorService; 
	protected String idName; 
	protected CollectionConfig collectionConfig;
	protected DataClient dataClient;
	
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
		collectionConfig = new CollectionConfig(c.getObject("collection"));
		dataClient = new DataClient(firebus, config.getString("dataservice"));
	}

	public RedbackFile getFile(String fileUid) throws DataException, RedbackException, FunctionErrorException, FunctionTimeoutException
	{
		RedbackFile file = null;
		DataMap resp = dataClient.getData(collectionConfig.getName(), new DataMap(collectionConfig.getField("fileuid"), fileUid));
		if(resp.getList("result").size() > 0)
		{
			DataMap fileInfo = collectionConfig.convertObjectToCanonical(resp.getList("result").getObject(0));
			String fileName = fileInfo.getString("filename");
			String mime = fileInfo.getString("mime");
			String username = fileInfo.getString("user");
			Date date = fileInfo.getDate("date");
			String fileAdapter = fileInfo.getString("endpoint");
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
		filter.put(collectionConfig.getField("relatedobject"), object);
		filter.put(collectionConfig.getField("relateduid"), uid);
		DataMap resp = dataClient.getData(collectionConfig.getName(), filter);
		List<RedbackFile> list = new ArrayList<RedbackFile>();
		for(int i = 0; i < resp.getList("result").size(); i++) 
		{
			DataMap fileInfo = collectionConfig.convertObjectToCanonical(resp.getList("result").getObject(i));
			String fileUid = fileInfo.getString("fileuid");
			String fileName = fileInfo.getString("filename");
			String mime = fileInfo.getString("mime");
			String username = fileInfo.getString("user");
			Date date = fileInfo.getDate("date");
			RedbackFile file = new RedbackFile(fileUid, fileName, mime, username, date, null);
			list.add(file);
		}
		return list;
	}

	public String putFile(String object, String uid, String fileName, String mime, String username, byte[] bytes) throws RedbackException, FunctionErrorException, FunctionTimeoutException
	{
		String fileUid = firebus.requestService(idGeneratorService, new Payload(idName)).getString();
		DataMap data = new DataMap();
		data.put(collectionConfig.getField("filename"), fileName);
		data.put(collectionConfig.getField("relatedobject"), object);
		data.put(collectionConfig.getField("relateduid"), uid);
		data.put(collectionConfig.getField("mime"), mime);
		data.put(collectionConfig.getField("user"), username);
		data.put(collectionConfig.getField("date"), new Date());
		dataClient.putData(collectionConfig.getName(), new DataMap(collectionConfig.getField("fileuid"), fileUid), data);

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

	public void clearCaches() 
	{
		
	}


}
