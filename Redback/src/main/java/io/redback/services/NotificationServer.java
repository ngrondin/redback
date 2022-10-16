package io.redback.services;

import java.util.List;
import java.util.logging.Logger;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.information.ServiceInformation;
import io.firebus.data.DataException;
import io.firebus.data.DataList;
import io.firebus.data.DataMap;
import io.redback.exceptions.RedbackException;
import io.redback.security.Session;
import io.redback.services.common.AuthenticatedServiceProvider;
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
			DataMap request = payload.getDataMap();
			String action = request.getString("action");
			if(action != null) {
				if(action.equals("sendemail")) {
					sendEmail(session, new Email(request));
					response = new Payload(new DataMap("result", "ok"));
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
						response = new Payload(new DataMap("result", result));
					} else {
						response = new Payload(new DataMap("result", new DataList()));
					}
				} else if(action.equals("registerfcmtoken")) {
					String token = request.getString("token");
					registerFCMToken(session, token);
					response = new Payload(new DataMap("result", "ok"));
				} else if(action.equals("sendfcmmessage")) {
					String username = request.getString("username");
					String message = request.getString("message");
					String subject = request.getString("subject");
					DataMap data = request.getObject("data");
					sendFCMMessage(session, username, subject, message, data);
					response = new Payload(new DataMap("result", "ok"));
				} else if(action.equals("sendsmsmessage")) {
					String phonenumber = request.getString("phonenumber");
					String senderId = request.getString("senderid");
					String message = request.getString("message");
					sendSMSMessage(session, phonenumber, senderId, message);
					response = new Payload(new DataMap("result", "ok"));
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

	protected abstract void registerFCMToken(Session session, String token) throws RedbackException;
	
	protected abstract void sendFCMMessage(Session session, String username, String subject, String message, DataMap data) throws RedbackException;
	
	protected abstract void sendSMSMessage(Session session, String phonenumber, String senderId, String message) throws RedbackException;
}
