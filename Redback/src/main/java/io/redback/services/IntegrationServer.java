package io.redback.services;

import java.util.List;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.information.ServiceInformation;
import io.firebus.utils.DataException;
import io.firebus.utils.DataList;
import io.firebus.utils.DataMap;
import io.redback.RedbackException;
import io.redback.security.Session;

public abstract class IntegrationServer extends AuthenticatedServiceProvider {

	public IntegrationServer(String n, DataMap c, Firebus f) {
		super(n, c, f);
	}

	public ServiceInformation getServiceInformation() {
		return null;
	}

	public Payload redbackAuthenticatedService(Session session, Payload payload) throws RedbackException {
		Payload responsePayload = null;
		try
		{
			DataMap request = new DataMap(payload.getString());
			DataMap response = null;
			String client = request.getString("client");
			String domain = request.getString("domain");
			String action = request.getString("action");
			String objectName = request.getString("object");
			DataMap options = request.getObject("options");
			if(action != null)
			{
				if(objectName != null)
				{
					if(action.equals("get"))
					{
						String uid = request.getString("uid");
						if(uid != null)
						{
							response = get(session, client, domain, objectName, uid, options);
						}
						else
						{
							throw new RedbackException("A 'get' action requires a 'uid' attribute");
						}
					}
					else if(action.equals("list"))
					{
						DataMap filter = request.getObject("filter");
						int page = request.containsKey("page") ? request.getNumber("page").intValue() : 0;
						int pageSize = request.containsKey("pagesize") ? request.getNumber("pagesize").intValue() : 50;
						List<DataMap> objects = list(session, client, domain, objectName, filter, options, page, pageSize);
						DataList respList = new DataList();
						for(DataMap map: objects) {
							respList.add(map);
						}
						response = new DataMap("list", respList);
					}					
					else if(action.equals("update"))
					{
						String uid = request.getString("uid");
						DataMap data = request.getObject("data");
						if(uid != null  &&  data != null)
						{
							response = update(session, client, domain, objectName, uid, data, options);
						}
						else
						{
							throw new RedbackException("An 'update' action requires a 'uid' and a 'data' attribute");
						}
					}
					else if(action.equals("create"))
					{
						DataMap data = request.getObject("data");
						response = create(session, client,domain, objectName, data, options);
					}
					else if(action.equals("delete"))
					{
						String uid = request.getString("uid");
						delete(session, client, domain, objectName, uid, options);
					}					
					else
					{
						throw new RedbackException("The '" + action + "' action is not valid as an object request");
					}
				}
				else
				{
					throw new RedbackException("No object was provided");
				}
			}
			else
			{
				throw new RedbackException("Requests must have at least an 'action' attribute");
			}	
			
			if(response != null) {
				responsePayload = new Payload(response.toString());
			} else {
				throw new RedbackException("Null response");
			}
		}
		catch(DataException e)
		{
			throw new RedbackException("Error in integration server", e);
		}		

		return responsePayload;		
	}

	public Payload redbackUnauthenticatedService(Session session, Payload payload) throws RedbackException {
		throw new RedbackException("All requests need to be authenticated");
	}

	
	protected abstract DataMap get(Session session, String client, String domain, String objectName, String uid, DataMap options) throws RedbackException;

	protected abstract List<DataMap> list(Session session, String client, String domain, String objectName, DataMap filter, DataMap options, int page, int pageSize) throws RedbackException;

	protected abstract DataMap update(Session session, String client, String domain, String objectName, String uid, DataMap data, DataMap options) throws RedbackException;

	protected abstract DataMap create(Session session, String client, String domain, String objectName, DataMap data, DataMap options) throws RedbackException;

	protected abstract void delete(Session session, String client, String domain, String objectName, String uid, DataMap options) throws RedbackException;


}
