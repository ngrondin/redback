package com.nic.redback.services;

import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;
import java.util.logging.Logger;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import com.nic.firebus.Payload;
import com.nic.firebus.exceptions.FunctionErrorException;
import com.nic.firebus.information.ServiceInformation;
import com.nic.firebus.utils.JSONEntity;
import com.nic.firebus.utils.JSONList;
import com.nic.firebus.utils.JSONLiteral;
import com.nic.firebus.utils.JSONObject;
import com.nic.redback.RedbackException;
import com.nic.redback.RedbackService;
import com.nic.redback.security.Role;
import com.nic.redback.security.Session;
import com.nic.redback.security.UserProfile;
import com.sun.org.apache.xml.internal.security.utils.Base64;

public class AccessManager extends RedbackService
{
	private Logger logger = Logger.getLogger("com.nic.redback");
	protected HashMap<String, Role> roles;
	protected ArrayList<Session> cachedSessions;
	protected SecretKeyFactory secretKeyFactory;
	protected KeySpec keySpec;
	
	public AccessManager(JSONObject c) 
	{
		super(c);
		cachedSessions = new ArrayList<Session>();
		roles = new HashMap<String, Role>();
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
		logger.info("Access manager service start");
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
					JSONObject userResult = request(configService, "{object:rbam_user, filter:{username:\"" + username + "\"}}");
					if(userResult.getList("result").size() > 0)
					{
						JSONObject userJSON = userResult.getObject("result.0");
		 				String passwordHash = hashString(password);
						if(userJSON.getString("passwordhash").equals(passwordHash))
						{
							Session session = getCachedSessionByUsername(username);
							if(session != null  &&  System.currentTimeMillis() > session.expiry)
								session = null;
							
							if(session == null)
							{
								UserProfile userProfile = buildUserProfile(userJSON);
								JSONObject sessionResult = request(configService, "{object:rbam_session, filter:{username:\"" + username + "\", expiry:{$gt:" + System.currentTimeMillis() + "}}}");
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
									firebus.publish(configService, new Payload("{object:rbam_session, data:{_id:\""+ sessionId.toString()+"\", username:\"" + username + "\", expiry:\"" + expiry + "\"}}"));
								}
								cachedSessions.add(session);
							}
							
							response.put("result", "ok");
							response.put("session", session.getJSON());
						}
						else
						{
							response.put("result", "failed");
							response.put("error", "Username does not exist or password does not match");
						}
					}
					else
					{
						response.put("result", "failed");
						response.put("error", "Username does not exist or password does not match");
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
					JSONObject sessionResult = request(configService, "{object:rbam_session, filter:{_id:\"" + sessionId.toString() + "\"}}");
					if(sessionResult.getList("result").size() > 0)
					{
						JSONObject sessionJSON = sessionResult.getObject("result.0");
						String username = sessionJSON.getString("username");
						long expiry = Long.parseLong(sessionJSON.getString("expiry"));
						JSONObject userResult = request(configService, "{object:rbam_user, filter:{username:\"" + username + "\"}}");
						if(userResult.getList("result").size() > 0)
						{
							JSONObject userJSON = userResult.getObject("result.0");
							UserProfile userProfile = buildUserProfile(userJSON);
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
						response.put("session", session.getJSON());
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
		logger.info("Access manager service finish");
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
	
	protected Role getRole(String name) throws RedbackException
	{
		Role role = roles.get(name);
		if(role == null)
		{
			try
			{
				JSONObject result = request(configService, "{object:rbam_role,filter:{name:" + name + "}}");
				if(result.getList("result").size() > 0)
					role = new Role(result.getObject("result.0"));
				roles.put(name,  role);
			}
			catch(Exception e)
			{
				logger.severe(e.getMessage());
				throw new RedbackException("Exception getting role from database : ", e);
			}
		}
		return role;		
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
	
	protected UserProfile buildUserProfile(JSONObject userJSON) throws RedbackException
	{
		String username =  userJSON.getString("username");
		JSONList rolesList = userJSON.getList("roles");
		JSONList domainsList = userJSON.getList("domains");
		JSONObject attributes = userJSON.getObject("attributes");
		JSONObject rights = new JSONObject();

		for(int i = 0; i < rolesList.size(); i++)
		{
			String roleName = rolesList.getString(i);
			Role role = getRole(roleName);
			JSONObject roleRights = role.getAllRights();
			mergeRights(rights, roleRights);
		}

		JSONObject userProfileJSON = new JSONObject();
		userProfileJSON.put("username", username);
		userProfileJSON.put("domains", domainsList);
		userProfileJSON.put("roles", rolesList);
		userProfileJSON.put("attributes", attributes);
		userProfileJSON.put("rights", rights);
		
		return new UserProfile(userProfileJSON);
	}
	
	protected void mergeRights(JSONObject to, JSONObject from)
	{
		Iterator<String> it = from.keySet().iterator();
		while(it.hasNext())
		{
			String key = it.next();
			JSONEntity e = from.get(key);
			if(e instanceof JSONObject)
			{
				JSONObject fromSub = (JSONObject)e;
				if(!fromSub.keySet().isEmpty())
				{
					JSONObject toSub = to.getObject(key);
					if(toSub == null)
					{
						toSub = new JSONObject();
						to.put(key, toSub);
					}
					mergeRights(toSub, (JSONObject)e);
				}
			}
			else if(e instanceof JSONLiteral)
			{
				String fromRight = ((JSONLiteral)e).getString();
				String toRight = to.getString(key);
				if(toRight == null)
					toRight = "";
				String result = "";
				if(toRight.contains("r") || fromRight.contains("r"))
					result += "r";
				if(toRight.contains("w") || fromRight.contains("w"))
					result += "w";
				if(toRight.contains("x") || fromRight.contains("x"))
					result += "x";
				to.put(key, result);				
			}
		}
	}
}
