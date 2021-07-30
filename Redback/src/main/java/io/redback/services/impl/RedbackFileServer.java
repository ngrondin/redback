package io.redback.services.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Date;
import java.util.Formatter;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.StreamEndpoint;
import io.firebus.utils.DataList;
import io.firebus.utils.DataMap;
import io.firebus.utils.StreamPipe;
import io.firebus.utils.StreamReceiver;
import io.firebus.utils.StreamSender;
import io.redback.client.DataClient;
import io.redback.exceptions.RedbackException;
import io.redback.security.Session;
import io.redback.services.FileServer;
import io.redback.utils.CollectionConfig;
import io.redback.utils.ImageUtils;
import io.redback.utils.RedbackFile;
import io.redback.utils.RedbackFileMetaData;

public class RedbackFileServer extends FileServer 
{
	private Logger logger = Logger.getLogger("io.redback");
	
	//protected FileManager fileManager;
	protected ArrayList<String> fileServices = new ArrayList<String>();
	protected String defaultFileService;
	protected String defaultFileStream;
	protected String idGeneratorService; 
	protected String idName; 
	protected CollectionConfig fileCollection;
	protected CollectionConfig linkCollection;
	protected DataClient dataClient;
	//protected MessageDigest messageDigest;

	public RedbackFileServer(String n, DataMap c, Firebus f) 
	{
		super(n, c, f);
		idGeneratorService = config.getString("idgeneratorservice");
		idName = config.getString("idname");
		if(config.containsKey("fileadapters")) {
			DataList list = config.getList("fileadapters");
			fileServices = new ArrayList<String>();
			for(int i = 0; i < list.size(); i++)
				fileServices.add(list.getString(i));
		}
		defaultFileService = config.getString("defaultadapter");
		defaultFileStream = config.getString("defaultstreamadapter");
		fileCollection = new CollectionConfig(config.getObject("filecollection"));
		if(config.containsKey("linkcollection"))
			linkCollection = new CollectionConfig(config.getObject("linkcollection"));
		dataClient = new DataClient(firebus, config.getString("dataservice"));
		enableStream = config.getBoolean("enablestream");
		if(enableStream) {
			firebus.registerStreamProvider(n, this, 10);
		}
	}

	public void configure() {

	}

	public void start() {
		
	}	

	public RedbackFileMetaData getMetadata(String fileUid) throws RedbackException
	{
		try {
			DataMap resp = dataClient.getData(fileCollection.getName(), new DataMap(fileCollection.getField("fileuid"), fileUid), null);
			if(resp.getList("result").size() > 0)
			{
				DataMap fileInfo = fileCollection.convertObjectToCanonical(resp.getList("result").getObject(0));
				return new RedbackFileMetaData(fileInfo);
			}
			else
			{
				throw new RedbackException("File not found " + fileUid);
			}
		} catch(Exception e) {
			throw new RedbackException("Error getting file", e);
		}
	}
	
	public RedbackFileMetaData getMetadata(File file) throws RedbackException
	{
		try {
			RedbackFileMetaData filemd = null;
		    FileInputStream fis = new FileInputStream(file);
		    MessageDigest localDigest = MessageDigest.getInstance("SHA-1");
		    byte[] byteArray = new byte[1024];
		    int bytesCount = 0; 
		    while ((bytesCount = fis.read(byteArray)) != -1) {
		    	localDigest.update(byteArray, 0, bytesCount);
		    };
		    fis.close();
			byte[] hash = localDigest.digest();
			Formatter formatter = new Formatter();
		    for (byte b : hash) 
		        formatter.format("%02x", b);
		    String hashStr = formatter.toString();
		    formatter.close();
		    
			if(linkCollection != null) { // This is because with the link collection, files can be reused. Without it, files have to be re-created every time
			    DataMap resp = dataClient.getData(fileCollection.getName(), new DataMap(fileCollection.getField("hash"), hashStr), null);
			    if(resp.getList("result").size() > 0) {
			    	DataMap fileInfo = fileCollection.convertObjectToCanonical(resp.getList("result").getObject(0));
			    	filemd = new RedbackFileMetaData(fileInfo);
			    }
			}
			if(filemd == null) {
				filemd = new RedbackFileMetaData(null, null, null, null, null, null, hashStr);
			}
			return filemd;
		} catch(Exception e) {
			throw new RedbackException("Error getting file metadata", e);
		}
	}
	
