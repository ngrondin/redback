package io.redback.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import io.firebus.Firebus;
import io.firebus.Payload;
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

	public ObjectServer(String n, DataMap c, Firebus f)
	{
		super(n, c, f);
	}

	public Payload redbackAuthenticatedService(Session session, Payload payload) throws RedbackException
	{
		try {
			Payload response = null;
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
							throw new RedbackException("A 'get' action requires a 'uid' attribute");
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
							throw new RedbackException("A 'list' action requires either a filter, a search or a uid-attribute pair");
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
							throw new RedbackException("A 'listrelated' action requires either a uid-attribute pair");
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
							throw new RedbackException("An 'update' action requires a 'uid' and a 'data' attribute");
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
						DataMap param = request.containsKey("param") ? request.getObject("param") : request.containsKey("data") ? request.getObject("data") : null;
						if(uid != null)
						{
							RedbackObject object = execute(session, objectName, uid, function, param);
							response = formatResponse(object, format, addValidation, addRelated);
						}
						else
						{
							throw new RedbackException("A 'create' action requires a 'uid' and a 'function' attribute");
						}
					}
					else if(action.equals("aggregate"))
					{
						DataMap filter = request.getObject("filter");
						String searchText = request.getString("search");
						DataList tuple = request.getList("tuple");
						DataList metrics = request.getList("metrics");
						DataMap sort = request.getObject("sort");
						int page = request.containsKey("page") ? request.getNumber("page").intValue() : 0;
						int pageSize = request.containsKey("pagesize") ? request.getNumber("pagesize").intValue() : 50;
						if(tuple != null && metrics != null)
						{
							List<RedbackAggregate> aggregates = aggregate(session, objectName, filter, searchText, tuple, metrics, sort, addRelated, page, pageSize);
							response = formatResponse(aggregates, format, addValidation, addRelated);							
						}
						else
						{
							throw new RedbackException("A 'aggregate' action requires a filter, a tuple and a metric");
						}
					}					
					else
					{
						throw new RedbackException("The '" + action + "' action is not valid as an object request");
					}
				}
				else
				{
					if(action.equals("execute"))
					{
						String function = request.getString("function");
						DataMap param = request.getObject("param");
						if(function != null)
						{
							execute(session, function, param);
							response = formatResponse(new DataMap("result", "ok"), format, addValidation, addRelated);
						}
						else
						{
							throw new RedbackException("A global 'execute' action requires a 'function' attribute");
						}
					}
					else
					{
						throw new RedbackException("No object was provided");
					}
				}
			}
			else
			{
				throw new RedbackException("Requests must have at least an 'action' attribute");
			}	
			return response;	
		} catch(DataException e) {
			throw new RedbackException("Error in object server", e);
		}
	}

	public Payload redbackUnauthenticatedService(Session session, Payload payload) throws RedbackException
	{
		throw new RedbackException("All requests need to be authenticated");
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

	protected abstract RedbackObject execute(Session session, String objectName, String uid, String function, DataMap param) throws RedbackException;

	protected abstract RedbackObject execute(Session session, String function, DataMap param) throws RedbackException;

	protected abstract List<RedbackAggregate> aggregate(Session session, String objectName, DataMap filter, String searchText, DataList tuple, DataList metrics, DataMap sort, boolean addRelated, int page, int pageSize) throws RedbackException;

}
