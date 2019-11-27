package com.nic.redback.services;

import java.util.List;
import java.util.logging.Logger;

import com.nic.firebus.Firebus;
import com.nic.firebus.Payload;
import com.nic.firebus.exceptions.FunctionErrorException;
import com.nic.firebus.information.ServiceInformation;
import com.nic.firebus.interfaces.Consumer;
import com.nic.firebus.utils.DataException;
import com.nic.firebus.utils.DataList;
import com.nic.firebus.utils.DataMap;
import com.nic.redback.RedbackException;
import com.nic.redback.managers.objectmanager.RedbackObject;
import com.nic.redback.security.Session;

public abstract class ObjectServer extends AuthenticatedService implements Consumer
{
	private Logger logger = Logger.getLogger("com.nic.redback");


	public ObjectServer(DataMap c, Firebus f)
	{
		super(c, f);
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
							RedbackObject object = get(session, objectName, uid);
							responseData = object.getJSON(addValidation, addRelated);
						}
						else
						{
							throw new FunctionErrorException("A 'get' action requires a 'uid' attribute");
						}
					}
					else if(action.equals("list"))
					{
						DataMap filter = request.getObject("filter");
						String attribute = request.getString("attribute");
						String uid = request.getString("uid");
						String search = request.getString("search");
						int page = request.containsKey("page") ? request.getNumber("page").intValue() : 0;
						if(filter != null || search != null || (uid != null && attribute != null))
						{
							List<RedbackObject> objects = null;
							if(uid != null && attribute != null && filter != null)
								objects = list(session, objectName, uid, attribute, filter, page, addRelated);
							else if(uid != null && attribute != null && search != null)
								objects = list(session, objectName, uid, attribute, search, page, addRelated);
							else if(filter != null)
								objects = list(session, objectName, filter, page, addRelated);
							else if(search != null)
								objects = list(session, objectName, search, page, addRelated);
							responseData = new DataMap();
							DataList list = new DataList();
							if(objects != null)
								for(int i = 0; i < objects.size(); i++)
									list.add(objects.get(i).getJSON(addValidation, addRelated));
							responseData.put("list", list);
						}
						else
						{
							throw new FunctionErrorException("A 'list' action requires either a filter, a search or a uid-attribute pair");
						}
					}
					else if(action.equals("update"))
					{
						String uid = request.getString("uid");
						DataMap data = request.getObject("data");
						if(uid != null  &&  data != null)
						{
							RedbackObject object = update(session, objectName, uid, data);
							responseData = object.getJSON(addValidation, addRelated);
						}
						else
						{
							throw new FunctionErrorException("An 'update' action requires a 'uid' and a 'data' attribute");
						}
					}
					else if(action.equals("create"))
					{
						DataMap data = request.getObject("data");
						String domain = request.getString("domain");
						RedbackObject object = create(session, objectName, domain, data);
						responseData = object.getJSON(addValidation, addRelated);
					}
					else if(action.equals("execute"))
					{
						String uid = request.getString("uid");
						String function = request.getString("function");
						DataMap data = request.getObject("data");
						if(uid != null)
						{
							RedbackObject object = execute(session, objectName, uid, function, data);
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
				else
				{
					throw new FunctionErrorException("No object was provided");
				}
			}
			else
			{
				throw new FunctionErrorException("Requests must have at least an 'action' attribute");
			}					
				
			response.setData(responseData.toString());
		}
		catch(DataException | RedbackException e)
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
			refreshConfigs();		
	}
	
	protected abstract RedbackObject get(Session session, String objectName, String uid) throws RedbackException;

	protected abstract List<RedbackObject> list(Session session, String objectName, DataMap filter, int page, boolean addRelated) throws RedbackException;

	protected abstract List<RedbackObject> list(Session session, String objectName, String search, int page, boolean addRelated) throws RedbackException;

	protected abstract List<RedbackObject> list(Session session, String objectName, String uid, String attribute, DataMap filter, int page, boolean addRelated) throws RedbackException;

	protected abstract List<RedbackObject> list(Session session, String objectName, String uid, String attribute, String search, int page, boolean addRelated) throws RedbackException;

	protected abstract RedbackObject update(Session session, String objectName, String uid, DataMap data) throws RedbackException;

	protected abstract RedbackObject create(Session session, String objectName, String domain, DataMap data) throws RedbackException;

	protected abstract RedbackObject execute(Session session, String objectName, String uid, String function, DataMap data) throws RedbackException;
	
	protected abstract void refreshConfigs();

}
