package io.redback.utils;

import java.util.Date;

import io.firebus.data.DataMap;

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
		fileuid = config.getString("_id");
		if(fileuid == null)
			fileuid = config.getString("fileuid");
		fileName = config.getString("filename");
		mime = config.getString("mime");
		thumbnail = config.getString("thumbnail");
		username = config.getString("username");
		date = config.getDate("date");
		hash = config.getString("hash");
	}
	
	public DataMap getDataMap(boolean addThumbnail) {
		DataMap fileInfo = new DataMap();
		fileInfo.put("fileuid", fileuid);
		fileInfo.put("filename", fileName);
		fileInfo.put("mime", mime);
		fileInfo.put("username", username);
		fileInfo.put("date", date != null ? date.toInstant().toString() : null);
		fileInfo.put("hash", hash);
		if(addThumbnail) 
			fileInfo.put("thumbnail", thumbnail);
		return fileInfo;
	}
	
	public String toString() {
		return "file:" + fileuid;
	}
}
