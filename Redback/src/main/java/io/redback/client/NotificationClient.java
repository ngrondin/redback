package io.redback.client;


import java.util.ArrayList;
import java.util.List;

import io.firebus.Firebus;
import io.firebus.utils.DataList;
import io.firebus.utils.DataMap;
import io.redback.RedbackException;
import io.redback.security.Session;
import io.redback.utils.Email;

public class NotificationClient extends Client {

	public NotificationClient(Firebus fb, String sn) {
		super(fb, sn);
	}
	
	public void sendEmail(Session session, List<String> addresses, String fromAddress, String fromName, String subject, String body, List<String> attachments) throws RedbackException {
		try {
			DataMap req = new DataMap();
			req.put("action", "sendemail");
			DataList adds = new DataList();
			for(String add : addresses)
				adds.add(add);
			req.put("addresses", adds);
			req.put("fromaddress", fromAddress);
			req.put("fromname", fromName);
			req.put("subject", subject);
			req.put("body", body);
			if(attachments != null) {
				DataList atts = new DataList();
				for(String att : attachments)
					atts.add(att);
				req.put("attachments", atts);
			}
			request(session, req);
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
			DataMap resp = request(session, req);
			DataList result = resp.getList("result");
			for(int i = 0; i < result.size(); i++) {
				emails.add(new Email(result.getObject(i)));
			}
		} catch(Exception e) {
			throw new RedbackException("Error getting emails", e);
		}
		return emails;
	}


}