	public RedbackFile getFile(String fileUid) throws RedbackException
	{
		try {
			RedbackFileMetaData filemd = getMetadata(fileUid);
			if(defaultFileStream != null) {
				StreamEndpoint sep = getFileStreamEndpoint(fileUid);
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				new StreamReceiver(baos, sep).sync();
				return new RedbackFile(filemd, baos.toByteArray());	
			} else if (defaultFileService != null){
				Payload filePayload = firebus.requestService(defaultFileService, new Payload(fileUid), 10000);
				return new RedbackFile(filemd, filePayload.getBytes());				
			} else {
				return null;
			}
		} catch(Exception e) {
			throw new RedbackException("Error getting file", e);
		}
	}
	
	public StreamEndpoint getFileStreamEndpoint(String fileUid) throws RedbackException
	{
		try {
			DataMap streamReq = new DataMap();
			streamReq.put("action", "get");
			streamReq.put("filename", fileUid);
			StreamEndpoint sep = firebus.requestStream(defaultFileStream, new Payload(streamReq.toString()), 5000);
			return sep;
		} catch(Exception e) {
			throw new RedbackException("Error getting file stream", e);
		}
	}
	
	public List<RedbackFileMetaData> listFilesFor(String object, String uid) throws RedbackException
	{
		DataMap fileFilter = null;
		if(linkCollection != null) {
			DataMap linkFilter = new DataMap();
			linkFilter.put(linkCollection.getField("object"), object);
			linkFilter.put(linkCollection.getField("objectuid"), uid);
			DataMap linksData = dataClient.getData(linkCollection.getName(), linkFilter, null);
			DataList fileFilterList = new DataList();
			for(int i = 0; i < linksData.getList("result").size(); i++) {
				DataMap linkData = linksData.getList("result").getObject(i);
				DataMap linkCanonicalData = linkCollection.convertObjectToCanonical(linkData);
				fileFilterList.add(linkCanonicalData.getString("fileuid"));
			}
			fileFilter = new DataMap(fileCollection.getField("fileuid"), new DataMap("$in", fileFilterList));
		} else {
			fileFilter = new DataMap();
			fileFilter.put(fileCollection.getField("object"), object);
			fileFilter.put(fileCollection.getField("objectuid"), uid);
		}
		DataMap filesData = dataClient.getData(fileCollection.getName(), fileFilter, null);
		List<RedbackFileMetaData> list = new ArrayList<RedbackFileMetaData>();
		for(int i = 0; i < filesData.getList("result").size(); i++) 
		{
			DataMap fileInfo = fileCollection.convertObjectToCanonical(filesData.getList("result").getObject(i));
			RedbackFileMetaData filemd = new RedbackFileMetaData(fileInfo);
			list.add(filemd);
		}
		return list;
	}
	

	public RedbackFileMetaData putFile(String fileName, String mime, String username, byte[] bytes) throws RedbackException
	{
		try {
			String tempFileName = UUID.randomUUID().toString();
			File file = new File(tempFileName);
			Files.write(file.toPath(), bytes);
			return putFile(fileName, mime, username, file);
		} catch(Exception e) {
			throw new RedbackException("Error putting file", e);
		}
	}
	
