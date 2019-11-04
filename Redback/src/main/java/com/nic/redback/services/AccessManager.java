package com.nic.redback.services;

import java.security.MessageDigest;
import java.security.spec.KeySpec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Logger;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.nic.firebus.Firebus;
import com.nic.firebus.Payload;
import com.nic.firebus.exceptions.FunctionErrorException;
import com.nic.firebus.information.ServiceInformation;
import com.nic.firebus.interfaces.Consumer;
import com.nic.firebus.utils.DataEntity;
import com.nic.firebus.utils.DataList;
import com.nic.firebus.utils.DataLiteral;
import com.nic.firebus.utils.DataMap;
import com.nic.redback.RedbackDataService;
import com.nic.redback.RedbackException;
import com.nic.redback.security.Role;
import com.nic.redback.security.Session;
import com.nic.redback.security.UserProfile;

public class AccessManager extends RedbackDataService implements Consumer
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
			
			/*
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
			else */
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
			/*else if(action.equals("logout"))
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
			}*/
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

	/*
	protected String hashString(String str) throws InvalidKeySpecException
	{
		digest.update(str.getBytes());
		byte[] hash = digest.digest();
		String hashStr = Base64.encode(hash);
		return hashStr;
	}
	*/
	
	protected Session validateToken(String token) throws RedbackException
	{
		Session session = null;
		try 
		{
			DecodedJWT jwt = JWT.decode(token);
			Claim usernameClaim = jwt.getClaim("email");
			String username = usernameClaim.asString();
			UserProfile profile = getUserProfile(username);
			session = new Session(token, profile, jwt.getExpiresAt().getTime());
		} 
		catch (JWTDecodeException  exception)
		{
		    throw new RedbackException("JWT token is invalid");
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
				DataMap userConfig = null;
				DataList userConfigs = config.getList("users");
				if(userConfigs != null)
				{
					for(int i = 0; i < userConfigs.size(); i++)
						if(userConfigs.getObject(i).containsKey("username") && userConfigs.getObject(i).getString("username").equals(username))
							userConfig = userConfigs.getObject(i);
				}
				
				if(userConfig == null && dataService != null)
				{
					String collection = config.containsKey("usertable") ? config.getString("usertable") : "rbam_user";
					DataMap userResult = getData(collection, "{\"username\":\"" + username + "\"}");
					if(userResult != null && userResult.getList("result") != null && userResult.getList("result").size() > 0)
						userConfig = userResult.getList("result").getObject(0);
				}
	
				if(userConfig != null)
				{
					DataList rolesList = userConfig.getList("roles");
					DataMap rights = new DataMap();
					for(int j = 0; j < rolesList.size(); j++)
					{
						String roleName = rolesList.getString(j);
						Role role = getRole(roleName);
						DataMap roleRights = role.getAllRights();
						mergeRights(rights, roleRights);
					}
					userConfig.put("rights", rights);
					userProfile = new UserProfile(userConfig);	
					cachedUserProfiles.add(userProfile);
				}
			}
			catch(Exception e)
			{
				error("Error getting user profile for " + username, e);
			}
		}
		return userProfile;
	}
	
	protected Role getRole(String name) throws RedbackException
	{
		Role role = roles.get(name);
		if(role == null)
		{
			try
			{
				role =  new Role(getConfig("rbam", "role", name));
				roles.put(name, role);
			}
			catch(Exception e)
			{
				error("Exception getting role from database : ", e);
			}
		}
		return role;		
	}
	/*
	protected Session createSession(String username) throws RedbackException
	{
		UUID sessionId = UUID.randomUUID();
		long expiry = System.currentTimeMillis() + expiryTime;
		Session session = new Session(sessionId, getUserProfile(username), expiry);
		cachedSessions.add(session);
		publishData(sessionTable, "{_id:\""+ sessionId.toString()+"\", username:\"" + username + "\", expiry:" + expiry + "}");
		return session;
	}
	*/
	
	/*
	protected void logoutSession(Session session)
	{
		session.expiry = System.currentTimeMillis();
		publishData(sessionTable, "{_id:\""+ session.getSessionId().toString()+"\", username:\"" + session.getUserProfile().getUsername() + "\", expiry:" + session.expiry + "}");
		firebus.publish(serviceName, new Payload("{action:dropfromcache, sessionid:\""+ session.getSessionId().toString()+"\"}"));
	}
	*/
	
	/*
	protected Session getSessionById(UUID sessionId) throws RedbackException
	{
		Session session = null;
		for(int i = 0; i < cachedSessions.size(); i++)
			if(cachedSessions.get(i).sessionId.equals(sessionId))
				if(cachedSessions.get(i).expiry > System.currentTimeMillis())
					session = cachedSessions.get(i);
				else
					cachedSessions.remove(i);
		
		if(session == null && dataService != null)
		{
			try
			{
				DataMap sessionResult = getData("rbam_session", "{_id:\"" + sessionId.toString() + "\", expiry:{$gt:" + System.currentTimeMillis() + "}}");
				if(sessionResult.getList("result").size() > 0)
				{
					DataMap sessionJSON = sessionResult.getObject("result.0");
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
	*/
	/*
	protected Session getSessionByUsername(String username) throws RedbackException
	{
		Session session = null;
		for(int i = 0; i < cachedSessions.size(); i++)
			if(cachedSessions.get(i).userProfile.getUsername().equals(username))
				if(cachedSessions.get(i).expiry > System.currentTimeMillis())
					session = cachedSessions.get(i);
				else
					cachedSessions.remove(i);
		
		if(session == null  &&  dataService != null)
		{
			try
			{
				DataMap	sessionResult = getData("rbam_session", "{username:\"" + username + "\", expiry:{$gt:" + System.currentTimeMillis() + "}}");
				if(sessionResult.getList("result").size() > 0)
				{
					DataMap sessionJSON = sessionResult.getObject("result.0");
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
	*/
	
	/*
	protected ArrayList<UserProfile> listUserProfiles(DataMap filter) throws RedbackException
	{
		ArrayList<UserProfile> list = new ArrayList<UserProfile>();
		try
		{
			DataList userConfigs = listUserConfigs(filter);
			for(int i = 0; i < userConfigs.size(); i++)
			{
				DataMap userConfig = userConfigs.getObject(i);
				UserProfile userProfile = null;
				for(int j = 0 ; j < cachedUserProfiles.size(); j++)
					if(cachedUserProfiles.get(j).getUsername().equals(userConfig.getString("username")))
						userProfile = cachedUserProfiles.get(j);
				if(userProfile == null)
				{
					DataList rolesList = userConfig.getList("roles");
					DataMap rights = new DataMap();
					for(int j = 0; j < rolesList.size(); j++)
					{
						String roleName = rolesList.getString(j);
						Role role = getRole(roleName);
						DataMap roleRights = role.getAllRights();
						mergeRights(rights, roleRights);
					}
					userConfig.put("rights", rights);
					userProfile = new UserProfile(userConfig);	
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
	*/
	/*
	protected DataList listUserConfigs(DataMap filter) throws RedbackException
	{
		DataList list = new DataList();
		try
		{
			if(dataService != null)
			{
				DataMap userResult = getData("rbam_user", filter.toString());
				for(int i = 0; i < userResult.getList("result").size(); i++)
				{
					list.add(userResult.getList("result").get(i));
				}			
			}
			if(config.containsKey("users"))
			{
				DataList userResult = config.getList("users");
				for(int i = 0; i < userResult.size(); i++)
				{
					if(userResult.getObject(i).matches(filter))
						list.add(userResult.get(i));
				}			
			}
		}
		catch(Exception e)
		{
			logger.severe(e.getMessage());
			throw new RedbackException("Exception listing user profiles : ", e);
		}
		return list;
	}	
	*/
	
	protected void extendSession(Session session)
	{
		/*
		if(session.expiry < System.currentTimeMillis() + (expiryTime / 2))
		{
			long newExpiry = System.currentTimeMillis() + expiryTime;
			session.expiry = newExpiry;
			publishData("rbam_session", "{_id:\""+ session.sessionId.toString()+"\", expiry:" + newExpiry + "}");
		}
		*/
	}
	
	protected void mergeRights(DataMap to, DataMap from)
	{
		Iterator<String> it = from.keySet().iterator();
		while(it.hasNext())
		{
			String key = it.next();
			DataEntity e = from.get(key);
			if(e instanceof DataMap)
			{
				DataMap fromSub = (DataMap)e;
				if(!fromSub.keySet().isEmpty())
				{
					DataMap toSub = to.getObject(key);
					if(toSub == null)
					{
						toSub = new DataMap();
						to.put(key, toSub);
					}
					mergeRights(toSub, (DataMap)e);
				}
			}
			else if(e instanceof DataLiteral)
			{
				String fromRight = ((DataLiteral)e).getString();
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
