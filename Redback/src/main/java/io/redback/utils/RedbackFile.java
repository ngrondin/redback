package io.redback.utils;


public class RedbackFile {
	
	public RedbackFileMetaData metadata;
	public byte[] bytes;
	
	public RedbackFile(RedbackFileMetaData md, byte[] b) {
		metadata = md;
		bytes = b;
	}

}
