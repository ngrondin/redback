package io.redback.services;

import java.util.List;
import java.util.logging.Logger;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.information.ServiceInformation;
import io.firebus.utils.DataException;
import io.firebus.utils.DataList;
import io.firebus.utils.DataMap;
import io.redback.RedbackException;
import io.redback.security.Session;
import io.redback.utils.Email;

public abstract class NotificationServer extends AuthenticatedServiceProvider {

	private Logger logger = Logger.getLogger("io.redback");
	
	public NotificationServer(String n, DataMap c, Firebus f) {
		super(n, c, f);
	}

	public ServiceInformation getServiceInformation() {
		return null;
	}

	public Payload redbackAuthenticatedService(Session session, Payload payload) throws RedbackException {
		try {
			logger.finer("Notification service start");
			Payload response = null;
			DataMap request = new DataMap(payload.getString());
			String action = request.getString("action");
			if(action != null) {
				if(action.equals("sendemail")) {
					sendEmail(session, new Email(request));
					response = new Payload(new DataMap("result", "ok").toString());
				} else if(action.equals("getemails")) {
					String server = request.getString("server");
					String username = request.getString("username");
					String password = request.getString("password");
					String folder = request.getString("folder");
					List<Email> emails = getEmails(session, server, username, password, folder);
					if(emails != null) {
						DataList result = new DataList();
						for(Email email: emails) {
							result.add(email.toDataMap());
						}
						response = new Payload(new DataMap("result", result).toString());
					} else {
						response = new Payload(new DataMap("result", new DataList()).toString());
					}
				}
			} else {
				throw new RedbackException("No valid action was provided");
			}
	
			logger.finer("Notification service finish");
			return response;	
		} catch(DataException e) {
			throw new RedbackException("Error in Notification server", e);
		}
	}

	public Payload redbackUnauthenticatedService(Session session, Payload payload) throws RedbackException {
		throw new RedbackException("Notification requests need to be authenticated");
	}
	
	protected abstract void sendEmail(Session session, Email email) throws RedbackException;

	protected abstract List<Email> getEmails(Session session, String server, String username, String password, String folder)  throws RedbackException;

}
