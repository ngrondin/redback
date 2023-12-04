package io.redback.utils;

import io.firebus.data.DataMap;

public class RedbackAttachment {
	
	public RedbackFileMetaData metadata;
	public String objectname;
	public String uid;
	
	public RedbackAttachment(RedbackFileMetaData md, String on, String i) {
		metadata = md;
		objectname = on;
		uid = i;
	}

	public DataMap getDataMap(boolean addThumbnail) {
		DataMap fileInfo = new DataMap();
		fileInfo.put("fileuid", metadata.fileuid);
		fileInfo.put("filename", metadata.fileName);
		fileInfo.put("mime", metadata.mime);
		fileInfo.put("username", metadata.username);
		fileInfo.put("date", metadata.date != null ? metadata.date.toInstant().toString() : null);
		fileInfo.put("hash", metadata.hash);
		if(addThumbnail) 
			fileInfo.put("thumbnail", metadata.thumbnail);
		fileInfo.put("object", objectname);
		fileInfo.put("objectuid", uid);
		return fileInfo;
	}
}
