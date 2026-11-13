package io.redback.services;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.data.DataMap;
import io.firebus.information.ServiceInformation;
import io.redback.exceptions.RedbackException;
import io.redback.security.Session;
import io.redback.services.common.AuthenticatedServiceProvider;

/**
 * Write-only secrets service. A secret value goes in and never comes back out through this service.
 *
 * Actions:
 * - put: name, value, optional domain. An empty value clears the secret.
 * - status: name, optional domain. Returns set, updated and updatedby, never the value.
 */
public abstract class SecretServer extends AuthenticatedServiceProvider {

	public SecretServer(String n, DataMap c, Firebus f) {
		super(n, c, f);
	}

	public ServiceInformation getServiceInformation() {
		return null;
	}

	public Payload redbackAuthenticatedService(Session session, Payload payload) throws RedbackException {
		try {
			DataMap request = payload.getDataMap();
			String action = request.getString("action");
			String name = request.getString("name");
			String domain = request.getString("domain") != null ? request.getString("domain") : session.getUserProfile().getDefaultDomain();
			if("put".equals(action)) {
				put(session, domain, name, request.getString("value"));
				return new Payload(new DataMap("result", "ok"));
			} else if("status".equals(action)) {
				return new Payload(status(session, domain, name));
			} else {
				throw new RedbackException("Unknown action");
			}
		} catch(Exception e) {
			throw new RedbackException("Error in secret service", e);
		}
	}

	public Payload redbackUnauthenticatedService(Session session, Payload payload) throws RedbackException {
		throw new RedbackException("All secret requests need to be authenticated");
	}

	protected abstract void put(Session session, String domain, String name, String value) throws RedbackException;

	protected abstract DataMap status(Session session, String domain, String name) throws RedbackException;
}
