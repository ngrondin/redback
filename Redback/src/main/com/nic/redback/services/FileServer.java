package com.nic.redback.services;

import java.util.ArrayList;
import java.util.UUID;
import java.util.logging.Logger;


import com.nic.firebus.Payload;
import com.nic.firebus.exceptions.FunctionErrorException;
import com.nic.firebus.exceptions.FunctionTimeoutException;
import com.nic.firebus.information.ServiceInformation;
import com.nic.firebus.utils.JSONException;
import com.nic.firebus.utils.JSONList;
import com.nic.firebus.utils.JSONObject;
import com.nic.redback.RedbackException;
import com.nic.redback.security.Session;

public class FileServer extends RedbackAuthenticatedService
{
	private Logger logger = Logger.getLogger("com.nic.redback");
	protected String metaDataDB;
	protected ArrayList<String> fileServices = new ArrayList<String>();
	protected String defaultFileService;

	public FileServer( JSONObject c)
	{
		super(c);
		metaDataDB = config.getString("metadatadb");
		JSONList list = c.getList("fileadapters");
		fileServices = new ArrayList<String>();
		for(int i = 0; i < list.size(); i++)
			fileServices.add(list.getString(i));
		defaultFileService = config.getString("defaultadapter");
	}
	
	public Payload unAuthenticatedService(Session session, Payload payload) throws FunctionErrorException
	{
		throw new FunctionErrorException("Redback File Service always needs to receive authenticated requests");
	}

	public Payload authenticatedService(Session session, Payload payload) throws FunctionErrorException
	{
		try
		{
			logger.info("Authenticated file service start");
			Payload response = null;
			if(payload.metadata.get("mime") != null  &&  payload.metadata.get("mime").startsWith("image/"))
			{
				response = putFile(payload);
			}
			else
			{
				JSONObject request = new JSONObject(payload.getString());
				String action = request.getString("action");
				if(action != null)
				{
					if(action.equals("get"))
					{
						response = getFile(request.getString("uid"));
					}
				}
			}
			if(response == null)
			{
				response = new Payload("{error:\"no action taken\"}");
			}
			logger.info("Authenticated file service finished");
			return response;
		}
		catch(RedbackException | JSONException e)
		{
			logger.severe(e.getMessage());
			throw new FunctionErrorException(e.getMessage());
		}
		
	}

	public ServiceInformation getServiceInformation()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public Payload getFile(String uid) throws RedbackException
	{
		Payload response = null;
		try
		{
			JSONObject req = new JSONObject("{object:\"rbfs_meta\", filter:{\"_id\":\"" + uid + "\"}}");
			JSONObject resp1 = request(metaDataDB, req);
			if(resp1.getList("result").size() > 0)
			{
				String fileAdapter = resp1.getString("result.0.endpoint");
				String fileName = resp1.getString("result.0.filename");
				response = firebus.requestService(fileAdapter, new Payload(fileName), 10000);
				response.metadata.put("mime", getMimeType(fileName));
			}
			else
			{
				throw new RedbackException("File not found");
			}
		}
		catch(JSONException | FunctionTimeoutException | FunctionErrorException e)
		{
			throw new RedbackException(e.getMessage());
		}
		return response;
	}
	
	public Payload putFile(Payload filePayload)
	{
		Payload response = new Payload();
		JSONObject respData = new JSONObject();
		UUID uid = UUID.randomUUID();
		String filename = uid.toString() + ".jpg";
		filePayload.metadata.put("filename", filename);
		firebus.publish(metaDataDB, new Payload("{object:\"rbfs_meta\", data:{_id:\"" + uid.toString() + "\", endpoint:\"" + defaultFileService + "\", filename:\"" + filename + "\"}}"));
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
