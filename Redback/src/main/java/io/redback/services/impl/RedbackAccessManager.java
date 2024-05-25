package io.redback.services.impl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;

import io.firebus.Firebus;
import io.firebus.data.DataEntity;
import io.firebus.data.DataList;
import io.firebus.data.DataLiteral;
import io.firebus.data.DataMap;
import io.firebus.logging.Logger;
import io.firebus.utils.jwt.JWTValidator;
import io.redback.client.ConfigClient;
import io.redback.client.DataClient;
import io.redback.client.GatewayClient;
import io.redback.exceptions.RedbackException;
import io.redback.exceptions.RedbackInvalidRequestException;
import io.redback.security.Role;
import io.redback.security.Session;
import io.redback.security.UserProfile;
import io.redback.services.AccessManager;
import io.redback.utils.CacheEntry;
import io.redback.utils.CollectionConfig;
import io.redback.utils.Convert;

public class RedbackAccessManager extends AccessManager 
{
	//protected String sysusername;
	//protected String secret;
	//protected String issuer;
	//protected String type;
	//protected String outboundService;
	protected String idmUserProfileUrl;
	protected String idmTokenUrl;
	protected String idmClientId;
	protected String idmClientSecret;
	protected DataList hardUsers;
	protected ConfigClient configClient;
	protected DataClient dataClient;
	protected CollectionConfig roleCollection;
	protected CollectionConfig userCollection;
	protected GatewayClient gatewayClient;
	protected JWTValidator jwtValidator;
	protected Map<String, Map<String, Role>> roles;
	protected Map<String, CacheEntry<UserProfile>> cachedUserProfiles;

	public RedbackAccessManager(String n, DataMap c, Firebus f) 
	{
		super(n, c, f);
		jwtValidator = new JWTValidator();
		if(config.containsKey("dataservice")) {
			dataClient = new DataClient(firebus, config.getString("dataservice"));
			roleCollection = new CollectionConfig(config.containsKey("rolecollection") ? config.getString("rolecollection") : "rbam_role");
			if(config.containsKey("usercollection"))
				userCollection = new CollectionConfig(config.getString("usercollection"));
		}
		configClient = new ConfigClient(firebus, config.getString("configservice"));
		if(config.containsKey("outboundservice")) {
			gatewayClient = new GatewayClient(firebus, config.getString("outboundservice"));
		}
		if(config.containsKey("users")) {
			hardUsers = config.getList("users");
		} 
		if(config.containsKey("idm")) {
			idmUserProfileUrl = config.getString("idm.userprofileurl");
			idmTokenUrl = config.getString("idm.tokenurl");
			idmClientId = config.getString("idm.clientid");
			idmClientSecret = config.getString("idm.clientsecret");
		}
		roles = new HashMap<String, Map<String, Role>>();
		cachedUserProfiles = new HashMap<String, CacheEntry<UserProfile>>();
	}
	
	public void configure() {
		cachedUserProfiles.clear();
		roles.clear();
		jwtValidator.clearAll();
		DataMap jwtValidMap = config.getObject("jwtvalidator");
		for(String issuer: jwtValidMap.keySet()) {
			DataMap issuerConfig = jwtValidMap.getObject(issuer);
			if(issuerConfig.containsKey("sharedsecret")) {
				jwtValidator.tryAddSharedSecret(issuer, issuerConfig.getString("sharedsecret"));
			}
			if(issuerConfig.containsKey("keysurl")) {
				try {
					DataMap resp = gatewayClient.get(issuerConfig.getString("keysurl"));
					jwtValidator.addJWK(issuer, resp);
				} catch(Exception e) {
					Logger.severe("rb.am.jwtvalidator", e);
				}
			}
		}
	}

	protected UserProfile validateToken(Session session, String token) throws RedbackException {
		try {
			DecodedJWT jwt = jwtValidator.decode(token);
			jwtValidator.validate(jwt);
			if(jwt.getClaim("email") == null) throw new RedbackInvalidRequestException("Email claim not provided");
			UserProfile profile = getUserProfile(session, jwt);
			profile.setExpiry(jwt.getExpiresAt().getTime());
			return profile;
		} catch (Exception exception) {
			throw new RedbackException("Cannot validate token", exception);
		}
	}
	