	public RedbackFileMetaData putFile(String fileName, String mime, String username, File file) throws RedbackException
	{
		try {
			RedbackFileMetaData filemd = getMetadata(file);
		    if(filemd.fileuid == null) 
		    {
				filemd.fileuid = firebus.requestService(idGeneratorService, new Payload(idName)).getString();
				if(mime.startsWith("image")) {
					filemd.thumbnail = ImageUtils.getBase64ThumbnailOfImage(Files.readAllBytes(file.toPath()));
				} else if(mime.equals("application/pdf")) {
					filemd.thumbnail = ImageUtils.getBase64ThumbnailOfPDF(Files.readAllBytes(file.toPath()));
				} else if(mime.startsWith("video")) {
					filemd.thumbnail = " data:image/png;base64,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Zy9sZGYveG1wLzEuMC8iCiAgICB4bWxuczpHSU1QPSJodHRwOi8vd3d3LmdpbXAub3JnL3htcC8iCiAgICB4bWxuczpkYz0iaHR0cDovL3B1cmwub3JnL2RjL2VsZW1lbnRzLzEuMS8iCiAgICB4bWxuczp0aWZmPSJodHRwOi8vbnMuYWRvYmUuY29tL3RpZmYvMS4wLyIKICAgIHhtbG5zOnhtcD0iaHR0cDovL25zLmFkb2JlLmNvbS94YXAvMS4wLyIKICAgeG1wTU06RG9jdW1lbnRJRD0iZ2ltcDpkb2NpZDpnaW1wOjg2YTQ3ODQxLTZjOTgtNDJmNi04ODVhLTE4MmQyODE3ZTcxNyIKICAgeG1wTU06SW5zdGFuY2VJRD0ieG1wLmlpZDozNGRmNDg0NS03NTE2LTQyZGYtYTFhZS02ODBlOWVkMjU2NjYiCiAgIHhtcE1NOk9yaWdpbmFsRG9jdW1lbnRJRD0ieG1wLmRpZDo4OGU5YmM0OS0wNzAxLTRiODQtOWQ0MS1kYzJhYjdjYmM0ZWMiCiAgIEdJTVA6QVBJPSIyLjAiCiAgIEdJTVA6UGxhdGZvcm09IkxpbnV4IgogICBHSU1QOlRpbWVTdGFtcD0iMTYxOTkzMzU0MTcxNjQ1OSIKICAgR0lNUDpWZXJzaW9uPSIyLjEwLjIyIgogICBkYzpGb3JtYXQ9ImltYWdlL3BuZyIKICAgdGlmZjpPcmllbnRhdGlvbj0iMSIKICAgeG1wOkNyZWF0b3JUb29sPSJHSU1QIDIuMTAiPgogICA8aXB0Y0V4dDpMb2NhdGlvbkNyZWF0ZWQ+CiAgICA8cmRmOkJhZy8+CiAgIDwvaXB0Y0V4dDpMb2NhdGlvbkNyZWF0ZWQ+CiAgIDxpcHRjRXh0OkxvY2F0aW9uU2hvd24+CiAgICA8cmRmOkJhZy8+CiAgIDwvaXB0Y0V4dDpMb2NhdGlvblNob3duPgogICA8aXB0Y0V4dDpBcnR3b3JrT3JPYmplY3Q+CiAgICA8cmRmOkJhZy8+CiAgIDwvaXB0Y0V4dDpBcnR3b3JrT3JPYmplY3Q+CiAgIDxpcHRjRXh0OlJlZ2lzdHJ5SWQ+CiAgICA8cmRmOkJhZy8+CiAgIDwvaXB0Y0V4dDpSZWdpc3RyeUlkPgogICA8eG1wTU06SGlzdG9yeT4KICAgIDxyZGY6U2VxPgogICAgIDxyZGY6bGkKICAgICAgc3RFdnQ6YWN0aW9uPSJzYXZlZCIKICAgICAgc3RFdnQ6Y2hhbmdlZD0iLyIKICAgICAgc3RFdnQ6aW5zdGFuY2VJRD0ieG1wLmlpZDozZjEwMDgyYy0xYjNkLTQzYmItODhlMy02ZWZkOTlhOTQzMTgiCiAgICAgIHN0RXZ0OnNvZnR3YXJlQWdlbnQ9IkdpbXAgMi4xMCAoTGludXgpIgogICAgICBzdEV2dDp3aGVuPSIrMTA6MDAiLz4KICAgIDwvcmRmOlNlcT4KICAgPC94bXBNTTpIaXN0b3J5PgogICA8cGx1czpJbWFnZVN1cHBsaWVyPgogICAgPHJkZjpTZXEvPgogICA8L3BsdXM6SW1hZ2VTdXBwbGllcj4KICAgPHBsdXM6SW1hZ2VDcmVhdG9yPgogICAgPHJkZjpTZXEvPgogICA8L3BsdXM6SW1hZ2VDcmVhdG9yPgogICA8cGx1czpDb3B5cmlnaHRPd25lcj4KICAgIDxyZGY6U2VxLz4KICAgPC9wbHVzOkNvcHlyaWdodE93bmVyPgogICA8cGx1czpMaWNlbnNvcj4KICAgIDxyZGY6U2VxLz4KICAgPC9wbHVzOkxpY2Vuc29yPgogIDwvcmRmOkRlc2NyaXB0aW9uPgogPC9yZGY6UkRGPgo8L3g6eG1wbWV0YT4KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgIAo8P3hwYWNrZXQgZW5kPSJ3Ij8+RBJangAAAYRpQ0NQSUNDIHByb2ZpbGUAACiRfZE9SMNQFIVPU4siFRE7iIhkqE4WREUctQpFqBBqhVYdTF76B00akhQXR8G14ODPYtXBxVlX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";
				}
				filemd.fileName = fileName;
				filemd.mime = mime;
				filemd.username = username;
				filemd.date = new Date();
				dataClient.putData(fileCollection.getName(), new DataMap(fileCollection.getField("fileuid"), filemd.fileuid), fileCollection.convertObjectToSpecific(filemd.getDataMap(true)));
	
				storeFile(filemd.fileuid, file);
				return filemd; 
		    }
		    else
		    {
		    	file.delete();
				return filemd;
		    }
		} catch(Exception e) {
			throw new RedbackException("Error putting file", e);
		}
	}	
	
