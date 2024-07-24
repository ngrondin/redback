package io.redback.services.impl;

import io.firebus.Firebus;
import io.firebus.data.DataMap;
import io.redback.exceptions.RedbackException;
import io.redback.managers.aimanager.AIManager;
import io.redback.security.Session;
import io.redback.services.AIServer;
import io.redback.utils.NLCommandResponse;

public class RedbackAIServer extends AIServer {
	protected AIManager aiManager;

	public RedbackAIServer(String n, DataMap c, Firebus f) throws RedbackException  {
		super(n, c, f);
		aiManager = new AIManager(n, config, firebus);
	}
	
	public void configure() {

	}	

	protected NLCommandResponse nlCommand(Session session, String model, String text, DataMap context) throws RedbackException {
		return aiManager.runNLCommand(session, model, text, context);
	}

	protected void feedback(Session session, String model, String command, String sequence, int points) throws RedbackException {
		aiManager.feedback(session, model, command, sequence, points);
	}

}
