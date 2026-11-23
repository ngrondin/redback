package io.redback.services.impl;

import io.firebus.Firebus;
import io.firebus.data.DataMap;
import io.firebus.logging.Logger;
import io.redback.exceptions.RedbackException;
import io.redback.managers.secretmanager.SecretManager;
import io.redback.security.Session;
import io.redback.services.SecretServer;

/** With no encryption key set the service still starts: enabled answers false and the other actions refuse. */
public class RedbackSecretServer extends SecretServer {
	protected SecretManager secretManager;

	public RedbackSecretServer(String n, DataMap c, Firebus f) throws RedbackException {
		super(n, c, f);
		if(hasKey(c))
			secretManager = new SecretManager(c, f);
		else
			Logger.warning("rb.secret.nokey", "No secrets key set, the secrets service refuses every request");
	}

	protected void put(Session session, String domain, String name, String value) throws RedbackException {
		manager().put(session, domain, name, value);
	}

	protected DataMap status(Session session, String domain, String name) throws RedbackException {
		return manager().status(session, domain, name);
	}

	protected String get(Session session, String domain, String name) throws RedbackException {
		return manager().get(session, domain, name);
	}

	protected boolean isEnabled() {
		return secretManager != null;
	}

	protected SecretManager manager() throws RedbackException {
		if(secretManager == null)
			throw new RedbackException("No secrets key set");
		return secretManager;
	}

	protected boolean hasKey(DataMap config) {
		DataMap keys = config.getObject("keys");
		if(keys == null) return false;
		for(String id: keys.keySet())
			if(keys.getString(id) != null && keys.getString(id).length() > 0)
				return true;
		return false;
	}
}
