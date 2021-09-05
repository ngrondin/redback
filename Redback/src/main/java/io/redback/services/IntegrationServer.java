package io.redback.services;

import java.util.List;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.information.ServiceInformation;
import io.firebus.data.DataEntity;
import io.firebus.data.DataException;
import io.firebus.data.DataList;
import io.firebus.data.DataMap;
import io.redback.exceptions.RedbackException;
import io.redback.security.Session;
import io.redback.services.common.AuthenticatedServiceProvider;

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
			String client = request.getString("client");
			String domain = request.getString("domain");
			String action = request.getString("action");
			String get = request.getString("get");
			String objectName = request.getString("object");
			DataMap options = request.getObject("options");
			if(get != null) 
			{
				client = get.startsWith("/") ? get.substring(1) : get;
				String code = request.getString("code");
				String state = request.getString("state");
				if(state != null && state.equals("")) state = null;
				if(domain == null) domain = session.getUserProfile().getDefaultDomain();
				String response = null;
				if(code == null) {
					String redirect = getLoginUrl(session, client, domain, state);
					response = "<html><body><script>window.location='" + redirect + "';</script></body></html>";
				} else {
					exchangeAuthCode(session, client, domain, code);
					response = "<html><body>" + (state != null ? "<script>window.location='" + state + "';</script>" : "Logged in. Thank you.") + "</body></html>";
				}
				responsePayload = new Payload(response);
			}
			else if(action != null)
			{
				DataMap response = null;
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
						DataEntity data = request.get("data");
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
						DataEntity data = request.get("data");
						response = create(session, client,domain, objectName, data, options);
					}
					else if(action.equals("delete"))
					{
						String uid = request.getString("uid");
						delete(session, client, domain, objectName, uid, options);
						response = new DataMap("result", "ok");
					}		
					else
					{
						throw new RedbackException("The '" + action + "' action is not valid as an object request");
					}
				}
				else if(action.equals("clearcacheddata"))
				{
					clearCachedClientData(session, client, domain);
					response = new DataMap("result", "ok");
				}
				else
				{
					throw new RedbackException("No object was provided");
				}
				
				if(response != null) {
					responsePayload = new Payload(response.toString());
				} else {
					throw new RedbackException("Null response");
				}
			}
			else
			{
				throw new RedbackException("Requests must have at least an 'action' attribute");
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

	protected abstract DataMap update(Session session, String client, String domain, String objectName, String uid, DataEntity data, DataMap options) throws RedbackException;

	protected abstract DataMap create(Session session, String client, String domain, String objectName, DataEntity data, DataMap options) throws RedbackException;

	protected abstract void delete(Session session, String client, String domain, String objectName, String uid, DataMap options) throws RedbackException;

	protected abstract String getLoginUrl(Session session, String client, String domain, String state) throws RedbackException;
	
	protected abstract void exchangeAuthCode(Session session, String client, String domain, String code) throws RedbackException;
	
	protected abstract void clearCachedClientData(Session session, String client, String domain) throws RedbackException;

}
