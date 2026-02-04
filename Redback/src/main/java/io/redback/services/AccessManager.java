package io.redback.services;

import java.security.MessageDigest;
import java.security.spec.KeySpec;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.data.DataMap;
import io.firebus.information.ServiceInformation;
import io.firebus.logging.Logger;
import io.redback.exceptions.RedbackException;
import io.redback.security.Session;
import io.redback.security.UserProfile;
import io.redback.services.common.ServiceProvider;

public abstract class AccessManager extends ServiceProvider
{
	protected KeySpec keySpec;
	protected MessageDigest digest;
	protected long expiryTime;
	protected String sessionTable = "rbam_session";
	protected String userTable = "rbam_user";
	
	public AccessManager(String n, DataMap c, Firebus f) 
	{
		super(n, c, f);
		expiryTime = 1800000;
		try
		{
			digest = MessageDigest.getInstance("SHA-256");
		}
		catch(Exception e)
		{
			Logger.severe("rb.access.init", "Error configuring access manager", e);
		}
	}

	public Payload redbackService(Session session, Payload payload) throws RedbackException 
	{
		Logger.finer("rb.access.start");
		Payload responsePayload = new Payload();
		DataMap response = new DataMap();
		try
		{
			DataMap request = payload.getDataMap();
			String action = request.getString("action");

			if(action.equals("validate"))
			{
				String token = request.getString("token");
				UserProfile userProfile = validateToken(session, token);
				response.put("result", "ok");
				response.put("userprofile", userProfile.getJSON());
			} else if(action.equals("getsysuser")) {
				String sysUserToken = getSysUserToken(session);
				response.put("token", sysUserToken);				
			}
		}
		catch(Exception e)
		{	
			throw new RedbackException("Exception in access management service", e);
		}
		
		responsePayload.setData(response);
		Logger.finer("rb.access.finish");
		return responsePayload;
	}

	public ServiceInformation getServiceInformation() 
	{
		return null;
	}
	

	protected abstract String getSysUserToken(Session session) throws RedbackException;
	
	protected abstract UserProfile validateToken(Session session, String token) throws RedbackException;
}
