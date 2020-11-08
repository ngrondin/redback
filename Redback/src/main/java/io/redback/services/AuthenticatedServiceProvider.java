package io.redback.services;

import java.util.logging.Logger;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.utils.DataMap;
import io.redback.RedbackException;
import io.redback.client.AccessManagementClient;
import io.redback.security.Session;
import io.redback.security.UserProfile;

public abstract class AuthenticatedServiceProvider extends ServiceProvider
{
	private Logger logger = Logger.getLogger("io.redback");
	protected String accessManagementService;
	protected AccessManagementClient accessManagementClient;


	public AuthenticatedServiceProvider(String n, DataMap c, Firebus f)
	{
		super(n, c, f);
		accessManagementService = config.getString("accessmanagementservice");
		accessManagementClient = new AccessManagementClient(firebus, accessManagementService);
	}
	
	protected Payload redbackService(Session session, Payload payload) throws RedbackException {
		Payload response = null;
		String token = payload.metadata.get("token");
		UserProfile up = null;
		
		logger.finer("Authenticated service start (token: " + token + ")");

		if(token != null)
			up = accessManagementClient.validate(session, token);

		if(up != null)
		{			
			session.setUserProfile(up);
			session.setToken(token);
			response = redbackAuthenticatedService(session, payload);
		}
		else
		{
			response = redbackUnauthenticatedService(session, payload);
		}
		logger.finer("Authenticated service finish");
		return response;

	}

	public abstract Payload redbackAuthenticatedService(Session session, Payload payload) throws RedbackException;

	public abstract Payload redbackUnauthenticatedService(Session session, Payload payload) throws RedbackException;

	
}
