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
	public String subject;
	public String body;
	public List<String> attachments;
	
	public Email(InternetAddress[] t, InternetAddress fr, String s, String b, List<String> f) {
		to = t;
		from = fr;
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
		} else if(map.get("to") instanceof DataLiteral) {
			to = new InternetAddress[1];
			to[0] = toInternetAddress(map.get("to"));
		}
		from = toInternetAddress(map.get("from"));
		subject = map.getString("subject");
		body = map.getString("body");					
		DataList attList = map.getList("attachments");
		if(attList != null) {
			attachments = new ArrayList<String>();
			for(int i = 0; i < attList.size(); i++) {
				attachments.add(attList.getString(i));
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
		map.put("subject", subject);
		map.put("body", body);
		if(attachments != null && attachments.size() > 0) {
			DataList attList = new DataList();
			for(String fileUid: attachments)
				attList.add(fileUid);
			map.put("attachments", attList);
		}
		return map;
	}
}
