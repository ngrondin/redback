package io.redback.services.impl;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
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

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.utils.DataList;
import io.firebus.utils.DataMap;
import io.redback.RedbackException;
import io.redback.client.DataClient;
import io.redback.services.FileServer;
import io.redback.utils.CollectionConfig;
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
		DataMap filter1 = new DataMap();
		filter1.put(linkCollection.getField("object"), object);
		filter1.put(linkCollection.getField("objectuid"), uid);
		DataMap resp1 = dataClient.getData(linkCollection.getName(), filter1, null);
		DataList filter2List = new DataList();
		for(int i = 0; i < resp1.getList("result").size(); i++) 
			filter2List.add(resp1.getList("result").getObject(i).getString("fileuid"));
		DataMap filter2 = new DataMap(fileCollection.getField("fileuid"), new DataMap("$in", filter2List));
		DataMap resp2 = dataClient.getData(fileCollection.getName(), filter2, null);
		List<RedbackFile> list = new ArrayList<RedbackFile>();
		for(int i = 0; i < resp2.getList("result").size(); i++) 
		{
			DataMap fileInfo = fileCollection.convertObjectToCanonical(resp2.getList("result").getObject(i));
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
		    DataMap resp = dataClient.getData(fileCollection.getName(), new DataMap(fileCollection.getField("hash"), hashStr), null);
		    if(resp.getList("result").size() == 0) 
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
					thumbnail = getBase64ThumbnailOfImage(bytes);
					data.put(fileCollection.getField("thumbnail"), thumbnail);
				} else if(mime.equals("application/pdf")) {
					thumbnail = getBase64ThumbnailOfPDF(bytes);
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
				DataMap fileInfo = fileCollection.convertObjectToCanonical(resp.getList("result").getObject(0));
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
			DataMap data = new DataMap();
			String linkId = firebus.requestService(idGeneratorService, new Payload(idName)).getString();
			data.put(linkCollection.getField("fileuid"), fileUid);
			data.put(linkCollection.getField("object"), object);
			data.put(linkCollection.getField("objectuid"), uid);
			dataClient.putData(linkCollection.getName(), new DataMap(linkCollection.getField("linkid"), linkId), data);
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
	
	public String getBase64ThumbnailOfImage(byte[] bytes) throws RedbackException 
	{
		String b64img = null;
		try
		{
			BufferedImage orig = ImageIO.read(new ByteArrayInputStream(bytes));
			return getBase64Thumbnail(orig);
		}
		catch(Exception e) 
		{
		}
		return b64img;
	}
	
	public String getBase64ThumbnailOfPDF(byte[] bytes) throws RedbackException
	{
		try {
			PDDocument doc = PDDocument.load(new ByteArrayInputStream(bytes));
		    PDFRenderer renderer = new PDFRenderer(doc);
		    BufferedImage img = renderer.renderImage(0, 0.1f);
		    return getBase64Thumbnail(img);
		} catch(Exception e) {
			throw new RedbackException("Error reading PDF", e);
		}
	}
	
	public String getBase64Thumbnail(BufferedImage orig) throws RedbackException
	{
		try
		{
			int newHeight = 80;
			int newWidth = orig.getWidth() / (orig.getHeight() / newHeight);
			BufferedImage img = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
			Graphics2D gc = img.createGraphics();
			gc.setColor(Color.WHITE);
			gc.fillRect(0, 0, newWidth, newHeight);
			gc.drawImage(orig.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH),0,0,null);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(img, "png", baos);
			return ("data:image/png;base64, " + (new String(Base64.getEncoder().encode(baos.toByteArray()), "UTF-8")));
		}
		catch(Exception e) 
		{
			throw new RedbackException("Error creating thumbnail", e);
		}
	}
	

	public void clearCaches() 
	{
		
	}


}
