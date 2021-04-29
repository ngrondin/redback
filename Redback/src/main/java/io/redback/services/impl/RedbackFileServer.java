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
import io.redback.RedbackException;
import io.redback.client.DataClient;
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


	public void clearCaches() 
	{
		
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
				}
				filemd.fileName = fileName;
				filemd.mime = mime;
				filemd.username = username;
				filemd.date = new Date();
				dataClient.putData(fileCollection.getName(), new DataMap(fileCollection.getField("fileuid"), filemd.fileuid), fileCollection.convertObjectToSpecific(filemd.getDataMap()));
	
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
		StreamEndpoint fileSep = getFileStreamEndpoint(fileUid);
		new StreamPipe(streamEndpoint, fileSep);	
	}

	public void acceptPutStream(Session session, StreamEndpoint streamEndpoint, String filename, String objectname, String objectuid) throws RedbackException {
		try {
			String tempFilename = UUID.randomUUID().toString();
			final File file = new File(tempFilename);
			FileOutputStream fos = new FileOutputStream(file);
			new StreamReceiver(fos, streamEndpoint, new StreamReceiver.CompletionListener() {
				public void completed() {
					try {
						fos.close();
						RedbackFileMetaData filemd = putFile(filename, getMimeType(filename), session.getUserProfile().getUsername(), file);
						if(objectname != null && objectuid != null)
							linkFileTo(filemd.fileuid, objectname, objectuid);
						DataMap resp = new DataMap();
						resp.put("fileuid", filemd.fileuid);
						resp.put("thumbnail", filemd.thumbnail);
						ByteArrayInputStream bais = new ByteArrayInputStream(resp.toString().getBytes());
						new StreamSender(bais, streamEndpoint);
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
				list.add(filemd.getDataMap());
			resp.put("list", list);
			ByteArrayInputStream bais = new ByteArrayInputStream(resp.toString().getBytes());
			new StreamSender(bais, streamEndpoint);
		} catch(Exception e) {
			throw new RedbackException("Error accepting list file stream");
		}

	}


}
