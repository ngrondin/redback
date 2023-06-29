package io.redback.services.impl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.data.DataEntity;
import io.firebus.data.DataList;
import io.firebus.data.DataLiteral;
import io.firebus.data.DataMap;
import io.redback.client.ConfigClient;
import io.redback.client.DataClient;
import io.redback.exceptions.RedbackException;
import io.redback.exceptions.RedbackInvalidRequestException;
import io.redback.security.Role;
import io.redback.security.Session;
import io.redback.security.UserProfile;
import io.redback.services.AccessManager;
import io.redback.utils.CacheEntry;
import io.redback.utils.CollectionConfig;

public class RedbackAccessManager extends AccessManager 
{
	protected String sysusername;
	protected String secret;
	protected String issuer;
	protected String type;
	protected String outboundService;
	protected String idmUrl;
	protected String idmClientId;
	protected String idmClientSecret;
	protected DataList hardUsers;
	protected DataClient dataClient;
	protected ConfigClient configClient;
	protected CollectionConfig roleCollection;
	protected CollectionConfig userCollection;
	protected Map<String, Map<String, Role>> roles;
	protected Map<String, CacheEntry<UserProfile>> cachedUserProfiles;


	public RedbackAccessManager(String n, DataMap c, Firebus f) 
	{
		super(n, c, f);
		sysusername = config.getString("sysuser");
		secret = config.getString("jwtsecret");
		issuer = config.getString("jwtissuer");
		if(config.containsKey("dataservice")) {
			dataClient = new DataClient(firebus, config.getString("dataservice"));
			roleCollection = new CollectionConfig(config.containsKey("rolecollection") ? config.getString("rolecollection") : "rbam_role");
			if(config.containsKey("usercollection"))
				userCollection = new CollectionConfig(config.getString("usercollection"));
		}
		configClient = new ConfigClient(firebus, config.getString("configservice"));
		if(config.containsKey("users")) {
			type = "hardusers";
			hardUsers = config.getList("users");
		} else if(userCollection != null) {
			type = "data";
			userCollection = new CollectionConfig(config.containsKey("usercollection") ? config.getString("usercollection") : "rbam_user");
		} else if(config.containsKey("idmurl") && config.containsKey("outboundservice")) {
			type = "idm";
			idmUrl = config.getString("idmurl");
			idmClientId = config.getString("idmclientid");
			idmClientSecret = config.getString("idmclientsecret");
			outboundService = config.getString("outboundservice");
		}
		roles = new HashMap<String, Map<String, Role>>();
		cachedUserProfiles = new HashMap<String, CacheEntry<UserProfile>>();
	}

	protected UserProfile validateToken(Session session, String token) throws RedbackException
	{
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
			if(username != null) {
				UserProfile profile = getUserProfile(session, username);
				if(profile == null) 
					profile = createEmptyProfile(username);
				profile.setExpiry(jwt.getExpiresAt().getTime());
				return profile;
			} else {
				throw new RedbackInvalidRequestException("token email is null");
			}
		} 
		catch (RedbackException exception)
		{
			throw new RedbackException("Cannot retrieve the user profile", exception);
		}
		catch (Exception exception)
		{
			throw new RedbackException("JWT token is invalid", exception);
		}
	}
	
	protected synchronized UserProfile getUserProfile(Session session, String username) throws RedbackException
	{
		long now = System.currentTimeMillis();
		CacheEntry<UserProfile> ce = cachedUserProfiles.get(username);
		
		if(ce != null && ce.hasExpired())
		{
			cachedUserProfiles.remove(username);
			ce = null;
		}

		if(ce != null)
		{
			return ce.get();
		}
		else
		{
			try
			{
				DataMap userConfig = null;
				if(sysusername != null && username.equals(sysusername)) 
				{
					userConfig = new DataMap();
					userConfig.put("username", sysusername);
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
					DataMap userResult = dataClient.getData(userCollection.getName(), userCollection.convertObjectToSpecific(new DataMap("username" , username)), null);
					if(userResult != null && userResult.getList("result") != null && userResult.getList("result").size() > 0)
						userConfig = userResult.getList("result").getObject(0);
				}
				else if(type.equals("idm")) {
					DataMap req = new DataMap();
					req.put("method", "post");
					req.put("url", idmUrl);
					DataMap reqBody = new DataMap();
					reqBody.put("user", username);
					reqBody.put("client_id", idmClientId);
					reqBody.put("client_secret", idmClientSecret);
					req.put("body", reqBody);
					try {
						Payload respP = firebus.requestService(outboundService, new Payload(req));
						userConfig = new DataMap(respP.getString());
					} catch(Exception e) {
						if(!e.getMessage().contains("Invalid user id"))
							throw e;
					}
				}
	
				if(userConfig != null)
				{
					DataList rolesList = userConfig.getList("roles");
					DataList domainsList = userConfig.getList("domains");
					DataMap rights = new DataMap();
					for(int i = 0; i < rolesList.size(); i++)
					{
						String roleName = rolesList.getString(i);
						mergeRoleIntoRights(session, null, roleName, rights);						
						for(int j = 0; j < domainsList.size(); j++)
						{
							String domain = domainsList.getString(j);
							if(!(domain != null && domain.equals("*"))) {
								mergeRoleIntoRights(session, domain, roleName, rights);
							}
						}
					}
					userConfig.put("rights", rights);
					UserProfile userProfile = new UserProfile(userConfig);
					ce = new CacheEntry<UserProfile>(userProfile, now + 900000);
					cachedUserProfiles.put(username, ce);
					return userProfile;
				} else {
					return null;
				}
			}
			catch(Exception e)
			{
				throw new RedbackException("Error getting user profile for " + username, e);
			}
		}
	}
	
	
	protected void mergeRoleIntoRights(Session session, String domain, String roleName, DataMap rights) throws RedbackException
	{
		Role role = getRole(session, domain, roleName);
		if(role != null)
		{
			DataMap roleRights = role.getAllRights();
			mergeRights(rights, roleRights);
		}
	}
	
	protected Role getRole(Session session, String domain, String name) throws RedbackException
	{
		Map<String, Role> domainRoles = roles.get(domain);
		if(domainRoles == null) {
			domainRoles = new HashMap<String, Role>();
			roles.put(domain, domainRoles);
		}
		Role role = domainRoles.get(name);
		if(role == null)
		{
			try
			{
				if(domain != null) {
					if(dataClient != null && roleCollection != null) {
						DataMap key = new DataMap();
						key.put("domain", domain);
						key.put("name", name);
						DataMap res = dataClient.getData(roleCollection.getName(), roleCollection.convertObjectToSpecific(key), null);
						if(res.containsKey("result") && res.getList("result").size() > 0) {
							DataMap config = roleCollection.convertObjectToCanonical(res.getList("result").getObject(0));
							role = new Role(config);
						}						
					}
				} else {
					DataMap config = configClient.getConfig(session, "rbam", "role", name);
					if(config != null) {
						role =  new Role(config);
					}
				}
				if(role != null)
					domainRoles.put(name, role);
			}
			catch(Exception e)
			{
				role = null;
			}
		}
		return role;		
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
	
	protected UserProfile createEmptyProfile(String username) 
	{
		DataMap userConfig = new DataMap();
		userConfig.put("username", username);
		userConfig.put("domains", new DataList());
		userConfig.put("roles", new DataList());
		UserProfile userProfile = new UserProfile(userConfig);
		return userProfile;
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
	
	public void configure() {
		this.cachedUserProfiles.clear();
		this.roles.clear();
	}

}
