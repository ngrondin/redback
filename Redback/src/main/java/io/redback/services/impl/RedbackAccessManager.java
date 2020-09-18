package io.redback.services.impl;

import java.util.ArrayList;
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
import io.redback.security.CachedUserProfile;
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
	//protected ArrayList<Session> cachedSessions;
	protected ArrayList<CachedUserProfile> cachedUserProfiles;


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
		//cachedSessions = new ArrayList<Session>();
		cachedUserProfiles = new ArrayList<CachedUserProfile>();
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
			throw new RedbackException("Cannot retrieve the user profile", exception);
		}
		catch (Exception exception)
		{
			exception.printStackTrace();
			throw new RedbackException("JWT token is invalid", exception);
		}
		return session;
	}
	
	protected synchronized UserProfile getUserProfile(String username) throws RedbackException
	{
		long now = System.currentTimeMillis();
		CachedUserProfile userProfile = null;
		for(int i = 0; i < cachedUserProfiles.size(); i++)
			if(cachedUserProfiles.get(i).getUsername().equalsIgnoreCase(username))
				userProfile = cachedUserProfiles.get(i);
		
		if(userProfile != null && userProfile.expiry < now)
		{
			cachedUserProfiles.remove(userProfile);
			userProfile = null;
		}

		if(userProfile == null)
		{
			try
			{
				DataMap userConfig = null;
				if(username.equals("sysuser@redbackwms.com")) 
				{
					userConfig = new DataMap();
					userConfig.put("username", "sysuser@redbackwms.com");
					DataList doms = new DataList();
					doms.add("*");
					userConfig.put("domains", doms);
					DataList roles = new DataList();
					roles.add("admin");
					roles.add("system");
					userConfig.put("roles", roles);
				}
				else if(type.equals("hardusers"))
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
					userProfile = new CachedUserProfile(userConfig, now + 300000);	
					cachedUserProfiles.add(userProfile);
				}
			}
			catch(Exception e)
			{
				throw new RedbackException("Error getting user profile for " + username, e);
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
		DataMap fromRb = from.getObject("rb");
		DataMap toRb = to.getObject("rb");
		if(toRb == null) {
			toRb = new DataMap();
			to.put("rb", toRb);
		}
		
		Iterator<String> catIt = fromRb.keySet().iterator();
		while(catIt.hasNext())
		{
			String cat = catIt.next();
			DataMap fromCat = fromRb.getObject(cat);
			DataMap toCat = toRb.getObject(cat);
			if(toCat == null) {
				toCat = new DataMap();
				toRb.put(cat, toCat);
			}
			
			Iterator<String> itemIt = fromCat.keySet().iterator();
			while(itemIt.hasNext()) {
				String item = itemIt.next();
				DataEntity fromItem = fromCat.get(item);
				DataEntity toItem = toCat.get(item);
				if(toItem == null) {
					toCat.put(item, fromItem);
				} else {
					DataMap toRights = convertToLongRights(toItem);
					DataMap fromRights = convertToLongRights(fromItem);
					String[] ops = new String[] {"read", "write", "execute"};
					boolean canBeShort = true;
					for(int i = 0; i < ops.length; i++) {
						DataEntity toOp = toRights.get(ops[i]);
						DataEntity fromOp = fromRights.get(ops[i]);
						if(toOp instanceof DataLiteral && ((DataLiteral)toOp).getBoolean() == false || fromOp instanceof DataLiteral && ((DataLiteral)fromOp).getBoolean() == false) {
							toRights.put(ops[i], false);
						} else if(toOp instanceof DataMap && fromOp instanceof DataMap) {
							((DataMap)toOp).merge((DataMap)fromOp);
							canBeShort = false;
						} else if(toOp instanceof DataLiteral && ((DataLiteral)toOp).getBoolean() == true && fromOp instanceof DataLiteral && ((DataLiteral)fromOp).getBoolean() == true) {
							toRights.put(ops[i], true);
						} else {
							toRights.put(ops[i], fromOp);
							canBeShort = false;
						}
					}
					if(canBeShort) {
						String sh = "";
						for(int i = 0; i < ops.length; i++) {
							if(toRights.getBoolean(ops[i]))
								sh += i == 0 ? "r" : i == 1 ? "w" : i == 2 ? "x" : "";
						}
						toCat.put(item, sh);
					} else {
						toCat.put(item, toRights);
					}
				}				
			}
		}
	}
	
	protected DataMap convertToLongRights(DataEntity e)
	{
		if(e instanceof DataMap) {
			return (DataMap)e;
		} else {
			String s = ((DataLiteral)e).getString();
			DataMap ret = new DataMap();
			ret.put("read", s.contains("r"));
			ret.put("write", s.contains("w"));
			ret.put("execute", s.contains("x"));
			return ret;
		} 
	}
	
	
	public void clearCaches()
	{
		//this.cachedSessions.clear();
		this.cachedUserProfiles.clear();
		this.roles.clear();
	}	
}
