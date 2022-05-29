package io.redback.services;

import java.util.List;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.data.DataException;
import io.firebus.data.DataList;
import io.firebus.data.DataMap;
import io.firebus.information.ServiceInformation;
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
import io.redback.managers.objectmanager.requests.UpdateRequest;
import io.redback.security.Session;
import io.redback.services.common.AuthenticatedServiceProvider;

public abstract class ObjectServer extends AuthenticatedServiceProvider 
{

	public ObjectServer(String n, DataMap c, Firebus f)
	{
		super(n, c, f);
	}

	public Payload redbackAuthenticatedService(Session session, Payload payload) throws RedbackException
	{
		try {
			DataMap requestData = payload.getDataMap();
			String action = requestData.getString("action");
			
			if(action != null)
			{
				DataMap responseData = null;
				if(action.equals("get"))
				{
					GetRequest request = new GetRequest(requestData);
					RedbackObject object = get(session, request.objectName, request.uid);
					responseData = request.produceResponse(object);
				}
				else if(action.equals("list") && !requestData.containsKey("uid"))
				{
					ListRequest request = new ListRequest(requestData);
					List<RedbackObject> objects = list(session, request.objectName, request.filter, request.searchText, request.sort, request.addRelated, request.page, request.pageSize);
					responseData = request.produceResponse(objects);
				}
				else if(action.equals("listrelated") || (action.equals("list") && requestData.containsKey("uid")))
				{
					ListRelatedRequest request = new ListRelatedRequest(requestData);
					List<RedbackObject> objects = listRelated(session, request.objectName, request.uid, request.attribute, request.filter, request.searchText, request.sort, request.addRelated, request.page, request.pageSize);
					responseData = request.produceResponse(objects);
				}					
				else if(action.equals("update"))
				{
					UpdateRequest request = new UpdateRequest(requestData);
					RedbackObject object = update(session, request.objectName, request.uid, request.updateData);
					responseData = request.produceResponse(object);
				}
				else if(action.equals("create"))
				{
					CreateRequest request = new CreateRequest(requestData);
					RedbackObject object = create(session, request.objectName, request.uid, request.domain, request.initialData);
					responseData = request.produceResponse(object);
				}
				else if(action.equals("delete"))
				{
					DeleteRequest request = new DeleteRequest(requestData);
					delete(session, request.objectName, request.uid);
					responseData = request.produceResponse(null);
				}					
				else if(action.equals("execute"))
				{
					if(requestData.containsKey("object")) {
						ExecuteRequest request = new ExecuteRequest(requestData);
						RedbackObject object = execute(session, request.objectName, request.uid, request.function, request.param);
						responseData = request.produceResponse(object);
					} else {
						ExecuteGlobalRequest request = new ExecuteGlobalRequest(requestData);
						execute(session, request.function, request.param);
						responseData = request.produceResponse(null);
					}
				}
				else if(action.equals("aggregate"))
				{
					AggregateRequest request = new AggregateRequest(requestData);
					List<RedbackAggregate> aggregates = aggregate(session, request.objectName, request.filter, request.searchText, request.tuple, request.metrics, request.sort, request.base, request.addRelated, request.page, request.pageSize);
					responseData = request.produceResponse(aggregates);
				}
				else if(action.equals("multi")) 
				{
					MultiRequest request = new MultiRequest(requestData);
					MultiResponse mr = multi(session, request);
					responseData = request.produceResponse(mr);
				}
				else if(action.equals("noop")) 
				{
					//Do nothing, just used to log the call;
					responseData = new DataMap("result", "ok");
				}
				else
				{
					throw new RedbackException("The '" + action + "' action is not valid as an object request");
				}
				Payload response = new Payload(responseData);
				response.metadata.put("mime", "application/json");
				return response;
			}
			else
			{
				throw new RedbackException("Requests must have at least an 'action' attribute");
			}	
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

	protected abstract RedbackObject get(Session session, String objectName, String uid) throws RedbackException;

	protected abstract List<RedbackObject> list(Session session, String objectName, DataMap filter, String search, DataMap sort, boolean addRelated, int page, int pageSize) throws RedbackException;

	protected abstract List<RedbackObject> listRelated(Session session, String objectName, String uid, String attribute, DataMap filter, String search, DataMap sort, boolean addRelated, int page, int pageSize) throws RedbackException;

	protected abstract RedbackObject update(Session session, String objectName, String uid, DataMap data) throws RedbackException;

	protected abstract RedbackObject create(Session session, String objectName, String uid, String domain, DataMap data) throws RedbackException;

	protected abstract void delete(Session session, String objectName, String uid) throws RedbackException;

	protected abstract RedbackObject execute(Session session, String objectName, String uid, String function, DataMap param) throws RedbackException;

	protected abstract void execute(Session session, String function, DataMap param) throws RedbackException;

	protected abstract List<RedbackAggregate> aggregate(Session session, String objectName, DataMap filter, String searchText, DataList tuple, DataList metrics, DataMap sort, DataList base, boolean addRelated, int page, int pageSize) throws RedbackException;

	protected abstract MultiResponse multi(Session session, MultiRequest request) throws RedbackException;

}
