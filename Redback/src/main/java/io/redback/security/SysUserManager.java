package io.redback.security;

import java.util.Date;
import java.util.logging.Logger;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

import io.firebus.data.DataMap;
import io.redback.client.AccessManagementClient;
import io.redback.exceptions.RedbackException;

public class SysUserManager {
	private Logger logger = Logger.getLogger("io.redback");
	protected DataMap config;
	protected String jwtSecret;
	protected String jwtIssuer;
	protected String username;
	protected AccessManagementClient accessManagementClient;
	protected UserProfile profile;
	protected String token;
	protected long expiry;

	public SysUserManager(AccessManagementClient amClient, DataMap c) {
		config = c;
		accessManagementClient = amClient;
		username = config.getString("sysusername");
		jwtSecret = config.getString("jwtsecret");
		jwtIssuer = config.getString("jwtissuer");
	}
	
	public String getUsername() {
		return username;
	}
	
	private synchronized void validate(Session session) throws RedbackException {
		long now = System.currentTimeMillis();
		if(profile == null || token == null || expiry < now + 300000)
		{
			try
			{
				expiry = now + 3600000;
				Algorithm algorithm = Algorithm.HMAC256(jwtSecret);
				token = JWT.create()
						.withIssuer(jwtIssuer)
						.withClaim("email", username)
						.withExpiresAt(new Date(expiry))
						.sign(algorithm);
				profile = accessManagementClient.validate(session, token);
				logger.info("New sysuser token: " + token);
			}
			catch(Exception e)
			{
				throw new RedbackException("Error authenticating sys user", e);
			}
		}
		
	}
	
	public UserProfile getProfile(Session session) throws RedbackException  {
		validate(session);
		return profile;
	}
	
	public Session getSession() throws RedbackException {
		return getSession(null);
	}
	
	public Session getSession(String sessionId) throws RedbackException {
		Session session = new Session(sessionId);
		validate(session);
		session.setUserProfile(profile);
		session.setToken(token);
		return session;
	}
}
