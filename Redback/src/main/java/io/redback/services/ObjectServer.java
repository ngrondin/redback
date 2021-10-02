package io.redback.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.information.ServiceInformation;
import io.firebus.data.DataException;
import io.firebus.data.DataList;
import io.firebus.data.DataMap;
import io.redback.exceptions.RedbackException;
import io.redback.managers.objectmanager.RedbackAggregate;
import io.redback.managers.objectmanager.RedbackObject;
import io.redback.managers.objectmanager.requests.AggregateRequest;
import io.redback.managers.objectmanager.requests.CreateRequest;
import io.redback.managers.objectmanager.requests.DeleteRequest;
import io.redback.managers.objectmanager.requests.ExecuteGlobalRequest;
import io.redback.managers.objectmanager.requests.ExecuteRequest;
import io.redback.managers.objectmanager.requests.GetRequest;
import io.redback.managers.objectmanager.requests.ListRelatedRequest;
import io.redback.managers.objectmanager.requests.ListRequest;
import io.redback.managers.objectmanager.requests.MultiRequest;
import io.redback.managers.objectmanager.requests.MultiResponse;
import io.redback.managers.objectmanager.requests.ObjectRequest;
import io.redback.managers.objectmanager.requests.UpdateRequest;
import io.redback.security.Session;
import io.redback.services.common.AuthenticatedServiceProvider;
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
			DataMap requestData = new DataMap(payload.getString());
			String action = requestData.getString("action");
			DataMap options = requestData.getObject("options");
			String format = "json";
			
			if(action != null)
			{
				if(options != null)
					format = options.containsKey("format") ? options.getString("format") : "json";
				
				if(action.equals("get"))
				{
					GetRequest request = new GetRequest(requestData);
					RedbackObject object = get(session, request.objectName, request.uid);
					response = formatResponse(object, format, request.addValidation, request.addRelated);
				}
				else if(action.equals("list") && !requestData.containsKey("uid"))
				{
					ListRequest request = new ListRequest(requestData);
					List<RedbackObject> objects = list(session, request.objectName, request.filter, request.searchText, request.sort, request.addRelated, request.page, request.pageSize);
					response = formatResponse(objects, format, request.addValidation, request.addRelated);
				}
				else if(action.equals("listrelated") || (action.equals("list") && requestData.containsKey("uid")))
				{
					ListRelatedRequest request = new ListRelatedRequest(requestData);
					List<RedbackObject> objects = listRelated(session, request.objectName, request.uid, request.attribute, request.filter, request.searchText, request.sort, request.addRelated, request.page, request.pageSize);
					response = formatResponse(objects, format, request.addValidation, request.addRelated);
				}					
				else if(action.equals("update"))
				{
					UpdateRequest request = new UpdateRequest(requestData);
					RedbackObject object = update(session, request.objectName, request.uid, request.updateData);
					response = formatResponse(object, format, request.addValidation, request.addRelated);
				}
				else if(action.equals("create"))
				{
					CreateRequest request = new CreateRequest(requestData);
					RedbackObject object = create(session, request.objectName, request.uid, request.domain, request.initialData);
					response = formatResponse(object, format, request.addValidation, request.addRelated);
				}
				else if(action.equals("delete"))
				{
					DeleteRequest request = new DeleteRequest(requestData);
					delete(session, request.objectName, request.uid);
					response = formatResponse(new DataMap("result", "ok"), format, false, false);
				}					
				else if(action.equals("execute"))
				{
					if(requestData.containsKey("object")) {
						ExecuteRequest request = new ExecuteRequest(requestData);
						RedbackObject object = execute(session, request.objectName, request.uid, request.function, request.param);
						response = formatResponse(object, format, request.addValidation, request.addRelated);
					} else {
						ExecuteGlobalRequest request = new ExecuteGlobalRequest(requestData);
						RedbackObject object = execute(session, request.function, request.param);
						response = formatResponse(object, format, request.addValidation, request.addRelated);
					}
				}
				else if(action.equals("aggregate"))
				{
					AggregateRequest request = new AggregateRequest(requestData);
					List<RedbackAggregate> aggregates = aggregate(session, request.objectName, request.filter, request.searchText, request.tuple, request.metrics, request.sort, request.base, request.addRelated, request.page, request.pageSize);
					response = formatResponse(aggregates, format, request.addValidation, request.addRelated);							
				}
				else if(action.equals("multi")) 
				{
					if(format.equals("json")) {
						MultiRequest request = new MultiRequest(requestData);
						MultiResponse mr = multi(session, request);
						DataMap respMap = new DataMap();
						for(String key: mr.getKeys()) {
							ObjectRequest originalRequest = request.getRequest(key);
							Payload resp = formatResponse(mr.getResponse(key), format, originalRequest.addRelated, originalRequest.addValidation);
							respMap.put(key, new DataMap(resp.getString())); //TODO: This is not ideal, should clean this up.
						}
						response = new Payload(respMap.toString());
						response.metadata.put("mime", "application/json");
					} else {
						throw new RedbackException("Multi requests can only produce JSON responses");
					}
				}
				else if(action.equals("noop")) 
				{
					//Do nothing, just used to log the call;
					response = new Payload(new DataMap("result", "ok").toString());
					response.metadata.put("mime", "application/json");
				}
				else
				{
					throw new RedbackException("The '" + action + "' action is not valid as an object request");
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

	protected abstract List<RedbackAggregate> aggregate(Session session, String objectName, DataMap filter, String searchText, DataList tuple, DataList metrics, DataMap sort, DataList base, boolean addRelated, int page, int pageSize) throws RedbackException;

	protected abstract MultiResponse multi(Session session, MultiRequest request) throws RedbackException;

}
