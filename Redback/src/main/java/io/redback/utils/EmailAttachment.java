package io.redback.utils;

public class EmailAttachment {
	public String fileUid;
	public String base64Content;
	public String filename;
	public String mime;
	
	public EmailAttachment(String f) {
		fileUid = f;
	}
	
	public EmailAttachment(String c, String n, String m) {
		base64Content = c;
		filename = n;
		mime = m;
	}
}
