package io.redback.utils;

import java.util.ArrayList;
import java.util.List;

import io.firebus.utils.DataList;
import io.firebus.utils.DataMap;

public class Email {
	public List<String> addresses;
	public String fromAddress;
	public String fromName;
	public String subject;
	public String body;
	public List<String> attachments;
	
	public Email(List<String> a, String fa, String fn, String s, String b, List<String> f) {
		addresses = a;
		fromAddress = fa;
		fromName = fn;
		subject = s;
		body = b;
		attachments = f;
	}
	
	public Email(DataMap map) {
		DataList addressList = map.getList("addresses");
		addresses = new ArrayList<String>();
		for(int i = 0; i < addressList.size(); i++) {
			addresses.add(addressList.getString(i));
		}
		fromAddress = map.getString("fromaddress");
		fromName = map.getString("fromname");
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
	
	public DataMap toDataMap() {
		DataMap map = new DataMap();
		if(addresses != null && addresses.size() > 0) {
			DataList addList = new DataList();
			for(String address: addresses)
				addList.add(address);
			map.put("addresses", addList);
		}
		map.put("fromname", fromName);
		map.put("fromaddress", fromAddress);
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