	protected String getSysUserToken(Session session) throws RedbackException {
		if(idmTokenUrl == null || idmClientId == null || idmClientSecret == null || gatewayClient == null) 
			throw new RedbackException("Missing configuration to retreive sysuser");
		DataMap req = new DataMap();
		req.put("client_id", idmClientId);
		req.put("client_secret", idmClientSecret);
		req.put("grant_type", "sysuser");
		DataMap resp = gatewayClient.postForm(idmTokenUrl, req);
		return resp.getString("access_token");
	}

	
	protected synchronized UserProfile getUserProfile(Session session, DecodedJWT jwt) throws RedbackException {
		String username = jwt.getClaim("email").asString();
		CacheEntry<UserProfile> ce = cachedUserProfiles.get(username);
		
		if(ce != null && ce.hasExpired()) {
			cachedUserProfiles.remove(username);
			ce = null;
		}
		if(ce != null)  return ce.get();
		
		try {
			DataMap userConfig = new DataMap("username", jwt.getClaim("email").asString());
			Claim roleClaim = jwt.getClaim("rol").isNull() ? jwt.getClaim("roles") : jwt.getClaim("rol");
			Claim domainClaim = jwt.getClaim("dom").isNull() ? jwt.getClaim("domains") : jwt.getClaim("dom");
			Claim attrClaim = jwt.getClaim("attr").isNull() ? jwt.getClaim("attrs") : jwt.getClaim("attr");
			if(!roleClaim.isNull()) {
				List<String> roles = roleClaim.asList(String.class);
				DataList roleList = new DataList();
				for(String role: roles)
					roleList.add(role);
				userConfig.put("roles", roleList);
			}
			if(!domainClaim.isNull()) {
				List<String> domains = domainClaim.asList(String.class);
				DataList domList = new DataList();
				for(String dom: domains)
					domList.add(dom);
				userConfig.put("domains", domList);
			}
			if(!attrClaim.isNull()) {
				if(attrClaim.asMap() != null) {
					Map<String, Object> attrs = attrClaim.asMap();
					userConfig.put("attributes", Convert.mapToDataMap(attrs));					
				} else if(attrClaim.asString() != null) {
					userConfig.put("attributes", new DataMap(attrClaim.asString()));
				}
			}
			
			if(!userConfig.containsKey("roles") || !userConfig.containsKey("domains") || !userConfig.containsKey("attributes")) {
				if(hardUsers != null) {
					for(int i = 0; i < hardUsers.size(); i++)
						if(hardUsers.getObject(i).containsKey("username") && hardUsers.getObject(i).getString("username").equals(username))
							userConfig.merge(hardUsers.getObject(i));
				}
				
				if(userCollection != null && dataClient != null) {
					DataMap userResult = dataClient.getData(userCollection.getName(), userCollection.convertObjectToSpecific(new DataMap("username" , username)), null);
					if(userResult != null && userResult.getList("result") != null && userResult.getList("result").size() > 0)
						userConfig.merge(userResult.getList("result").getObject(0));
				}
				
				if(idmUserProfileUrl != null && idmClientId != null && idmClientSecret != null && gatewayClient != null) {
					DataMap reqBody = new DataMap();
					reqBody.put("user", username);
					reqBody.put("client_id", idmClientId);
					reqBody.put("client_secret", idmClientSecret);
					DataMap resp = gatewayClient.postForm(idmUserProfileUrl, reqBody);
					userConfig.merge(resp);
				}
			}

			DataList rolesList = userConfig.getList("roles");
			DataList domainsList = userConfig.getList("domains");
			DataMap rights = new DataMap();
			for(int i = 0; i < rolesList.size(); i++) {
				String roleName = rolesList.getString(i);
				mergeRoleIntoRights(session, null, roleName, rights);						
				for(int j = 0; j < domainsList.size(); j++) {
					String domain = domainsList.getString(j);
					if(!(domain != null && domain.equals("*"))) {
						mergeRoleIntoRights(session, domain, roleName, rights);
					}
				}
			}
			userConfig.put("rights", rights);
			UserProfile userProfile = new UserProfile(userConfig);
			long expiry = jwt.getExpiresAt().getTime();
			long maxExpiry = System.currentTimeMillis() + (15*60*1000);
			if(expiry > maxExpiry) expiry = maxExpiry;
			userProfile.setExpiry(expiry);
			ce = new CacheEntry<UserProfile>(userProfile, expiry);
			cachedUserProfiles.put(username, ce);
			return userProfile;
		}
		catch(Exception e)
		{
			throw new RedbackException("Error getting user profile for " + username, e);
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

}
