package io.redback.services.impl;

import java.util.Date;

import io.firebus.Firebus;
import io.firebus.data.DataMap;
import io.redback.exceptions.RedbackException;
import io.redback.managers.queuemanager.QueueManager;
import io.redback.security.Session;
import io.redback.services.QueueServer;

public class RedbackQueueServer extends QueueServer {
	protected QueueManager queueManager;
	
	public RedbackQueueServer(String n, DataMap c, Firebus f) {
		super(n, c, f);
		queueManager = new QueueManager(c, f);
	}
	
	public void start() {
		super.start();
		queueManager.start();
	}

	protected void enqueue(Session session, String service, DataMap message, int timeout, Date schedule, String uniqueKey) throws RedbackException {
		queueManager.enqueue(session, service, message, timeout, schedule, uniqueKey);
		
	}

}
