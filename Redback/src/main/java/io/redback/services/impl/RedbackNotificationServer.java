package io.redback.services.impl;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.Flags.Flag;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.search.FlagTerm;
import javax.mail.util.ByteArrayDataSource;

import com.sun.mail.imap.IMAPFolder;

import io.firebus.Firebus;
import io.firebus.utils.DataMap;
import io.redback.RedbackException;
import io.redback.client.FileClient;
import io.redback.security.Session;
import io.redback.services.NotificationServer;
import io.redback.utils.Email;
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

	protected void email(Session session, List<String> addresses, String fromAddress, String fromName, String subject, String body, List<String> attachments) throws RedbackException {

	}

	public void clearCaches() {
		
	}

	@Override
	protected void sendEmail(Session session, Email email) throws RedbackException {
		Thread worker = new Thread() {
			public void run() {
				try {
					if(email.attachments != null && email.attachments.size() > 0)
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
			        msg.setFrom(email.from);
			        msg.setRecipients(Message.RecipientType.TO, email.to);
			        msg.setSubject(email.subject);
			        if(email.attachments == null) {
			        	msg.setText(email.body); 
			        } else {
			            Multipart multipart = new MimeMultipart();

			            BodyPart messageBodyPart = new MimeBodyPart();
			            messageBodyPart.setText(email.body);
			            multipart.addBodyPart(messageBodyPart);

			            for(String fileUid: email.attachments) {
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

	
	protected List<Email> getEmails(Session session, String server, String username, String password, String folderName) throws RedbackException {
    	List<Email> emails = new ArrayList<Email>();
        try 
        {
			Properties props = System.getProperties();
			props.setProperty("mail.store.protocol", "imaps");
			
			javax.mail.Session mailSession = javax.mail.Session.getDefaultInstance(props, null);
			Store store = mailSession.getStore("imaps");
			store.connect(server, username, password);
			IMAPFolder folder = (IMAPFolder) store.getFolder(folderName);
			folder.open(Folder.READ_WRITE);
			
			Message[] messages = folder.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));
			for (int i=0; i < messages.length;i++) 
			{
				Message msg =  messages[i];
				String body = null;
				List<String> attachments = null;
				Object content = msg.getContent();
				if(content instanceof String) {
					body = (String)content;
				} else if(content instanceof MimeMultipart) {
					attachments = new ArrayList<String>();
					MimeMultipart mmp = (MimeMultipart)content;
					for(int j = 0; j < mmp.getCount(); j++) {
						BodyPart bp = mmp.getBodyPart(j);
						if(bp.isMimeType("text/plain")) {
							body = (String)bp.getContent();
						} else if(bp.isMimeType("text/html")) {

						} else if(bp.isMimeType("multipart/ALTERNATIVE")) {

						} else {
							InputStream is = bp.getInputStream();
						    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
						    int read;
						    while((read = is.read()) != -1)
						    	buffer.write(read);
						    RedbackFile file = fileClient.putFile(session, bp.getFileName(), bp.getContentType(), session.getUserProfile().getUsername(), buffer.toByteArray());
						    attachments.add(file.uid);
						}
					}
				}
				Email email = new Email((InternetAddress[])msg.getAllRecipients(), (InternetAddress)msg.getFrom()[0], msg.getSubject(), body, attachments);
				emails.add(email);
				msg.setFlag(Flag.SEEN, true);
			}
			folder.close(true);
			store.close();
		}
		catch(Exception e) {
			throw new RedbackException("Error getting emails", e);
		} 		
		return emails;
	}

	protected void registerFCMToken(Session session, String token) throws RedbackException {
		System.out.println("Registering token: " + token + "");
	}

	protected void sendFCMMessage(Session session, String username, String message) throws RedbackException {		// TODO Auto-generated method stub
		
	}

}
