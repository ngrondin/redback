package io.redback.managers.objectmanager;

import io.redback.security.Session;

public class Updater {
	public Session session;
	public boolean script;
	
	public Updater(Session se, boolean sc) {
		session = se;
		script = sc;
	}
}
