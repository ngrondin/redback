package com.nic.redback.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

import javax.script.ScriptException;

import com.nic.firebus.Firebus;
import com.nic.firebus.Payload;
import com.nic.firebus.exceptions.FunctionErrorException;
import com.nic.firebus.information.ServiceInformation;
import com.nic.firebus.interfaces.Consumer;
import com.nic.firebus.utils.DataException;
import com.nic.firebus.utils.DataList;
import com.nic.firebus.utils.DataMap;
import com.nic.redback.RedbackAuthenticatedService;
import com.nic.redback.RedbackException;
import com.nic.redback.security.Session;
import com.nic.redback.services.objectserver.ObjectConfig;
import com.nic.redback.services.objectserver.ObjectManager;
import com.nic.redback.services.objectserver.RedbackObject;

public class ObjectServer extends RedbackAuthenticatedService implements Consumer
{
	private Logger logger = Logger.getLogger("com.nic.redback");
	protected ObjectManager objectManager;
	protected HashMap<String, ObjectConfig> objectConfigs;


	public ObjectServer(DataMap c)
	{
		super(c);
		objectManager = new ObjectManager(config);
	}

	public void setFirebus(Firebus fb)
	{
		super.setFirebus(fb);
		objectManager.setFirebus(fb);
	}
	
	public Payload authenticatedService(Session session, Payload payload) throws FunctionErrorException
	{
		logger.finer("Object service start");
		Payload response = new Payload();
		try
		{
			DataMap request = new DataMap(payload.getString());
			String action = request.getString("action");
			String objectName = request.getString("object");
			DataMap options = request.getObject("options");
			DataMap responseData = null;
			boolean addValidation = false;
			boolean addRelated = false;
			
			if(action != null)
			{
				if(objectName != null)
				{
					if(options != null)
					{
						addValidation = options.getBoolean("addvalidation");
						addRelated = options.getBoolean("addrelated");
					}
					
					if(action.equals("get"))
					{
						String uid = request.getString("uid");
						if(uid != null)
						{
							RedbackObject object = objectManager.getObject(session, objectName, uid);
							objectManager.commitCurrentTransaction();
							responseData = object.getJSON(addValidation, addRelated);
						}
						else
						{
							throw new FunctionErrorException("A 'get' action requires a 'uid' attribute");
						}
					}
					else if(action.equals("list"))
					{
						ArrayList<RedbackObject> objects = null;
						DataMap filter = request.getObject("filter");
						String attribute = request.getString("attribute");
						String uid = request.getString("uid");
						String search = request.getString("search");
						if(filter == null)
							filter = new DataMap();
						
						if(uid != null  &&  attribute != null)
							objects = objectManager.getObjectList(session, objectName, uid, attribute, filter, search);
						else
							objects = objectManager.getObjectList(session, objectName, filter, search);
						objectManager.commitCurrentTransaction();

						if(addRelated)
							objectManager.addRelatedBulk(session, objects);
						
						responseData = new DataMap();
						DataList list = new DataList();
						if(objects != null)
							for(int i = 0; i < objects.size(); i++)
								list.add(objects.get(i).getJSON(addValidation, addRelated));
						responseData.put("list", list);
					}
					else if(action.equals("update"))
					{
						String uid = request.getString("uid");
						DataMap data = request.getObject("data");
						if(uid != null  &&  data != null)
						{
							RedbackObject object = objectManager.updateObject(session, objectName, uid, data);
							objectManager.commitCurrentTransaction();
							if(object != null)
								responseData = object.getJSON(addValidation, addRelated);
							else
								throw new FunctionErrorException("No such object to update");
						}
						else
						{
							throw new FunctionErrorException("An 'update' action requires a 'uid' and a 'data' attribute");
						}
					}
					else if(action.equals("create"))
					{
						DataMap data = request.getObject("data");
						RedbackObject object = objectManager.createObject(session, objectName, data);
						objectManager.commitCurrentTransaction();
						responseData = object.getJSON(addValidation, addRelated);
					}
					else if(action.equals("execute"))
					{
						String uid = request.getString("uid");
						String function = request.getString("function");
						DataMap data = request.getObject("data");
						if(uid != null)
						{
							RedbackObject object = objectManager.executeFunction(session, objectName, uid, function, data);
							objectManager.commitCurrentTransaction();
							responseData = object.getJSON(addValidation, addRelated);
						}
						else
						{
							throw new FunctionErrorException("A 'create' action requires a 'uid' and a 'function' attribute");
						}
					}
					else
					{
						throw new FunctionErrorException("The '" + action + "' action is not valid as an object request");
					}
				}
			}
			else
			{
				throw new FunctionErrorException("Requests must have at least an 'action' attribute");
			}					
				
			response.setData(responseData.toString());
		}
		catch(ScriptException | DataException | RedbackException e)
		{
			String errorMsg = buildErrorMessage(e);
			logger.severe(errorMsg);
			logger.severe(getStackTrace(e));
			throw new FunctionErrorException(errorMsg);
		}		

		logger.finer("Object service finish");
		return response;	}

	public Payload unAuthenticatedService(Session session, Payload payload)	throws FunctionErrorException
	{
		throw new FunctionErrorException("All requests need to be authenticated");
	}


	public ServiceInformation getServiceInformation()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public void consume(Payload payload)
	{
		String msg = payload.getString();
		if(msg.equals("refreshconfig"))
			objectManager.refreshAllConfigs();		
	}
}
