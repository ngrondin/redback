package io.redback.client;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.utils.DataMap;
import io.redback.RedbackException;
import io.redback.security.Session;

public class Client {

	protected Firebus firebus;
	protected String serviceName;
	
	public Client(Firebus fb, String sn)
	{
		firebus = fb;
		serviceName = sn;
	}
	
	protected DataMap request(DataMap req) throws RedbackException
	{
		return request(null, req);
	}
	
	protected DataMap request(Session session, DataMap req) throws RedbackException
	{
		try
		{
			Payload reqP = new Payload(req.toString());
			reqP.metadata.put("mime", "application/json");
			Payload respP = requestPayload(session, reqP);
			if(respP.getBytes().length > 0) {
				DataMap resp = new DataMap(respP.getString());
				return resp;
			} else {
				return null;
			}
		}
		catch(Exception e)
		{
			throw new RedbackException("Error requesting " + serviceName, e);
		}
	}
	
	protected Payload requestPayload(Session session, Payload reqP) throws RedbackException 
	{
		if(serviceName != null)
		{
			try
			{
				if(session != null) {
					reqP.metadata.put("session", session.id);
					reqP.metadata.put("token", session.token);
				}
				Payload respP = firebus.requestService(serviceName, reqP);
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
				Payload reqP = new Payload(req.toString());
				if(session != null) {
					reqP.metadata.put("session", session.id);
					reqP.metadata.put("token", session.token);
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
