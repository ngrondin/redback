package io.redback.services;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.information.ServiceInformation;
import io.firebus.utils.DataMap;
import io.redback.RedbackException;
import io.redback.security.Session;
import io.redback.services.common.AuthenticatedServiceProvider;
import io.redback.utils.CollectionConfig;

public abstract class UserPreferenceServer extends AuthenticatedServiceProvider {
	
	public UserPreferenceServer(String n, DataMap c, Firebus f) {
		super(n, c, f);
	}

	public ServiceInformation getServiceInformation() {
		return null;
	}

	public Payload redbackAuthenticatedService(Session session, Payload payload) throws RedbackException {
		Payload response = new Payload();
		try {
			DataMap request = new DataMap(payload.getString());
			String action = request.getString("action");
			String type = request.getString("type");
			String name = request.getString("name");
			if(action != null && name != null) {
				if(action.equals("get")) {
					DataMap value = null;
					if(type == null || (type != null && type.equals("user"))) {
						value = getUserPreference(session, name);
					} else if(type.equals("role")) {
						value = getRolePreference(session, name);
					} else if(type.equals("domain")) {
						value = getDomainPreference(session, name);
					}
					response.setData(value != null ? value.toString() : new DataMap().toString());
				} else if(action.equals("put")) {
					DataMap value = request.getObject("value");
					if(type == null || (type != null && type.equals("user"))) {
						putUserPreference(session, name, value);
					} else if(type.equals("role")) {
						putRolePreference(session, name, value);
					} else if(type.equals("domain")) {
						putDomainPreference(session, name, value);
					}
					response.setData(new DataMap("result", "ok").toString());
				} else {
					throw new RedbackException("Unknown action");
				}
			} else {
				throw new RedbackException("Missing action or value name");
			}
		} catch(Exception e) {
			throw new RedbackException("Error in user preference service", e);
		}
		return response;
	}

	public Payload redbackUnauthenticatedService(Session session, Payload payload) throws RedbackException {
		throw new RedbackException("All user preference requests need to be authenticated");
	}

	public void clearCaches() {
		
	}
	
	public abstract DataMap getUserPreference(Session session, String name) throws RedbackException;
	
	public abstract DataMap getRolePreference(Session session, String name) throws RedbackException;

	public abstract DataMap getDomainPreference(Session session, String name) throws RedbackException;

	public abstract void putUserPreference(Session session, String name, DataMap value) throws RedbackException;
	
	public abstract void putRolePreference(Session session, String name, DataMap value) throws RedbackException;
	
	public abstract void putDomainPreference(Session session, String name, DataMap value) throws RedbackException;
}
