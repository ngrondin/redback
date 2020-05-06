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
		if(serviceName != null)
		{
			try
			{
				Payload reqP = new Payload(req.toString());
				if(session != null)
					reqP.metadata.put("token", session.getToken());
				Payload respP = firebus.requestService(serviceName, reqP);
				DataMap resp = new DataMap(respP.getString());
				return resp;
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
				if(session != null)
					reqP.metadata.put("token", session.getToken());
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
