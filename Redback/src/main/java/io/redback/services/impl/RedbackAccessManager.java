package io.redback.services.impl;

import java.util.Iterator;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.utils.DataEntity;
import io.firebus.utils.DataList;
import io.firebus.utils.DataLiteral;
import io.firebus.utils.DataMap;
import io.redback.RedbackException;
import io.redback.client.ConfigurationClient;
import io.redback.client.DataClient;
import io.redback.security.Role;
import io.redback.security.Session;
import io.redback.security.UserProfile;
import io.redback.services.AccessManager;

public class RedbackAccessManager extends AccessManager 
{
	protected String secret;
	protected String issuer;
	protected String type;
	protected String outboundService;
	protected String idmUrl;
	protected String idmKey;
	protected DataClient dataClient;
	protected DataList hardUsers;
	protected ConfigurationClient configClient;

	public RedbackAccessManager(String n, DataMap c, Firebus f) 
	{
		super(n, c, f);
		secret = config.getString("jwtsecret");
		issuer = config.getString("jwtissuer");
		if(config.containsKey("users")) {
			type = "hardusers";
			hardUsers = config.getList("users");
		} else if(config.containsKey("dataservice")) {
			type = "data";
			dataClient = new DataClient(firebus, config.getString("dataservice"));
		} else if(config.containsKey("idmurl") && config.containsKey("outboundservice")) {
			type = "idm";
			idmUrl = config.getString("idmurl");
			idmKey = config.getString("idmkey");
			outboundService = config.getString("outboundservice");
		}
		configClient = new ConfigurationClient(firebus, config.getString("configservice"));
	}

	protected Session validateToken(String token) throws RedbackException
	{
		Session session = null;
		try 
		{
		    Algorithm algorithm = Algorithm.HMAC256(secret);
			JWTVerifier verifier = JWT.require(algorithm)
	                .withIssuer(issuer)
	                .build();
			verifier.verify(token);
			
			DecodedJWT jwt = JWT.decode(token);
			Claim usernameClaim = jwt.getClaim("email");
			String username = usernameClaim.asString();
			UserProfile profile = getUserProfile(username);
			if(profile != null)
				session = new Session(token, profile, jwt.getExpiresAt().getTime());
		} 
		catch (RedbackException exception)
		{
			error("Cannot retrieve the user profile", exception);
		}
		catch (Exception exception)
		{
		    error("JWT token is invalid", exception);
		}
		return session;
	}
	
	protected UserProfile getUserProfile(String username) throws RedbackException
	{
		UserProfile userProfile = null;
		for(int i = 0; i < cachedUserProfiles.size(); i++)
			if(cachedUserProfiles.get(i).getUsername().equalsIgnoreCase(username))
				userProfile = cachedUserProfiles.get(i);

		if(userProfile == null)
		{
			try
			{
				DataMap userConfig = null;
				if(type.equals("hardusers"))
				{
					for(int i = 0; i < hardUsers.size(); i++)
						if(hardUsers.getObject(i).containsKey("username") && hardUsers.getObject(i).getString("username").equals(username))
							userConfig = hardUsers.getObject(i);
				}
				else if(type.equals("data"))
				{
					String userCollection = config.containsKey("usertable") ? config.getString("usertable") : "rbam_user";
					DataMap userResult = dataClient.getData(userCollection, new DataMap("username" , username), null);
					if(userResult != null && userResult.getList("result") != null && userResult.getList("result").size() > 0)
						userConfig = userResult.getList("result").getObject(0);
				}
				else if(type.equals("idm")) {
					DataMap req = new DataMap();
					req.put("method", "get");
					req.put("url", idmUrl + "?user=" + username);
					req.put("authorization", idmKey);
				    Payload respP = firebus.requestService(outboundService, new Payload(req.toString()));
					userConfig = new DataMap(respP.getString());
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
				role =  new Role(configClient.getConfig("rbam", "role", name));
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
	
	
	public void clearCaches()
	{
		this.cachedSessions.clear();
		this.cachedUserProfiles.clear();
		this.roles.clear();
	}	
}
