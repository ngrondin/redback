package io.redback.services;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.StreamEndpoint;
import io.firebus.exceptions.FunctionErrorException;
import io.firebus.utils.DataMap;
import io.redback.RedbackException;
import io.redback.client.AccessManagementClient;
import io.redback.security.Session;
import io.redback.security.UserProfile;

public abstract class AuthenticatedStreamProvider extends StreamProvider {
	private Logger logger = Logger.getLogger("io.redback");
	protected String accessManagementService;
	protected AccessManagementClient accessManagementClient;
	protected Map<StreamEndpoint, Session> endpointToSession;
	protected Map<Session, StreamEndpoint> sessionToEndpoint;
	
	public AuthenticatedStreamProvider(String n, DataMap c, Firebus f) {
		super(n, c, f);
		accessManagementService = config.getString("accessmanagementservice");
		accessManagementClient = new AccessManagementClient(firebus, accessManagementService);
		endpointToSession = new HashMap<StreamEndpoint, Session>();
		sessionToEndpoint = new HashMap<Session, StreamEndpoint>();
	}


	public void acceptStream(Payload payload, StreamEndpoint streamEndpoint) throws FunctionErrorException {
		Session session = new Session(payload.metadata.get("session"));
		UserProfile up = null;
		String token = payload.metadata.get("token");
		try {
			if(token != null)
			{
				up = accessManagementClient.validate(session, token);
			}

			if(up != null) {
				session.setUserProfile(up);
				onNewStream(session);
				endpointToSession.put(streamEndpoint, session);
				sessionToEndpoint.put(session, streamEndpoint);
				streamEndpoint.setHandler(this);
			} else {
				throw new RedbackException("User session cannot be created");
			}
		}
		catch(Exception e)
		{
			throw new FunctionErrorException("Cannot accept stream", e);
		}
	}

	public void receiveStreamData(Payload payload, StreamEndpoint streamEndpoint) {
		Session session = endpointToSession.get(streamEndpoint);
		try {
			onStreamData(session, payload);
		} catch(Exception e) {
			e.printStackTrace();
			logger.severe("Error receiving stream data : " + e.getMessage());
		}
	}
	
	public void sendStreamData(Session session, Payload payload) {
		StreamEndpoint endpoint = sessionToEndpoint.get(session);
		if(endpoint != null) {
			endpoint.send(payload);
		}
	}

	public int getStreamIdleTimeout() {
		return 3600000;
	}

	public void streamClosed(StreamEndpoint streamEndpoint) {
		Session session = endpointToSession.get(streamEndpoint);
		try {
			onStreamClose(session);
			endpointToSession.remove(streamEndpoint);
			sessionToEndpoint.remove(session);
		} catch(Exception e) {
			logger.severe("Error closing the stream data : " + e.getMessage());
		}
	}



	

	protected abstract void onNewStream(Session session) throws RedbackException;

	protected abstract void onStreamData(Session session, Payload payload) throws RedbackException;

	protected abstract void onStreamClose(Session session) throws RedbackException;

}
