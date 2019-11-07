package com.nic.redback.services;

import java.security.MessageDigest;
import java.security.spec.KeySpec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

import com.nic.firebus.Firebus;
import com.nic.firebus.Payload;
import com.nic.firebus.exceptions.FunctionErrorException;
import com.nic.firebus.information.ServiceInformation;
import com.nic.firebus.interfaces.Consumer;
import com.nic.firebus.utils.DataMap;
import com.nic.redback.RedbackException;
import com.nic.redback.security.Role;
import com.nic.redback.security.Session;
import com.nic.redback.security.UserProfile;

public abstract class AccessManager extends DataService implements Consumer
{
	private Logger logger = Logger.getLogger("com.nic.redback");
	protected HashMap<String, Role> roles;
	protected ArrayList<Session> cachedSessions;
	protected ArrayList<UserProfile> cachedUserProfiles;
	protected KeySpec keySpec;
	protected MessageDigest digest;
	protected long expiryTime;
	protected String sessionTable = "rbam_session";
	protected String userTable = "rbam_user";
	
	public AccessManager(DataMap c, Firebus f) 
	{
		super(c, f);
		expiryTime = 1800000;
		cachedSessions = new ArrayList<Session>();
		cachedUserProfiles = new ArrayList<UserProfile>();
		roles = new HashMap<String, Role>();
		try
		{
			digest = MessageDigest.getInstance("SHA-256");
		}
		catch(Exception e)
		{
			logger.severe(e.getMessage());
		}
	}

	public Payload service(Payload payload) throws FunctionErrorException 
	{
		logger.finer("Access manager service start");
		Payload responsePayload = new Payload();
		DataMap response = new DataMap();
		try
		{
			DataMap request = new DataMap(payload.getString());
			String action = request.getString("action");

			if(action.equals("validate"))
			{
				String token = request.getString("token");
				Session session = validateToken(token);
				
				if(session != null)
				{
					if(System.currentTimeMillis() < session.expiry)
					{
						response.put("result", "ok");
						response.put("session", session.getJSON());
						//extendSession(session);
					}
					else
					{
						response.put("result", "failed");
						response.put("error", "Session has expired");
					}
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
			logger.severe(e.getMessage());
			throw new FunctionErrorException(e.getMessage());
		}
		
		responsePayload.setData(response.toString());
		logger.finer("Access manager service finish");
		return responsePayload;
	}

	public ServiceInformation getServiceInformation() 
	{
		return null;
	}
	
	public void consume(Payload payload)
	{
		logger.finer("Access manager consumer start");
		try
		{
			DataMap request = new DataMap(payload.getString());
			String action = request.getString("action");
			
			if(action.equals("dropfromcache"))
			{
				//UUID sessionId = UUID.fromString(request.getString("sessionid"));
				for(int i = 0; i < cachedSessions.size(); i++)
					cachedSessions.remove(i);
			}
		}
		catch(Exception e)
		{	
			logger.severe(e.getMessage());
		}
		logger.finer("Access manager consumer finish");
	}

	
	protected abstract Session validateToken(String token) throws RedbackException;
}
