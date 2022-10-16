package io.redback.client.js;

import java.util.List;

import io.firebus.data.DataList;
import io.firebus.data.DataMap;
import io.redback.client.NotificationClient;
import io.redback.exceptions.RedbackException;
import io.redback.security.Session;
import io.redback.utils.Email;
import io.redback.utils.js.CallableJSWrapper;
import io.redback.utils.js.ObjectJSWrapper;

public class NotificationClientJSWrapper extends ObjectJSWrapper {
	protected NotificationClient notificationClient;
	protected Session session;

	public NotificationClientJSWrapper(NotificationClient nc, Session s)
	{
		super(new String[] {"sendemail", "getemails", "sendfcmmessage"});
		notificationClient = nc;
		session = s;
	}
	
	public Object get(String key) {
		if(key.equals("sendemail")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					DataMap emailDataMap = (DataMap)(arguments[0]);
					Email email = new Email(emailDataMap);
					notificationClient.sendEmail(session, email);
					return null;
				}
			};
		} else if(key.equals("getemails")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					String server = (String)arguments[0];
					String username = (String)arguments[1];
					String password = (String)arguments[2];
					String folder = (String)arguments[3];
					List<Email> emails = notificationClient.getEmails(session, server, username, password, folder);
					DataList result = new DataList();
					for(Email email: emails)
						result.add(email.toDataMap());
					DataMap resp = new DataMap("result", result);
					return resp;
				}
			};
		} else if(key.equals("sendfcmmessage")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					String username = (String)arguments[0];
					String subject = (String)arguments[1];
					String message = (String)arguments[2];
					DataMap data = arguments.length >= 4 ? (DataMap)(arguments[3]) : null;
					notificationClient.sendFCMMessage(session, username, subject, message, data);
					return null;
				}
			};
		} else if(key.equals("sendsmsmessage")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					String phoneNumber = (String)arguments[0];
					String senderId = arguments.length > 2 ? (String)arguments[1] : null;
					String message = (String)arguments[arguments.length > 2 ? 2 : 1];
					notificationClient.sendSMSMessage(session, phoneNumber, senderId, message);
					return null;
				}
			};
		} else {
			return null;
		}
	}	
}
