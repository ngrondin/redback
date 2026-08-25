package io.redback.test;

import java.io.File;
import java.nio.file.Files;

import io.redback.services.impl.RedbackFileServer;

/**
 * Manual test for RedbackFileServer.getMimeType. Each case writes a temp file
 * that starts with the real magic number of the format (PAR1 for parquet, the
 * png and jpeg headers) and checks the resolved mime. Run the main and look
 * for FAIL lines in the output.
 */
public class GetMimeTypeTest {

	public static void main(String[] args) throws Exception {
		check("file.parquet", new byte[] { 0x50, 0x41, 0x52, 0x31, 0x00, 0x00 }, "application/vnd.apache.parquet");
		check("file.csv", "a,b,c\n1,2,3\n".getBytes(), "text/csv");
		check("file.png", new byte[] { (byte)0x89, 0x50, 0x4e, 0x47, 0x00, 0x00 }, "image/png");
		check("renamed.parquet", new byte[] { (byte)0x89, 0x50, 0x4e, 0x47, 0x00, 0x00 }, "");
		check("file.jpeg", new byte[] { (byte)0xff, (byte)0xd8, (byte)0xff, (byte)0xe0, 0x00, 0x00 }, "image/jpeg");
		check("noextension", new byte[] { 0x00, 0x01, 0x02, 0x03, 0x04, 0x05 }, "");
	}

	protected static void check(String filename, byte[] content, String expected) throws Exception {
		File file = File.createTempFile("mimetest", null);
		Files.write(file.toPath(), content);
		String mime = RedbackFileServer.getMimeType(filename, file);
		file.delete();
		String result = mime.equals(expected) ? "OK  " : "FAIL";
		System.out.println(result + " " + filename + " -> \"" + mime + "\" (expected \"" + expected + "\")");
	}
}
