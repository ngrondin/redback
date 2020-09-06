package io.redback.client;


import java.util.List;

import io.firebus.Firebus;
import io.firebus.utils.DataList;
import io.firebus.utils.DataMap;
import io.redback.RedbackException;
import io.redback.security.Session;

public class NotificationClient extends Client {

	public NotificationClient(Firebus fb, String sn) {
		super(fb, sn);
	}
	
	public void email(Session session, List<String> addresses, String subject, String body, List<String> attachments) throws RedbackException {
		try {
			DataMap req = new DataMap();
			req.put("action", "email");
			DataList adds = new DataList();
			for(String add : addresses)
				adds.add(add);
			req.put("addresses", adds);
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
			throw new RedbackException("Error putting domain report", e);
		}
	}


}
