package io.redback.services;

import java.security.MessageDigest;
import java.security.spec.KeySpec;
import java.util.HashMap;
import java.util.logging.Logger;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.exceptions.FunctionErrorException;
import io.firebus.information.ServiceInformation;
import io.firebus.interfaces.ServiceProvider;
import io.firebus.utils.DataMap;
import io.redback.RedbackException;
import io.redback.security.Role;
import io.redback.security.Session;
import io.redback.utils.StringUtils;

public abstract class AccessManager extends Service implements ServiceProvider
{
	private Logger logger = Logger.getLogger("io.redback");
	protected HashMap<String, Role> roles;
	protected KeySpec keySpec;
	protected MessageDigest digest;
	protected long expiryTime;
	protected String sessionTable = "rbam_session";
	protected String userTable = "rbam_user";
	
	public AccessManager(String n, DataMap c, Firebus f) 
	{
		super(n, c, f);
		expiryTime = 1800000;
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
			logger.severe(StringUtils.getStackTrace(e));
			throw new FunctionErrorException("Exception in access management service", e);
		}
		
		responsePayload.setData(response.toString());
		logger.finer("Access manager service finish");
		return responsePayload;
	}

	public ServiceInformation getServiceInformation() 
	{
		return null;
	}
	

	
	protected abstract Session validateToken(String token) throws RedbackException;
}
