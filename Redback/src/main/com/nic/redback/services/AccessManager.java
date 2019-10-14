package com.nic.redback.services;

import java.security.MessageDigest;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;
import java.util.logging.Logger;

import com.nic.firebus.Payload;
import com.nic.firebus.exceptions.FunctionErrorException;
import com.nic.firebus.information.ServiceInformation;
import com.nic.firebus.interfaces.Consumer;
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

public class AccessManager extends RedbackService implements Consumer
{
	private Logger logger = Logger.getLogger("com.nic.redback");
	protected HashMap<String, Role> roles;
	protected ArrayList<Session> cachedSessions;
	protected ArrayList<UserProfile> cachedUserProfiles;
	protected KeySpec keySpec;
	protected MessageDigest digest;
	protected long expiryTime;
	
	public AccessManager(JSONObject c) 
	{
		super(c);
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
					UserProfile userProfile = getUserProfile(username);
					if(userProfile != null)
					{
		 				String passwordHash = hashString(password);
						if(userProfile.getPasswordHash().equals(passwordHash))
						{
							Session session = getSessionByUsername(username);
							if(session == null)
								session = createSession(username);
							
							response.put("result", "ok");
							response.put("session", session.getJSON());
							extendSession(session);
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
				Session session = getSessionById(sessionId);
				
				if(session != null)
				{
					if(System.currentTimeMillis() < session.expiry)
					{
						response.put("result", "ok");
						response.put("session", session.getJSON());
						extendSession(session);
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
			else if(action.equals("listusers"))
			{
				JSONObject filter = request.getObject("filter");
				if(filter != null)
				{
					JSONList respList = new JSONList();
					ArrayList<UserProfile> list = listUserProfiles(filter);
					for(int i = 0; i < list.size(); i++)
						respList.add(list.get(i).getSimpleJSON());
					response.put("result", respList);
				}
			}
			else if(action.equals("logout"))
			{
				String sessionidStr = request.getString("sessionid");
				UUID sessionId = UUID.fromString(sessionidStr);
				Session session = getSessionById(sessionId);
				if(session != null)
				{	
					logoutSession(session);
					response.put("result", "ok");
				}
				else
				{
					response.put("result", "failed");
					response.put("error", "Session not found");						
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
	
	public void consume(Payload payload)
	{
		logger.info("Access manager consumer start");
		try
		{
			JSONObject request = new JSONObject(payload.getString());
			String action = request.getString("action");
			
			if(action.equals("dropfromcache"))
			{
				UUID sessionId = UUID.fromString(request.getString("sessionid"));
				for(int i = 0; i < cachedSessions.size(); i++)
					if(cachedSessions.get(i).sessionId.equals(sessionId))
							cachedSessions.remove(i);
			}
		}
		catch(Exception e)
		{	
			logger.severe(e.getMessage());
		}
		logger.info("Access manager consumer finish");
	}

	
	protected String hashString(String str) throws InvalidKeySpecException
	{
		digest.update(str.getBytes());
		byte[] hash = digest.digest();
		String hashStr = Base64.encode(hash);
		return hashStr;
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
	
	protected Session createSession(String username) throws RedbackException
	{
		UUID sessionId = UUID.randomUUID();
		long expiry = System.currentTimeMillis() + expiryTime;
		Session session = new Session(sessionId, getUserProfile(username), expiry);
		cachedSessions.add(session);
		firebus.publish(configService, new Payload("{object:rbam_session, data:{_id:\""+ sessionId.toString()+"\", username:\"" + username + "\", expiry:" + expiry + "}}"));
		return session;
	}
	
	protected void logoutSession(Session session)
	{
		session.expiry = System.currentTimeMillis();
		firebus.publish(configService, new Payload("{object:rbam_session, data:{_id:\""+ session.getSessionId().toString()+"\", username:\"" + session.getUserProfile().getUsername() + "\", expiry:" + session.expiry + "}}"));
		firebus.publish(this.serviceName, new Payload("{action:dropfromcache, sessionid:\""+ session.getSessionId().toString()+"\"}"));
	}
	
	protected Session getSessionById(UUID sessionId) throws RedbackException
	{
		Session session = null;
		for(int i = 0; i < cachedSessions.size(); i++)
			if(cachedSessions.get(i).sessionId.equals(sessionId))
				if(cachedSessions.get(i).expiry > System.currentTimeMillis())
					session = cachedSessions.get(i);
				else
					cachedSessions.remove(i);
		
		if(session == null)
		{
			try
			{
				JSONObject sessionResult = request(configService, "{object:rbam_session, filter:{_id:\"" + sessionId.toString() + "\", expiry:{$gt:" + System.currentTimeMillis() + "}}}");
				if(sessionResult.getList("result").size() > 0)
				{
					JSONObject sessionJSON = sessionResult.getObject("result.0");
					String username = sessionJSON.getString("username");
					long expiry = sessionJSON.getNumber("expiry").longValue();
					session = new Session(sessionId, getUserProfile(username), expiry);
					cachedSessions.add(session);
				}
			}
			catch(Exception e)
			{
				logger.severe(e.getMessage());
				throw new RedbackException("Exception getting session by id from database : ", e);
			}
		}
		
		return session;
	}

	protected Session getSessionByUsername(String username) throws RedbackException
	{
		Session session = null;
		for(int i = 0; i < cachedSessions.size(); i++)
			if(cachedSessions.get(i).userProfile.getUsername().equals(username))
				if(cachedSessions.get(i).expiry > System.currentTimeMillis())
					session = cachedSessions.get(i);
				else
					cachedSessions.remove(i);

		
		if(session == null)
		{
			try
			{
				JSONObject sessionResult = request(configService, "{object:rbam_session, filter:{username:\"" + username + "\", expiry:{$gt:" + System.currentTimeMillis() + "}}}");
				if(sessionResult.getList("result").size() > 0)
				{
					JSONObject sessionJSON = sessionResult.getObject("result.0");
					long expiry = sessionJSON.getNumber("expiry").longValue();
					UUID sessionId = UUID.fromString(sessionJSON.getString("_id"));
					session = new Session(sessionId, getUserProfile(username), expiry);
					cachedSessions.add(session);
				}
			}
			catch(Exception e)
			{
				logger.severe(e.getMessage());
				throw new RedbackException("Exception getting session by username from database : ", e);
			}

		}
		
		return session;
	}
	
	protected UserProfile getUserProfile(String username) throws RedbackException
	{
		UserProfile userProfile = null;
		for(int i = 0; i < cachedUserProfiles.size(); i++)
			if(cachedUserProfiles.get(i).getUsername().equals(username))
				userProfile = cachedUserProfiles.get(i);

		if(userProfile == null)
		{
			try
			{
				ArrayList<UserProfile> list = listUserProfiles(new JSONObject("{username:\"" + username + "\"}"));
				if(list.size() > 0)
				{
					userProfile = list.get(0);
				}
			}
			catch(Exception e)
			{
				logger.severe(e.getMessage());
				throw new RedbackException("Exception getting user profile from database : ", e);
			}

		}
		return userProfile;
	}
	
	protected ArrayList<UserProfile> listUserProfiles(JSONObject filter) throws RedbackException
	{
		ArrayList<UserProfile> list = new ArrayList<UserProfile>();
		try
		{
			JSONObject userResult = request(configService, "{object:rbam_user, filter:" + filter.toString() + "}");
			for(int i = 0; i < userResult.getList("result").size(); i++)
			{
				JSONObject userJSON = userResult.getObject("result." + i);
				UserProfile userProfile = null;
				for(int j = 0 ; j < cachedUserProfiles.size(); j++)
					if(cachedUserProfiles.get(j).getUsername().equals(userJSON.getString("username")))
						userProfile = cachedUserProfiles.get(j);
				if(userProfile == null)
				{
					JSONList rolesList = userJSON.getList("roles");
					JSONObject rights = new JSONObject();
					for(int j = 0; j < rolesList.size(); j++)
					{
						String roleName = rolesList.getString(j);
						Role role = getRole(roleName);
						JSONObject roleRights = role.getAllRights();
						mergeRights(rights, roleRights);
					}
					userJSON.put("rights", rights);
					userProfile = new UserProfile(userJSON);	
					cachedUserProfiles.add(userProfile);
				}
				list.add(userProfile);
			}			
		}
		catch(Exception e)
		{
			logger.severe(e.getMessage());
			throw new RedbackException("Exception listing user profiles from database : ", e);
		}
		return list;
	}

	protected void extendSession(Session session)
	{
		if(session.expiry < System.currentTimeMillis() + (expiryTime / 2))
		{
			long newExpiry = System.currentTimeMillis() + expiryTime;
			session.expiry = newExpiry;
			firebus.publish(configService, new Payload("{object:rbam_session, data:{_id:\""+ session.sessionId.toString()+"\", expiry:" + newExpiry + "}}"));
		}
		
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
