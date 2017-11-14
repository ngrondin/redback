package com.nic.redback;

import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.ArrayList;
import java.util.UUID;
import java.util.logging.Logger;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import com.nic.firebus.Payload;
import com.nic.firebus.exceptions.FunctionErrorException;
import com.nic.firebus.information.ServiceInformation;
import com.nic.firebus.utils.JSONObject;
import com.nic.redback.security.Session;
import com.nic.redback.security.UserProfile;
import com.sun.org.apache.xml.internal.security.utils.Base64;

public class AccessManager extends RedbackService
{
	private Logger logger = Logger.getLogger("com.nic.redback");
	protected ArrayList<Session> cachedSessions;
	protected String dataService;
	protected SecretKeyFactory secretKeyFactory;
	protected KeySpec keySpec;
	
	public AccessManager(JSONObject c) 
	{
		super(c);
		dataService = config.getString("dataservice");
		cachedSessions = new ArrayList<Session>();
		try
		{
			secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
		}
		catch(Exception e)
		{
			logger.severe(e.getMessage());
		}
	}

	public Payload service(Payload payload) throws FunctionErrorException 
	{
		Payload responsePayload = new Payload();
		JSONObject response = new JSONObject();
		try
		{
			JSONObject request = new JSONObject(payload.getString());
			String action = request.getString("action");
			
			if(action.equals("authenticate"))
			{
				String username = request.getString("username");
				String password = request.getString("password");
				if(username != null  &&  password != null)
				{
					JSONObject result = request(dataService, "{object:rbam_user, filter:{username:\"" + username + "\"}}");
					if(result.getList("result").size() > 0)
					{
						UserProfile userProfile = new UserProfile(result.getObject("result.0"));
		 				String passwordHash = hashString(password);
						if(userProfile.getPasswordHash().equals(passwordHash))
						{
							Session session = getCachedSessionByUsername(username);
							if(session != null  &&  System.currentTimeMillis() > session.expiry)
								session = null;
							
							if(session == null)
							{
								JSONObject sessionResult = request(dataService, "{object:rbam_session, filter:{username:\"" + username + "\", expiry:{$gt:" + System.currentTimeMillis() + "}}}");
								if(sessionResult.getList("result").size() > 0)
								{
									JSONObject sessionJSON = sessionResult.getObject("result.0");
									long expiry = Long.parseLong(sessionJSON.getString("expiry"));
									UUID sessionId = UUID.fromString(sessionJSON.getString("_id"));
									session = new Session(sessionId, userProfile, expiry);
								}
								else
								{
									UUID sessionId = UUID.randomUUID();
									long expiry = System.currentTimeMillis() + 1800000;
									session = new Session(sessionId, userProfile, expiry);
									firebus.publish(dataService, new Payload("{object:rbam_session, data:{_id:\""+ sessionId.toString()+"\", username:\"" + username + "\", expiry:\"" + expiry + "\"}}"));
								}
								cachedSessions.add(session);
							}
							
							response.put("result", "ok");
							response.put("sessionid", session.sessionId.toString());
							response.put("username", userProfile.getUsername());
							response.put("roles", userProfile.getJSON().getList("roles"));
							response.put("domains", userProfile.getJSON().getList("domains"));
						}
						else
						{
							response.put("result", "failed");
							response.put("error", "Username or password does not match");
						}
					}
					else
					{
						response.put("result", "failed");
						response.put("error", "Username or password does not match");
					}
				}
				else
				{
					String msg = "An authenticate action requires a username and a password";
					response.put("result", "error");
					response.put("error", msg);
					logger.severe(msg);
				}
			}
			else if(action.equals("validate"))
			{
				String sessionidStr = request.getString("sessionid");
				UUID sessionId = UUID.fromString(sessionidStr);
				Session session = getCachedSessionById(sessionId);

				if(session == null)
				{
					JSONObject result = request(dataService, "{object:rbam_session, filter:{_id:\"" + sessionId.toString() + "\"}}");
					if(result.getList("result").size() > 0)
					{
						JSONObject sessionJSON = result.getObject("result.0");
						String username = sessionJSON.getString("username");
						long expiry = Long.parseLong(sessionJSON.getString("expiry"));
						JSONObject userProfileResult = request(dataService, "{object:rbam_user, filter:{username:\"" + username + "\"}}");
						if(userProfileResult.getList("result").size() > 0)
						{
							JSONObject userProfileJSON = userProfileResult.getObject("result.0");
							UserProfile userProfile = new UserProfile(userProfileJSON);
							session = new Session(sessionId, userProfile, expiry);
							cachedSessions.add(session);
						}
					}	
				}
				
				if(session != null)
				{
					if(System.currentTimeMillis() < session.expiry)
					{
						response.put("result", "ok");
						response.put("username", session.userProfile.getUsername());
						response.put("roles", session.userProfile.getJSON().getList("roles"));
						response.put("domains", session.userProfile.getJSON().getList("domains"));
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
					response.put("error", "Not a valid session");						
				}
			}
		}
		catch(Exception e)
		{	
			logger.severe(e.getMessage());
			throw new FunctionErrorException(e.getMessage());
		}
		
		responsePayload.setData(response.toString());
		return responsePayload;
	}

	public ServiceInformation getServiceInformation() 
	{
		return null;
	}
	
	protected String hashString(String str) throws InvalidKeySpecException
	{
		if(secretKeyFactory != null)
		{
			byte[] salt = "redback_access_s".getBytes();
			KeySpec keySpec = new PBEKeySpec(str.toCharArray(), salt, 65536, 128);
			byte[] hash = secretKeyFactory.generateSecret(keySpec).getEncoded();
			return Base64.encode(hash);
		}
		return "";
	}
	
	protected Session getCachedSessionById(UUID sessionId)
	{
		for(int i = 0; i < cachedSessions.size(); i++)
			if(cachedSessions.get(i).sessionId.equals(sessionId))
				return cachedSessions.get(i);
		return null;
	}

	protected Session getCachedSessionByUsername(String username)
	{
		for(int i = 0; i < cachedSessions.size(); i++)
			if(cachedSessions.get(i).userProfile.getUsername().equals(username))
				return cachedSessions.get(i);
		return null;
	}
}
