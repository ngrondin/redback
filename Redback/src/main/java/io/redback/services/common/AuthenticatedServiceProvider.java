package io.redback.services.common;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.data.DataMap;
import io.redback.client.AccessManagementClient;
import io.redback.exceptions.RedbackException;
import io.redback.security.Session;

public abstract class AuthenticatedServiceProvider extends ServiceProvider{
	protected String accessManagementService;
	protected AccessManagementClient accessManagementClient;

	public AuthenticatedServiceProvider(String n, DataMap c, Firebus f) {
		super(n, c, f);
		accessManagementService = config.getString("accessmanagementservice");
		accessManagementClient = new AccessManagementClient(firebus, accessManagementService);
	}
	
	protected Payload redbackService(Session session, Payload payload) throws RedbackException {
		if(AuthenticationHelper.authenticateSession(session, payload, accessManagementClient)) {
			return redbackAuthenticatedService(session, payload);
		} else {
			return redbackUnauthenticatedService(session, payload);
		}
	}

	public abstract Payload redbackAuthenticatedService(Session session, Payload payload) throws RedbackException;

	public abstract Payload redbackUnauthenticatedService(Session session, Payload payload) throws RedbackException;	
}