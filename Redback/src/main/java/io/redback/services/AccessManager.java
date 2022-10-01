package io.redback.services;

import java.security.MessageDigest;
import java.security.spec.KeySpec;
import java.util.logging.Logger;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.data.DataMap;
import io.firebus.information.ServiceInformation;
import io.redback.exceptions.RedbackException;
import io.redback.security.Session;
import io.redback.security.UserProfile;
import io.redback.services.common.ServiceProvider;

public abstract class AccessManager extends ServiceProvider
{
	private Logger logger = Logger.getLogger("io.redback");
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
			logger.severe(e.getMessage());
		}
	}

	public Payload redbackService(Session session, Payload payload) throws RedbackException 
	{
		logger.finer("Access manager service start");
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
				
				if(userProfile != null)
				{
					response.put("result", "ok");
					response.put("userprofile", userProfile.getJSON());
				}
				else
				{
					response.put("result", "failed");
					response.put("error", "User has no profile");						
				}
			}
		}
		catch(Exception e)
		{	
			throw new RedbackException("Exception in access management service", e);
		}
		
		responsePayload.setData(response);
		logger.finer("Access manager service finish");
		return responsePayload;
	}

	public ServiceInformation getServiceInformation() 
	{
		return null;
	}
	

	
	protected abstract UserProfile validateToken(Session session, String token) throws RedbackException;
}
