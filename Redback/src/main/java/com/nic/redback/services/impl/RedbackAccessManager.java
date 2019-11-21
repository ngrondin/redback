package com.nic.redback.services.impl;

import java.util.Iterator;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.nic.firebus.Firebus;
import com.nic.firebus.utils.DataEntity;
import com.nic.firebus.utils.DataList;
import com.nic.firebus.utils.DataLiteral;
import com.nic.firebus.utils.DataMap;
import com.nic.redback.RedbackException;
import com.nic.redback.security.Role;
import com.nic.redback.security.Session;
import com.nic.redback.security.UserProfile;
import com.nic.redback.services.AccessManager;

public class RedbackAccessManager extends AccessManager
{

	public RedbackAccessManager(DataMap c, Firebus f) 
	{
		super(c, f);
	}

	protected Session validateToken(String token) throws RedbackException
	{
		Session session = null;
		try 
		{
			DecodedJWT jwt = JWT.decode(token);
			Claim usernameClaim = jwt.getClaim("email");
			String username = usernameClaim.asString();
			UserProfile profile = getUserProfile(username);
			if(profile != null)
				session = new Session(token, profile, jwt.getExpiresAt().getTime());
		} 
		catch (JWTDecodeException  exception)
		{
		    error("JWT token is invalid");
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
					String userCollection = config.containsKey("usertable") ? config.getString("usertable") : "rbam_user";
					DataMap userResult = getData(userCollection, new DataMap("username" , username));
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
						if(role != null)
						{
							DataMap roleRights = role.getAllRights();
							mergeRights(rights, roleRights);
						}
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
				role = null;
			}
		}
		return role;		
	}

	
	protected void extendSession(Session session)
	{

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