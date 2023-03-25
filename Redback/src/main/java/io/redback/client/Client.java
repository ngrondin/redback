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
		Payload resp = requestPayload(session, req, async);
		try {
			return resp.getDataMap();
		} catch(DataException e) {
			throw new RedbackException("Return data from " + serviceName + " is not a DataMap", e);
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
		Payload resp = requestPayload(session, req, async);
		try {
			return resp.getDataList();
		} catch(DataException e) {
			throw new RedbackException("Return data from " + serviceName + " is not a DataList", e);
		}
	}
	
	protected Payload requestPayload(Session session, DataMap req, boolean async) throws RedbackException 
	{
		Payload reqP = new Payload(req);
		reqP.metadata.put("mime", "application/json");
		return requestPayload(session, reqP, async);
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
				setSessionMeta(session, reqP);
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
				setSessionMeta(session, reqP);
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
				setSessionMeta(session, reqP);
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
	
	protected void setSessionMeta(Session session, Payload payload) 
	{
		if(session != null && payload != null) {
			payload.metadata.put("session", session.id);
			payload.metadata.put("token", session.token);
			if(session.getTimezone() != null) 
				payload.metadata.put("timezone", session.getTimezone());
			if(session.getDomainLock() != null)
				payload.metadata.put("domain", session.getDomainLock());
		}
	}
}
