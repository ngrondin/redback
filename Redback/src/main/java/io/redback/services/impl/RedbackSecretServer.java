package io.redback.services.impl;

import io.firebus.Firebus;
import io.firebus.data.DataMap;
import io.redback.exceptions.RedbackException;
import io.redback.managers.secretmanager.SecretManager;
import io.redback.security.Session;
import io.redback.services.SecretServer;

public class RedbackSecretServer extends SecretServer {
	protected SecretManager secretManager;

	public RedbackSecretServer(String n, DataMap c, Firebus f) throws RedbackException {
		super(n, c, f);
		secretManager = new SecretManager(c, f);
	}

	protected void put(Session session, String domain, String name, String value) throws RedbackException {
		secretManager.put(session, domain, name, value);
	}

	protected DataMap status(Session session, String domain, String name) throws RedbackException {
		return secretManager.status(session, domain, name);
	}
}
