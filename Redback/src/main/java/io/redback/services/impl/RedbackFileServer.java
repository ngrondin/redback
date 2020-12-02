package io.redback.services.impl;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.Formatter;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.jpeg.JpegDirectory;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.utils.DataList;
import io.firebus.utils.DataMap;
import io.redback.RedbackException;
import io.redback.client.DataClient;
import io.redback.services.FileServer;
import io.redback.utils.CollectionConfig;
import io.redback.utils.ImageUtils;
import io.redback.utils.RedbackFile;

public class RedbackFileServer extends FileServer 
{
	protected ArrayList<String> fileServices = new ArrayList<String>();
	protected String defaultFileService;
	protected String idGeneratorService; 
	protected String idName; 
	protected CollectionConfig fileCollection;
	protected CollectionConfig linkCollection;
	protected DataClient dataClient;
	protected MessageDigest messageDigest;
	
	public RedbackFileServer(String n, DataMap c, Firebus f) 
	{
		super(n, c, f);
		idGeneratorService = c.getString("idgeneratorservice");
		idName = c.getString("idname");
		DataList list = c.getList("fileadapters");
		fileServices = new ArrayList<String>();
		for(int i = 0; i < list.size(); i++)
			fileServices.add(list.getString(i));
		defaultFileService = config.getString("defaultadapter");
		fileCollection = new CollectionConfig(c.getObject("filecollection"));
		if(c.containsKey("linkcollection"))
			linkCollection = new CollectionConfig(c.getObject("linkcollection"));
		dataClient = new DataClient(firebus, config.getString("dataservice"));
		try {
			messageDigest = MessageDigest.getInstance("SHA-1");
		} catch(Exception e) {
		}
	}

	public RedbackFile getFile(String fileUid) throws RedbackException
	{
		try {
			RedbackFile file = null;
			DataMap resp = dataClient.getData(fileCollection.getName(), new DataMap(fileCollection.getField("fileuid"), fileUid), null);
			if(resp.getList("result").size() > 0)
			{
				DataMap fileInfo = fileCollection.convertObjectToCanonical(resp.getList("result").getObject(0));
				String fileName = fileInfo.getString("filename");
				String mime = fileInfo.getString("mime");
				String thumbnail = fileInfo.getString("thumbnail");
				String username = fileInfo.getString("user");
				Date date = fileInfo.getDate("date");
				String fileAdapter = fileInfo.getString("endpoint");
				if(fileAdapter == null)
					fileAdapter = defaultFileService;
				Payload filePayload = firebus.requestService(fileAdapter, new Payload(fileUid), 10000);
				file = new RedbackFile(fileUid, fileName, mime, thumbnail, username, date, filePayload.getBytes());
			}
			else
			{
				throw new RedbackException("File not found " + fileUid);
			}
			return file;
		} catch(Exception e) {
			throw new RedbackException("Error getting file", e);
		}
	}
	
	public RedbackFile getMetadata(String fileUid) throws RedbackException
	{
		try {
			RedbackFile file = null;
			DataMap resp = dataClient.getData(fileCollection.getName(), new DataMap(fileCollection.getField("fileuid"), fileUid), null);
			if(resp.getList("result").size() > 0)
			{
				DataMap fileInfo = fileCollection.convertObjectToCanonical(resp.getList("result").getObject(0));
				String fileName = fileInfo.getString("filename");
				String mime = fileInfo.getString("mime");
				String thumbnail = fileInfo.getString("thumbnail");
				String username = fileInfo.getString("user");
				Date date = fileInfo.getDate("date");
				file = new RedbackFile(fileUid, fileName, mime, thumbnail, username, date, null);
			}
			else
			{
				throw new RedbackException("File not found " + fileUid);
			}
			return file;
		} catch(Exception e) {
			throw new RedbackException("Error getting file", e);
		}
	}
	
	public List<RedbackFile> listFilesFor(String object, String uid) throws RedbackException
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
		List<RedbackFile> list = new ArrayList<RedbackFile>();
		for(int i = 0; i < filesData.getList("result").size(); i++) 
		{
			DataMap fileInfo = fileCollection.convertObjectToCanonical(filesData.getList("result").getObject(i));
			String fileUid = fileInfo.getString("fileuid");
			String fileName = fileInfo.getString("filename");
			String mime = fileInfo.getString("mime");
			String thumbnail = fileInfo.getString("thumbnail");
			String username = fileInfo.getString("user");
			Date date = fileInfo.getDate("date");
			RedbackFile file = new RedbackFile(fileUid, fileName, mime, thumbnail, username, date, null);
			list.add(file);
		}
		return list;
	}

	public RedbackFile putFile(String fileName, String mime, String username, byte[] bytes) throws RedbackException
	{
		try {
			byte[] hash = messageDigest.digest(bytes);
			Formatter formatter = new Formatter();
		    for (byte b : hash) 
		        formatter.format("%02x", b);
		    String hashStr = formatter.toString();
		    formatter.close();
		    
			DataMap existingFileData = null;
			if(linkCollection != null) { // This is because with the link collection, files can be reused. Without it, files have to be re-created every time
			    DataMap resp = dataClient.getData(fileCollection.getName(), new DataMap(fileCollection.getField("hash"), hashStr), null);
			    if(resp.getList("result").size() > 0) 
			    	existingFileData = resp.getList("result").getObject(0);
			}
			
		    if(existingFileData == null) 
		    {
				String fileUid = firebus.requestService(idGeneratorService, new Payload(idName)).getString();
				Date date = new Date();
				String thumbnail = null;
				DataMap data = new DataMap();
				data.put(fileCollection.getField("filename"), fileName);
				data.put(fileCollection.getField("mime"), mime);
				data.put(fileCollection.getField("user"), username);
				data.put(fileCollection.getField("date"), new Date());
				data.put(fileCollection.getField("hash"), hashStr);
				if(mime.startsWith("image")) {
					thumbnail = ImageUtils.getBase64ThumbnailOfImage(bytes);
					data.put(fileCollection.getField("thumbnail"), thumbnail);
				} else if(mime.equals("application/pdf")) {
					thumbnail = ImageUtils.getBase64ThumbnailOfPDF(bytes);
					data.put(fileCollection.getField("thumbnail"), thumbnail);
				}
				dataClient.putData(fileCollection.getName(), new DataMap(fileCollection.getField("fileuid"), fileUid), data);
	
				Payload filePayload = new Payload(bytes);
				filePayload.metadata.put("filename", fileUid);
				firebus.publish(defaultFileService, filePayload);
				return new RedbackFile(fileUid, fileName, mime, thumbnail, username, date, null);
		    }
		    else
		    {
				DataMap fileInfo = fileCollection.convertObjectToCanonical(existingFileData);
				String fileUid = fileInfo.getString("fileuid");
				String thumbnail = fileInfo.getString("thumbnail");
				Date date = fileInfo.getDate("date");
				RedbackFile file = new RedbackFile(fileUid, fileName, mime, thumbnail, username, date, null);
				return file;
		    }
		} catch(Exception e) {
			throw new RedbackException("Error putting file", e);
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


	public void clearCaches() 
	{
		
	}


}
