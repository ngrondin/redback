package io.redback.services;

import java.util.List;
import java.util.logging.Logger;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.exceptions.FunctionErrorException;
import io.firebus.information.ServiceInformation;
import io.firebus.utils.DataException;
import io.firebus.utils.DataList;
import io.firebus.utils.DataMap;
import io.redback.RedbackException;
import io.redback.managers.objectmanager.RedbackAggregate;
import io.redback.managers.objectmanager.RedbackObject;
import io.redback.security.Session;

public abstract class ObjectServer extends AuthenticatedService 
{
	private Logger logger = Logger.getLogger("io.redback");


	public ObjectServer(String n, DataMap c, Firebus f)
	{
		super(n, c, f);
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
							if(uid != null && attribute != null)
								objects = listRelated(session, objectName, uid, attribute, filter, search, page, addRelated);
							else
								objects = list(session, objectName, filter, search, page, addRelated);
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
					else if(action.equals("listrelated"))
					{
						DataMap filter = request.getObject("filter");
						String attribute = request.getString("attribute");
						String uid = request.getString("uid");
						String search = request.getString("search");
						int page = request.containsKey("page") ? request.getNumber("page").intValue() : 0;
						if(uid != null && attribute != null)
						{
							List<RedbackObject> objects = null;
							objects = listRelated(session, objectName, uid, attribute, filter, search, page, addRelated);
							responseData = new DataMap();
							DataList list = new DataList();
							if(objects != null)
								for(int i = 0; i < objects.size(); i++)
									list.add(objects.get(i).getJSON(addValidation, addRelated));
							responseData.put("list", list);
						}
						else
						{
							throw new FunctionErrorException("A 'listrelated' action requires either a uid-attribute pair");
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
						String uid = request.getString("uid");
						String domain = request.getString("domain");
						DataMap data = request.getObject("data");
						RedbackObject object = create(session, objectName, uid, domain, data);
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
					else if(action.equals("aggregate"))
					{
						DataMap filter = request.getObject("filter");
						DataList tuple = request.getList("tuple");
						DataList metrics = request.getList("metrics");
						if(tuple != null && metrics != null)
						{
							List<RedbackAggregate> aggregates = aggregate(session, objectName, filter, tuple, metrics, addRelated);
							responseData = new DataMap();
							DataList list = new DataList();
							if(aggregates != null)
								for(int i = 0; i < aggregates.size(); i++)
									list.add(aggregates.get(i).getJSON(addRelated));
							responseData.put("list", list);
						}
						else
						{
							throw new FunctionErrorException("A 'aggregate' action requires a filter, a tuple and a metric");
						}
					}					
					else
					{
						throw new FunctionErrorException("The '" + action + "' action is not valid as an object request");
					}
				}
				else
				{
					if(action.equals("execute"))
					{
						String function = request.getString("function");
						if(function != null)
						{
							execute(session, function);
							responseData = new DataMap();
							responseData.put("result", "ok");
						}
						else
						{
							throw new FunctionErrorException("A global 'execute' action requires a 'function' attribute");
						}
					}
					else
					{
						throw new FunctionErrorException("No object was provided");
					}
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
	
	protected abstract RedbackObject get(Session session, String objectName, String uid) throws RedbackException;

	protected abstract List<RedbackObject> list(Session session, String objectName, DataMap filter, String search, int page, boolean addRelated) throws RedbackException;

	protected abstract List<RedbackObject> listRelated(Session session, String objectName, String uid, String attribute, DataMap filter, String search, int page, boolean addRelated) throws RedbackException;

	protected abstract RedbackObject update(Session session, String objectName, String uid, DataMap data) throws RedbackException;

	protected abstract RedbackObject create(Session session, String objectName, String uid, String domain, DataMap data) throws RedbackException;

	protected abstract RedbackObject execute(Session session, String objectName, String uid, String function, DataMap data) throws RedbackException;

	protected abstract RedbackObject execute(Session session, String function) throws RedbackException;

	protected abstract List<RedbackAggregate> aggregate(Session session, String objectName, DataMap filter, DataList tuple, DataList metrics, boolean addRelated) throws RedbackException;

}
