package io.redback.client;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.StreamEndpoint;
import io.firebus.data.DataException;
import io.firebus.data.DataList;
import io.firebus.data.DataMap;
import io.redback.exceptions.RedbackException;
import io.redback.security.Session;

public class Client {

	protected Firebus firebus;
	protected String serviceName;
	protected int defaultTimeout = 10000;
	
	public Client(Firebus fb, String sn)
	{
		firebus = fb;
		serviceName = sn;
	}
	
	public void setTimeout(int t) {
		defaultTimeout = t;
	}
	
	protected DataMap requestDataMap(DataMap req) throws RedbackException
	{
		return requestDataMap(null, req, false);
	}
	
	protected DataMap requestDataMap(Session session, DataMap req) throws RedbackException
	{
		return requestDataMap(session, req, false);
	}
	
	protected DataMap requestDataMap(Session session, DataMap req, boolean async) throws RedbackException
	{
		Object resp = requestObject(session, req, async);
		if(resp instanceof DataMap) {
			return (DataMap)resp;
		} else if(resp instanceof String) {
			try {
				DataMap map = new DataMap((String)resp);
				return map;
			} catch (DataException e) {
				throw new RedbackException("Return data from " + serviceName + " is not a DataMap");
			}
		} else {
			throw new RedbackException("Return data from " + serviceName + " is not a DataMap");
		}
	}
	
	protected DataList requestDataList(DataMap req) throws RedbackException
	{
		return requestDataList(null, req, false);
	}
	
	protected DataList requestDataList(Session session, DataMap req) throws RedbackException
	{
		return requestDataList(session, req, false);
	}
	
	protected DataList requestDataList(Session session, DataMap req, boolean async) throws RedbackException
	{
		Object resp = requestObject(session, req, async);
		if(resp instanceof DataMap) {
			return (DataList)resp;
		} else if(resp instanceof String) {
			try {
				DataList list = new DataList((String)resp);
				return list;
			} catch (DataException e) {
				throw new RedbackException("Return data from " + serviceName + " is not a DataList");
			}
		} else {
			throw new RedbackException("Return data from " + serviceName + " is not a DataList");
		}
	}
	
	protected Object requestObject(Session session, DataMap req, boolean async) throws RedbackException
	{
		Payload reqP = new Payload(req);
		reqP.metadata.put("mime", "application/json");
		Payload respP = requestPayload(session, reqP, async);
		return respP.getDataObject();
	}
	
	protected Payload requestPayload(Session session, Payload reqP) throws RedbackException 
	{
		return requestPayload(session, reqP, false);
	}
	
	protected Payload requestPayload(Session session, Payload reqP, boolean async) throws RedbackException 
	{
		if(serviceName != null)
		{
			try
			{
				if(session != null) {
					reqP.metadata.put("session", session.id);
					reqP.metadata.put("token", session.token);
					if(session.getTimezone() != null) 
						reqP.metadata.put("timezone", session.getTimezone());
				}
				Payload respP = null;
				if(async)
					firebus.requestServiceAndForget(serviceName, reqP);
				else
					respP = firebus.requestService(serviceName, reqP, defaultTimeout);
				return respP;
			}
			catch(Exception e)
			{
				throw new RedbackException("Error requesting " + serviceName, e);
			}
		}
		else
		{
			throw new RedbackException("Service name not provided");
		}
	}
	
	protected StreamEndpoint requestStream(Session session, DataMap req) throws RedbackException
	{
		Payload reqP = new Payload(req);
		reqP.metadata.put("mime", "application/json");
		StreamEndpoint sep = requestStream(session, reqP);
		return sep;
	}
	
	protected StreamEndpoint requestStream(Session session, Payload reqP) throws RedbackException 
	{
		if(serviceName != null)
		{
			try
			{
				if(session != null) {
					reqP.metadata.put("session", session.id);
					reqP.metadata.put("token", session.token);
					if(session.getTimezone() != null) 
						reqP.metadata.put("timezone", session.getTimezone());
				}
				StreamEndpoint sep = firebus.requestStream(serviceName, reqP, 5000);
				return sep;
			}
			catch(Exception e)
			{
				throw new RedbackException("Error requesting " + serviceName, e);
			}
		}
		else
		{
			throw new RedbackException("Stream name not provided");
		}
	}
	
	protected void publish(DataMap req) throws RedbackException
	{
		publish(null, req);
	}
	
	protected void publish(Session session, DataMap req) throws RedbackException
	{
		if(serviceName != null)
		{
			try
			{
				Payload reqP = new Payload(req);
				reqP.metadata.put("mime", "application/json");
				if(session != null) {
					reqP.metadata.put("session", session.id);
					reqP.metadata.put("token", session.token);
					if(session.getTimezone() != null) 
						reqP.metadata.put("timezone", session.getTimezone());
				}
				reqP.metadata.put("mime", "application/json");
				firebus.publish(serviceName, reqP);
			}
			catch(Exception e)
			{
				throw new RedbackException("Error publishing to " + serviceName, e);
			}
		}
		else
		{
			throw new RedbackException("Service name not provided");
		}

	}	
}
