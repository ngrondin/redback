package io.redback.services.common;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.StreamEndpoint;
import io.firebus.data.DataMap;
import io.redback.client.AccessManagementClient;
import io.redback.exceptions.RedbackException;
import io.redback.security.Session;

public abstract class AuthenticatedDualProvider extends DualProvider{
	protected String accessManagementService;
	protected AccessManagementClient accessManagementClient;

	public AuthenticatedDualProvider(String n, DataMap c, Firebus f) {
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
	
	public Payload redbackAcceptStream(Session session, Payload payload, StreamEndpoint streamEndpoint) throws RedbackException {
		if(AuthenticationHelper.authenticateSession(session, payload, accessManagementClient)) {
			return redbackAcceptAuthenticatedStream(session, payload, streamEndpoint);
		} else {
			return redbackAcceptUnauthenticatedStream(session, payload, streamEndpoint);
		}
	}

	public abstract Payload redbackAuthenticatedService(Session session, Payload payload) throws RedbackException;

	public abstract Payload redbackUnauthenticatedService(Session session, Payload payload) throws RedbackException;	
	
	public abstract Payload redbackAcceptAuthenticatedStream(Session session, Payload payload, StreamEndpoint streamEndpoint) throws RedbackException;

	public abstract Payload redbackAcceptUnauthenticatedStream(Session session, Payload payload, StreamEndpoint streamEndpoint) throws RedbackException;
}