	public void storeFile(String fileUid, File file) throws RedbackException {
		try {
			if(defaultFileStream != null) {
				FileInputStream fis = new FileInputStream(file);
				DataMap req = new DataMap();
				req.put("filename", fileUid);
				req.put("action", "put");
				StreamEndpoint sep = firebus.requestStream(defaultFileStream, new Payload(req.toString()), 5000);
				new StreamSender(fis, sep, new StreamSender.CompletionListener() {
					public void completed() {
						try {
							sep.close();
							fis.close();
							file.delete();
						} catch(Exception e2) {
							logger.severe("Error sending file to storage service : " + e2.getMessage());
						}
					}

					public void error(String message) {
						completed();					
					}					
				});
			} else if(defaultFileService != null) {
				byte[] bytes = Files.readAllBytes(file.toPath());
				Payload filePayload = new Payload(bytes);
				filePayload.metadata.put("filename", fileUid);
				firebus.publish(defaultFileService, filePayload);
				file.delete();
			}
		} catch(Exception e) {
			throw new RedbackException("Error sending file to storage service", e);
		}
	}
	
	public void linkFileTo(String fileUid, String object, String uid) throws RedbackException {
		try {
			if(linkCollection != null) {
				String linkId = firebus.requestService(idGeneratorService, new Payload(idName)).getString();
				DataMap key = new DataMap(linkCollection.getField("linkid"), linkId);
				DataMap data = new DataMap();
				data.put(linkCollection.getField("fileuid"), fileUid);
				data.put(linkCollection.getField("object"), object);
				data.put(linkCollection.getField("objectuid"), uid);
				dataClient.putData(linkCollection.getName(), key, data);
			} else {
				DataMap key = new DataMap(fileCollection.getField("fileuid"), fileUid);
				DataMap data = new DataMap();
				data.put(fileCollection.getField("object"), object);
				data.put(fileCollection.getField("objectuid"), uid);
				dataClient.putData(fileCollection.getName(), key, data);
			}
		} catch(Exception e) {
			throw new RedbackException("Error linking file");
		}
	}
	
