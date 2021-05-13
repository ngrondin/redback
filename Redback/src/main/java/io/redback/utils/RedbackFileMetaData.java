package io.redback.utils;

import java.util.Date;

import io.firebus.utils.DataMap;

public class RedbackFileMetaData {
	public String fileuid;
	public String fileName;
	public String mime;
	public String thumbnail;
	public String username;
	public Date date;
	public String hash;
	
	public RedbackFileMetaData(String u, String fn, String m, String tn, String un, Date d, String h) {
		fileuid = u;
		fileName = fn;
		mime = m;
		thumbnail = tn;
		username = un;
		date = d;
		hash = h;
	}
	
	public RedbackFileMetaData(DataMap config) {
		fileuid = config.getString("fileuid");
		fileName = config.getString("filename");
		mime = config.getString("mime");
		thumbnail = config.getString("thumbnail");
		username = config.getString("user");
		date = config.getDate("date");
		hash = config.getString("hash");
	}
	
	public DataMap getDataMap() {
		DataMap fileInfo = new DataMap();
		fileInfo.put("fileuid", fileuid);
		fileInfo.put("filename", fileName);
		fileInfo.put("mime", mime);
		fileInfo.put("thumbnail", thumbnail);
		fileInfo.put("username", username);
		fileInfo.put("date", date != null ? date.toInstant().toString() : null);
		fileInfo.put("hash", hash);
		return fileInfo;
	}
}