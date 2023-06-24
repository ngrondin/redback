package io.redback.services.common;

import io.firebus.Payload;
import io.firebus.threads.FirebusThread;
import io.redback.client.AccessManagementClient;
import io.redback.exceptions.RedbackException;
import io.redback.security.Session;
import io.redback.security.UserProfile;

public class AuthenticationHelper {

	public static boolean authenticateSession(Session session, Payload payload, AccessManagementClient accessManagementClient)  throws RedbackException {
		String token = payload.metadata.get("token");
		String domain = payload.metadata.get("domain");
		UserProfile up = token != null && accessManagementClient != null ? accessManagementClient.validate(session, token) : null;
		if(up != null)
		{			
			session.setUserProfile(up);
			session.setToken(token);
			if(domain != null && (session.getUserProfile().hasAllDomains() || session.getUserProfile().hasDomain(domain)))
				session.setDomainLock(domain);
			if(Thread.currentThread() instanceof FirebusThread) 
				((FirebusThread)Thread.currentThread()).setUser(up.getUsername());
			return true;
		}
		else
		{
			return false;
		}
	}
}
