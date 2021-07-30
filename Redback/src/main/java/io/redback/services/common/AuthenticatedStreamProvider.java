package io.redback.services.common;

import java.util.logging.Logger;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.utils.DataMap;
import io.redback.client.AccessManagementClient;
import io.redback.exceptions.RedbackException;
import io.redback.security.Session;
import io.redback.security.UserProfile;

public abstract class AuthenticatedStreamProvider extends StreamProvider {
	private Logger logger = Logger.getLogger("io.redback");
	protected String accessManagementService;
	protected AccessManagementClient accessManagementClient;
	
	public AuthenticatedStreamProvider(String n, DataMap c, Firebus f) {
		super(n, c, f);
		accessManagementService = config.getString("accessmanagementservice");
		accessManagementClient = new AccessManagementClient(firebus, accessManagementService);
	}

	public StreamHandler redbackStream(Session session, Payload payload) throws RedbackException {
		StreamHandler response = null;
		String token = payload.metadata.get("token");
		UserProfile up = null;
		
		logger.finer("Authenticated stream request start (token: " + token + ")");

		if(token != null)
			up = accessManagementClient.validate(session, token);

		if(up != null)
		{			
			session.setUserProfile(up);
			session.setToken(token);
			response = redbackAuthenticatedStream(session, payload);
		}
		else
		{
			response = redbackUnauthenticatedStream(session, payload);
		}
		logger.finer("Authenticated stream request finish");
		return response;
	}

	public abstract StreamHandler redbackAuthenticatedStream(Session session, Payload payload) throws RedbackException;

	public abstract StreamHandler redbackUnauthenticatedStream(Session session, Payload payload) throws RedbackException;

	
}
