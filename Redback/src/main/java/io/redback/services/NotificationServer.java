package io.redback.services;

import java.util.ArrayList;
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

public abstract class NotificationServer extends AuthenticatedServiceProvider {

	private Logger logger = Logger.getLogger("io.redback");
	
	public NotificationServer(String n, DataMap c, Firebus f) {
		super(n, c, f);
	}

	public ServiceInformation getServiceInformation() {
		return null;
	}

	public Payload authenticatedService(Session session, Payload payload) throws RedbackException {
		try {
			logger.finer("Notification service start");
			Payload response = null;
			DataMap request = new DataMap(payload.getString());
			String action = request.getString("action");
			if(action != null) {
				if(action.equals("email")) {
					DataList addresses = request.getList("addresses");
					List<String> addList = new ArrayList<String>();
					for(int i = 0; i < addresses.size(); i++) {
						addList.add(addresses.getString(i));
					}
					String subject = request.getString("subject");
					String body = request.getString("body");
					DataList attachments = request.getList("attachments");
					List<String> attList = null;
					if(attachments != null) {
						attList = new ArrayList<String>();
						for(int i = 0; i < attachments.size(); i++) {
							attList.add(attachments.getString(i));
						}
					}
					email(session, addList, subject, body, attList);
					response = new Payload(new DataMap("result", "ok").toString());
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

	public Payload unAuthenticatedService(Session session, Payload payload) throws RedbackException {
		throw new RedbackException("Notification requests need to be authenticated");
	}
	
	protected abstract void email(Session session, List<String> addresses, String subject, String body, List<String> attachments) throws RedbackException;


}
