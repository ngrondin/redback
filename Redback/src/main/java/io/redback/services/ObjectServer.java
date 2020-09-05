package io.redback.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
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
import io.redback.utils.StringUtils;

public abstract class ObjectServer extends AuthenticatedServiceProvider 
{
	private Logger logger = Logger.getLogger("io.redback");


	public ObjectServer(String n, DataMap c, Firebus f)
	{
		super(n, c, f);
	}

	public Payload authenticatedService(Session session, Payload payload) throws FunctionErrorException
	{
		logger.finer("Object service start");
		Payload response = null;
		try
		{
			DataMap request = new DataMap(payload.getString());
			String action = request.getString("action");
			String objectName = request.getString("object");
			DataMap options = request.getObject("options");
			boolean addValidation = false;
			boolean addRelated = false;
			String format = "json";
			
			if(action != null)
			{
				if(objectName != null)
				{
					if(options != null)
					{
						addValidation = options.getBoolean("addvalidation");
						addRelated = options.getBoolean("addrelated");
						format = options.containsKey("format") ? options.getString("format") : "json";
					}
					
					if(action.equals("get"))
					{
						String uid = request.getString("uid");
						if(uid != null)
						{
							RedbackObject object = get(session, objectName, uid);
							response = formatResponse(object, format, addValidation, addRelated);
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
						DataMap sort = request.getObject("sort");
						int page = request.containsKey("page") ? request.getNumber("page").intValue() : 0;
						int pageSize = request.containsKey("pagesize") ? request.getNumber("pagesize").intValue() : 50;
						if(filter != null || search != null || (uid != null && attribute != null))
						{
							List<RedbackObject> objects = null;
							if(uid != null && attribute != null)
								objects = listRelated(session, objectName, uid, attribute, filter, search, sort, addRelated, page, pageSize);
							else
								objects = list(session, objectName, filter, search, sort, addRelated, page, pageSize);
							response = formatResponse(objects, format, addValidation, addRelated);
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
						DataMap sort = request.getObject("sort");
						int page = request.containsKey("page") ? request.getNumber("page").intValue() : 0;
						int pageSize = request.containsKey("pagesize") ? request.getNumber("pagesize").intValue() : 50;
						if(uid != null && attribute != null)
						{
							List<RedbackObject> objects = null;
							objects = listRelated(session, objectName, uid, attribute, filter, search, sort, addRelated, page, pageSize);
							response = formatResponse(objects, format, addValidation, addRelated);
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
							response = formatResponse(object, format, addValidation, addRelated);
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
						response = formatResponse(object, format, addValidation, addRelated);
					}
					else if(action.equals("delete"))
					{
						String uid = request.getString("uid");
						delete(session, objectName, uid);
						response = formatResponse(new DataMap("result", "ok"), format, addValidation, addRelated);
					}					
					else if(action.equals("execute"))
					{
						String uid = request.getString("uid");
						String function = request.getString("function");
						DataMap data = request.getObject("data");
						if(uid != null)
						{
							RedbackObject object = execute(session, objectName, uid, function, data);
							response = formatResponse(object, format, addValidation, addRelated);
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
						DataMap sort = request.getObject("sort");
						if(tuple != null && metrics != null)
						{
							List<RedbackAggregate> aggregates = aggregate(session, objectName, filter, tuple, metrics, sort, addRelated);
							response = formatResponse(aggregates, format, addValidation, addRelated);							
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
							response = formatResponse(new DataMap("result", "ok"), format, addValidation, addRelated);
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
		}
		catch(DataException | RedbackException e)
		{
			String errorMsg = buildErrorMessage(e);
			logger.severe(errorMsg);
			logger.severe(getStackTrace(e));
			throw new FunctionErrorException(errorMsg);
		}		

		logger.finer("Object service finish");
		return response;	
	}

	public Payload unAuthenticatedService(Session session, Payload payload)	throws FunctionErrorException
	{
		throw new FunctionErrorException("All requests need to be authenticated");
	}


	public ServiceInformation getServiceInformation()
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	protected Payload formatResponse(Object data, String format, boolean addValidation, boolean addRelated) throws RedbackException 
	{
		Payload response = null;
		if(data != null) {
			if(data instanceof List<?>) {
				List<?> list = (List<?>)data;
				if(list.size() > 0) {
					if(list.get(0) instanceof RedbackObject) {
						if(format.equals("csv")) {
							StringBuilder sb = new StringBuilder();
							boolean newLine = true;
							List<String> allCols = new ArrayList<String>();
							RedbackObject o = (RedbackObject)list.get(0);
							Set<String> cols = o.getObjectConfig().getAttributeNames();
							for(String col : cols) {
								allCols.add(col);
								sb.append((!newLine ? "," : "") + col);
								newLine = false;
								if(addRelated) {
									RedbackObject ro = o.getRelated(col);
									if(ro != null) {
										Set<String> subcols = ro.getObjectConfig().getAttributeNames();
										for(String subcol : subcols) {
											allCols.add(col + "." + subcol);
											sb.append((!newLine ? "," : "") + col + "." + subcol);
										}
									}
								}
							}
							for(int i = 0; i < list.size(); i++) {
								sb.append("\r\n");
								newLine = true;
								for(String col : allCols) {
									Object val = null;
									if(col.indexOf(".") == -1) {
										val = ((RedbackObject)list.get(i)).get(col).getObject();
									} else {
										RedbackObject ro = ((RedbackObject)list.get(i)).getRelated(col.substring(0, col.indexOf(".")));
										if(ro != null)
											val = ro.get(col.substring(col.indexOf(".") + 1)).getObject();
									}
									sb.append((!newLine ? "," : "") + StringUtils.convertObjectToCSVField(val));
									newLine = false;
								}
							}
							response = new Payload(sb.toString().getBytes());
							response.metadata.put("mime", "text/csv");
						} else {
							DataMap responseData = new DataMap();
							DataList respList = new DataList();
							for(int i = 0; i < list.size(); i++)
								respList.add(((RedbackObject)list.get(i)).getJSON(addValidation, addRelated));
							responseData.put("list", respList);
							response = new Payload(responseData.toString());
							response.metadata.put("mime", "application/json");
						}				
					} else if(list.get(0) instanceof RedbackAggregate) {
						if(format.equals("csv")) {
							StringBuilder sb = new StringBuilder();
							if(list.size() > 0) {
								RedbackAggregate a = (RedbackAggregate)list.get(0);
								Set<String> cols = a.getObjectConfig().getAttributeNames();
								for(String col : cols) 
									sb.append(col + ",");
								for(int i = 0; i < list.size(); i++) {
									for(String col : cols) 
										sb.append(((RedbackAggregate)list.get(i)).getString(col) + ",");
								}
							} else {
								
							}
							response = new Payload(sb.toString().getBytes());
							response.metadata.put("mime", "text/csv");
						} else {
							DataMap responseData = new DataMap();
							DataList respList = new DataList();
							for(int i = 0; i < list.size(); i++)
								respList.add(((RedbackAggregate)list.get(i)).getJSON(addRelated));
							responseData.put("list", respList);
							response = new Payload(responseData.toString());
							response.metadata.put("mime", "application/json");
						}							
					}
				} else {
					if(format.equals("csv")) {
						response = new Payload("");
						response.metadata.put("mime", "text/csv");
					} else {
						response = new Payload(new DataMap("list", new DataList()).toString());
						response.metadata.put("mime", "application/json");
					}
				}
			} else if(data instanceof RedbackObject) {
				RedbackObject object = (RedbackObject)data;
				response = new Payload(object.getJSON(addValidation, addRelated).toString());
				response.metadata.put("mime", "application/json");
			} else if(data instanceof RedbackAggregate) {
				RedbackAggregate agg = (RedbackAggregate)data;
				response = new Payload(agg.getJSON(addRelated).toString());
				response.metadata.put("mime", "application/json");
			} else if(data instanceof DataMap) {
				response = new Payload(((DataMap)data).toString());
				response.metadata.put("mime", "application/json");
			}
		} else {
			response = new Payload();
		}

		return response;
	}
	
	protected abstract RedbackObject get(Session session, String objectName, String uid) throws RedbackException;

	protected abstract List<RedbackObject> list(Session session, String objectName, DataMap filter, String search, DataMap sort, boolean addRelated, int page, int pageSize) throws RedbackException;

	protected abstract List<RedbackObject> listRelated(Session session, String objectName, String uid, String attribute, DataMap filter, String search, DataMap sort, boolean addRelated, int page, int pageSize) throws RedbackException;

	protected abstract RedbackObject update(Session session, String objectName, String uid, DataMap data) throws RedbackException;

	protected abstract RedbackObject create(Session session, String objectName, String uid, String domain, DataMap data) throws RedbackException;

	protected abstract void delete(Session session, String objectName, String uid) throws RedbackException;

	protected abstract RedbackObject execute(Session session, String objectName, String uid, String function, DataMap data) throws RedbackException;

	protected abstract RedbackObject execute(Session session, String function) throws RedbackException;

	protected abstract List<RedbackAggregate> aggregate(Session session, String objectName, DataMap filter, DataList tuple, DataList metrics, DataMap sort, boolean addRelated) throws RedbackException;

}
