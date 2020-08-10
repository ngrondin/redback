package io.redback.utils;

import java.util.Date;

public class RedbackFile {
	
	public String uid;
	public String fileName;
	public String mime;
	public String thumbnail;
	public String username;
	public Date date;
	public byte[] bytes;
	
	public RedbackFile(String u, String fn, String m, String tn, String un, Date d, byte[] b) {
		uid = u;
		fileName = fn;
		mime = m;
		thumbnail = tn;
		username = un;
		date = d;
		bytes = b;
	}

}
