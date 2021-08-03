package io.redback.utils;

import java.util.ArrayList;
import java.util.List;

import javax.mail.internet.InternetAddress;

import io.firebus.utils.DataEntity;
import io.firebus.utils.DataList;
import io.firebus.utils.DataLiteral;
import io.firebus.utils.DataMap;

public class Email {
	public InternetAddress[] to;
	public InternetAddress from;
	public InternetAddress reply;
	public String subject;
	public String body;
	public List<EmailAttachment> attachments;
	
	public Email(InternetAddress[] t, InternetAddress fr, String s, String b, List<EmailAttachment> f) {
		to = t;
		from = fr;
		subject = s;
		body = b;
		attachments = f;
	}
	
	public Email(InternetAddress[] t, InternetAddress fr, InternetAddress r, String s, String b, List<EmailAttachment> f) {
		to = t;
		from = fr;
		reply = r;
		subject = s;
		body = b;
		attachments = f;
	}
	
	public Email(DataMap map) {
		if(map.get("to") instanceof DataList) {
			DataList toList = map.getList("to");
			to = new InternetAddress[toList.size()];
			for(int i = 0; i < toList.size(); i++) {
				to[i] = toInternetAddress(toList.get(i));
			}
		} else if(map.get("to") instanceof DataEntity) {
			to = new InternetAddress[1];
			to[0] = toInternetAddress(map.get("to"));
		}
		from = toInternetAddress(map.get("from"));
		reply = toInternetAddress(map.get("reply"));
		subject = map.getString("subject");
		body = map.getString("body");					
		DataList attList = map.getList("attachments");
		if(attList != null) {
			attachments = new ArrayList<EmailAttachment>();
			for(int i = 0; i < attList.size(); i++) {
				if(attList.get(i) instanceof DataLiteral) {
					attachments.add(new EmailAttachment(attList.getString(i)));	
				} else if(attList.get(i) instanceof DataMap) {
					DataMap attMap = attList.getObject(i);
					attachments.add(new EmailAttachment(attMap.getString("base64"), attMap.getString("filename"), attMap.getString("mime")));	
				}
			}
		}
	}
	
	protected InternetAddress toInternetAddress(DataEntity entity) {
		try {
			if(entity instanceof DataLiteral) {
				return new InternetAddress(entity.toString());
			} else if(entity instanceof DataMap) {
				DataMap addr = (DataMap)entity;
				return new InternetAddress(addr.getString("address"), addr.getString("name"));
			}
		} catch(Exception e) {}
		return null;
	}
	
	public DataMap toDataMap() {
		DataMap map = new DataMap();
		if(to != null && to.length > 0) {
			DataList toList = new DataList();
			for(int i = 0; i < to.length; i++) {
				DataMap toMap = new DataMap();
				toMap.put("address", to[i].getAddress());
				toMap.put("name", to[i].getPersonal());
				toList.add(toMap);
			}
			map.put("to", toList);
		}
		if(from != null) {
			DataMap fromMap = new DataMap();
			fromMap.put("address", from.getAddress());
			fromMap.put("name", from.getPersonal());
			map.put("from", fromMap);
		}
		if(reply != null) {
			DataMap replyMap = new DataMap();
			replyMap.put("address", reply.getAddress());
			replyMap.put("name", reply.getPersonal());
			map.put("reply", replyMap);			
		}
		map.put("subject", subject);
		map.put("body", body);
		if(attachments != null && attachments.size() > 0) {
			DataList attList = new DataList();
			for(EmailAttachment att: attachments) {
				if(att.fileUid != null) {
					attList.add(att.fileUid);	
				} else {
					DataMap attMap = new DataMap();
					attMap.put("base64", att.base64Content);
					attMap.put("filename", att.filename);
					attMap.put("mime", att.mime);
					attList.add(attMap);
				}	
			}
			map.put("attachments", attList);
		}
		return map;
	}
}
