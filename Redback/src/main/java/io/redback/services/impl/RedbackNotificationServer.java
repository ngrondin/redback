package io.redback.services.impl;

import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

import io.firebus.Firebus;
import io.firebus.utils.DataMap;
import io.redback.RedbackException;
import io.redback.client.FileClient;
import io.redback.security.Session;
import io.redback.services.NotificationServer;
import io.redback.utils.RedbackFile;

public class RedbackNotificationServer extends NotificationServer {
	private Logger logger = Logger.getLogger("io.redback");
	
	protected String smtpServer;
	protected String smtpUser;
	protected String smtpPass;
	protected String fileServiceName;
	protected FileClient fileClient;

	public RedbackNotificationServer(String n, DataMap c, Firebus f) {
		super(n, c, f);
		smtpServer = config.getString("smtpserver");
		smtpUser = config.getString("smtpuser");
		smtpPass = config.getString("smtppassword");
		fileServiceName = config.getString("fileservice");
		fileClient = new FileClient(firebus, fileServiceName);
	}

	protected void email(Session session, List<String> addresses, String subject, String body, List<String> attachments) throws RedbackException {
		Thread worker = new Thread() {
			public void run() {
				try {
					if(attachments != null && attachments.size() > 0)
						sleep(2000);
					Properties prop = System.getProperties();
			        prop.put("mail.smtp.host", smtpServer); 
			        prop.put("mail.smtp.socketFactory.port", "465");
			        prop.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");		        
			        prop.put("mail.smtp.auth", "true");
			        prop.put("mail.smtp.port", "465"); 
			        prop.put("mail.transport.protocol", "smtp");
			
			        javax.mail.Session mailSession = javax.mail.Session.getInstance(prop, new javax.mail.Authenticator() {
			        	protected PasswordAuthentication getPasswordAuthentication() {
			        		return new PasswordAuthentication(smtpUser,smtpPass);
			        	}
			        });
			        
			        logger.fine("Sending email");
			        Message msg = new MimeMessage(mailSession);
			        msg.setFrom(new InternetAddress("info@redbackwms.com", "RedbackWMS"));
			        msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(String.join(",", addresses), false));
			        msg.setSubject(subject);
			        if(attachments == null) {
			        	msg.setText(body); 
			        } else {
			            Multipart multipart = new MimeMultipart();

			            BodyPart messageBodyPart = new MimeBodyPart();
			            messageBodyPart.setText(body);
			            multipart.addBodyPart(messageBodyPart);

			            for(String fileUid: attachments) {
			            	RedbackFile file = fileClient.getFile(session, fileUid);
				            BodyPart fileBodyPart = new MimeBodyPart();
				            DataSource source = new ByteArrayDataSource(file.bytes, file.mime);
				            fileBodyPart.setDataHandler(new DataHandler(source));
				            fileBodyPart.setFileName(file.fileName);
				            multipart.addBodyPart(fileBodyPart);
			            }
			            
			            msg.setContent(multipart);
			        }
			        msg.setSentDate(new Date());
			        Transport.send(msg);	
				} catch(Exception e) {
					logger.severe("Problem sending email : " + e.getMessage());
				}
			}
		};
		worker.start();
	}

	public void clearCaches() {
		
	}

}
