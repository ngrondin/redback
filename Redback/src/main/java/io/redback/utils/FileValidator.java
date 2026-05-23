package io.redback.utils;

import static java.util.Map.entry;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;

public class FileValidator {
	
	protected static Map<String, byte[]> magicNumbers = Map.ofEntries(
		entry("pdf", new byte[] { 0x25, 0x50, 0x44, 0x46 }),
		entry("jpg", new byte[] { (byte)0xff, (byte)0xd8, (byte)0xff, (byte)0xe0 }),
		entry("gif", new byte[] { 0x47, 0x49, 0x46, 0x38 }),
		entry("png", new byte[] { (byte) 0x89, 0x50, 0x4e, 0x47 }),
		entry("tiff", new byte[] { 0x49, 0x49 }),
		entry("bmp", new byte[] { 0x42, 0x4d }),
		entry("rar", new byte[] { 0x52, 0x61, 0x72, 0x21 }),
		entry("zip", new byte[] { 0x50, 0x4b }),
		entry("xsl", new byte[] { (byte)0xD0, (byte)0xCF, 0x11, (byte)0xE0, (byte)0xA1, (byte)0xB1, (byte)0x1A, (byte)0xE1 }),
		entry("parquet", new byte[] { 0x50, 0x41, 0x52, 0x31 })
	);
	
	public static String getFileTypeFromMagicNumber(File file) {
		try {
			byte[] bytes = new byte[10];
			FileInputStream fis = new FileInputStream(file.getPath());
			int len = fis.read(bytes);
			fis.close();
			for(String key: magicNumbers.keySet()) {
				byte[] mnBytes = magicNumbers.get(key);
				boolean match = true;
				if(mnBytes.length < len) {
					for(int i = 0; i < mnBytes.length; i++) 
						if(mnBytes[i] != bytes[i])
							match = false;
				} else {
					match = false;
				}
				if(match) {
					return key;
				}
			}
			return null;
		} catch(IOException e) {
			return null;
		}
	}
}
