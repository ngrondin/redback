package io.redback.client;


import java.util.ArrayList;
import java.util.List;

import javax.mail.internet.InternetAddress;

import io.firebus.Firebus;
import io.firebus.data.DataList;
import io.firebus.data.DataMap;
import io.redback.exceptions.RedbackException;
import io.redback.security.Session;
import io.redback.utils.Email;
import io.redback.utils.EmailAttachment;

public class NotificationClient extends Client {

	public NotificationClient(Firebus fb, String sn) {
		super(fb, sn);
	}
	
	public void sendEmail(Session session, InternetAddress[] to, InternetAddress from, String subject, String body, List<String> att) throws RedbackException {
		List<EmailAttachment> attachments = null;
		if(att != null) {
			attachments = new ArrayList<EmailAttachment>();
			for(String fileuid: att) 
				attachments.add(new EmailAttachment(fileuid));
		}
		Email email = new Email(to, from, subject, body, attachments);
		sendEmail(session, email);
	}
	
	public void sendEmail(Session session, Email email) throws RedbackException {
		try {
			DataMap req = email.toDataMap();
			req.put("action", "sendemail");
			requestDataMap(session, req);
		} catch(Exception e) {
			throw new RedbackException("Error sending emails", e);
		}
	}
	
	public List<Email> getEmails(Session session, String server, String username, String password, String folder) throws RedbackException {
		List<Email> emails = new ArrayList<Email>();
		try {
			DataMap req = new DataMap();
			req.put("action", "getemails");
			req.put("server", server);
			req.put("username", username);
			req.put("password", password);
			req.put("folder", folder);
			DataMap resp = requestDataMap(session, req);
			DataList result = resp.getList("result");
			for(int i = 0; i < result.size(); i++) {
				emails.add(new Email(result.getObject(i)));
			}
		} catch(Exception e) {
			throw new RedbackException("Error getting emails", e);
		}
		return emails;
	}

	public void sendFCMMessage(Session session, String username, String subject, String message, DataMap data) throws RedbackException {
		try {
			DataMap req = new DataMap();
			req.put("action", "sendfcmmessage");
			req.put("username", username);
			req.put("subject", subject);
			req.put("message", message);
			if(data != null)
				req.put("data", data);
			requestDataMap(session, req);
		} catch(Exception e) {
			throw new RedbackException("Error sending FCM Message", e);
		}		
	}

	public void sendSMSMessage(Session session, String phoneNumber, String senderId, String message) throws RedbackException {
		try {
			DataMap req = new DataMap();
			req.put("action", "sendsmsmessage");
			req.put("phonenumber", phoneNumber);
			req.put("senderid", senderId);
			req.put("message", message);
			requestDataMap(session, req);
		} catch(Exception e) {
			throw new RedbackException("Error sending SMS Message", e);
		}		
	}
}