	public void unlinkFileFrom(String fileUid, String object, String uid) throws RedbackException {
		try {
			if(linkCollection != null) {
				DataMap key = new DataMap();
				key.put(linkCollection.getField("fileuid"), fileUid);
				key.put(linkCollection.getField("object"), object);
				key.put(linkCollection.getField("objectuid"), uid);
				dataClient.deleteData(linkCollection.getName(), key);
			} else {
				DataMap key = new DataMap(fileCollection.getField("fileuid"), fileUid);
				DataMap data = new DataMap();
				data.put(fileCollection.getField("object"), null);
				data.put(fileCollection.getField("objectuid"), null);
				dataClient.putData(fileCollection.getName(), key, data);
			}
		} catch(Exception e) {
			throw new RedbackException("Error unlinking file");
		}
		
	}
	
	public String getMimeType(String filename)
	{
		String type = "";
		if(filename.toLowerCase().endsWith(".jpg"))
			type = "image/jpg";
		else if(filename.toLowerCase().endsWith(".png"))
			type = "image/png";
		return type;
	}

	public void acceptGetStream(Session session, StreamEndpoint streamEndpoint, String fileUid) throws RedbackException {
		getMetadata(fileUid);
		StreamEndpoint fileSep = getFileStreamEndpoint(fileUid);
		new StreamPipe(streamEndpoint, fileSep);	
	}

	public void acceptPutStream(Session session, StreamEndpoint streamEndpoint, String filename, int filesize, String mime, String objectname, String objectuid) throws RedbackException {
		try {
			String tempFilename = UUID.randomUUID().toString();
			final File file = new File(tempFilename);
			FileOutputStream fos = new FileOutputStream(file);
			new StreamReceiver(fos, streamEndpoint, new StreamReceiver.CompletionListener() {
				public void completed() {
					try {
						fos.close();
						RedbackFileMetaData filemd = putFile(filename, mime != null ? mime : getMimeType(filename), session.getUserProfile().getUsername(), file);
						if(objectname != null && objectuid != null)
							linkFileTo(filemd.fileuid, objectname, objectuid);
						DataMap resp = new DataMap();
						resp.put("fileuid", filemd.fileuid);
						resp.put("thumbnail", filemd.thumbnail);
						ByteArrayInputStream bais = new ByteArrayInputStream(resp.toString().getBytes());
						new StreamSender(bais, streamEndpoint);
						logger.info("Finished putting file");
					} catch(Exception e) {
						logger.severe("Error putting file : " + e);
					}	
				}
	
				public void error(String message) {
					logger.severe(message);
				}
			});
		} catch(Exception e) {
			throw new RedbackException("Error accepting put stream");
		}
	}

	public void acceptListFilesForStream(Session session, StreamEndpoint streamEndpoint, String objectname, String objectuid) throws RedbackException {
		try {
			List<RedbackFileMetaData> fileData = listFilesFor(objectname, objectuid);
			DataMap resp = new DataMap();
			DataList list = new DataList();
			for(RedbackFileMetaData filemd : fileData) 
				list.add(filemd.getDataMap(true));
			resp.put("list", list);
			ByteArrayInputStream bais = new ByteArrayInputStream(resp.toString().getBytes());
			new StreamSender(bais, streamEndpoint);
		} catch(Exception e) {
			throw new RedbackException("Error accepting list file stream");
		}

	}


}
