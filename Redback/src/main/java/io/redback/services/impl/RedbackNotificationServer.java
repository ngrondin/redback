package io.redback.services.impl;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Flags.Flag;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.search.FlagTerm;
import javax.mail.util.ByteArrayDataSource;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.RSAKeyProvider;
import com.sun.mail.imap.IMAPFolder;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.data.DataList;
import io.firebus.data.DataMap;
import io.firebus.exceptions.FunctionErrorException;
import io.firebus.logging.Logger;
import io.redback.client.DataClient;
import io.redback.client.FileClient;
import io.redback.client.GatewayClient;
import io.redback.exceptions.RedbackException;
import io.redback.security.Session;
import io.redback.services.NotificationServer;
import io.redback.utils.CollectionConfig;
import io.redback.utils.Email;
import io.redback.utils.EmailAttachment;
import io.redback.utils.RedbackFile;
import io.redback.utils.RedbackFileMetaData;
import io.redback.utils.StringUtils;

public class RedbackNotificationServer extends NotificationServer {
	
	protected String smtpServer;
	protected String smtpUser;
	protected String smtpPass;
	protected String dataServiceName;
	protected DataClient dataClient;
	protected CollectionConfig userCollection;
	protected CollectionConfig deviceCollection;
	protected String fileServiceName;
	protected FileClient fileClient;
	protected String gatewayServiceName;
	protected String smsServiceName;
	protected GatewayClient gatewayClient;
	protected DataMap fcmConfig;
	protected String fcmAccessToken;
	protected long fcmAccessTokenExpiry;

	public RedbackNotificationServer(String n, DataMap c, Firebus f) {
		super(n, c, f);
		smtpServer = config.getString("smtpserver");
		smtpUser = config.getString("smtpuser");
		smtpPass = config.getString("smtppassword");
		dataServiceName = config.getString("dataservice");
		dataClient = new DataClient(firebus, dataServiceName);
		userCollection = new CollectionConfig(dataClient, config.getObject("usercollection"), "rbns_users");
		deviceCollection = new CollectionConfig(dataClient, config.getObject("devicecollection"), "rbns_devices");
		fileServiceName = config.getString("fileservice");
		fileClient = new FileClient(firebus, fileServiceName);
		gatewayServiceName = config.getString("gatewayservice");
		gatewayClient = new GatewayClient(firebus, gatewayServiceName);
		fcmConfig = config.getObject("fcmconfig");
		smsServiceName = config.getString("smsservice");
	}

	protected void email(Session session, List<String> addresses, String fromAddress, String fromName, String subject, String body, List<String> attachments) throws RedbackException {

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
			        prop.put("mail.smtp.socketFactory.port", "587");
			        prop.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");		        
			        prop.put("mail.smtp.auth", "true");
			        prop.put("mail.smtp.port", "587"); 
			        prop.put("mail.transport.protocol", "smtp");
			
			        javax.mail.Session mailSession = javax.mail.Session.getInstance(prop, new javax.mail.Authenticator() {
			        	protected PasswordAuthentication getPasswordAuthentication() {
			        		return new PasswordAuthentication(smtpUser,smtpPass);
			        	}
			        });
			        
			        String bodyType = StringUtils.isHtml(email.body) ? "text/html; charset=utf-8" : "text/plain";
			        Logger.fine("rb.notif.sendemail");
			        Message msg = new MimeMessage(mailSession);
			        msg.setFrom(email.from);
			        if(email.reply != null)
			        	msg.setReplyTo(new Address[] {email.reply});
			        msg.setRecipients(Message.RecipientType.TO, email.to);
			        msg.setSubject(email.subject);
			        if(email.attachments == null) {
			        	msg.setContent(email.body, bodyType);
			        } else {
			            Multipart multipart = new MimeMultipart();

			            BodyPart messageBodyPart = new MimeBodyPart();
			            messageBodyPart.setContent(email.body, bodyType);
			            multipart.addBodyPart(messageBodyPart);

			            for(EmailAttachment att: email.attachments) {
				            BodyPart fileBodyPart = new MimeBodyPart();
			            	if(att.fileUid != null) {
			            		RedbackFile file = fileClient.getFile(session, att.fileUid);
					            DataSource source = new ByteArrayDataSource(file.bytes, file.metadata.mime);
					            fileBodyPart.setDataHandler(new DataHandler(source));
					            fileBodyPart.setFileName(file.metadata.fileName);
			            	} else {
			            		byte[] bytes = Base64.getDecoder().decode(att.base64Content);
					            DataSource source = new ByteArrayDataSource(bytes, att.mime);
					            fileBodyPart.setDataHandler(new DataHandler(source));
					            fileBodyPart.setFileName(att.filename);
			            	}
				            multipart.addBodyPart(fileBodyPart);			            		
			            }
			            msg.setContent(multipart);
			        }
			        msg.setSentDate(new Date());
			        Transport.send(msg);	
				} catch(Exception e) {
					Logger.severe("rb.notif.sendemail", "Error sending emails", e);
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
				List<EmailAttachment> attachments = null;
				Object content = msg.getContent();
				if(content instanceof String) {
					body = (String)content;
				} else if(content instanceof MimeMultipart) {
					attachments = new ArrayList<EmailAttachment>();
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
						    RedbackFileMetaData filemd = fileClient.putFile(session, bp.getFileName(), bp.getContentType(), session.getUserProfile().getUsername(), buffer.toByteArray());
						    EmailAttachment att = new EmailAttachment(filemd.fileuid);
						    attachments.add(att);
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
		if(dataClient != null && userCollection != null) {
			userCollection.putData(new DataMap("_id", session.getUserProfile().getUsername()), new DataMap("fcmtoken", token));
		} else {
			throw new RedbackException("No data client was configured");
		}
	}
	
	protected void removeFCMToken(Session session, String username, String deviceId) throws RedbackException {
		if(dataClient != null && userCollection != null) {
			DataMap data = new DataMap("fcmtoken", null);
			if(deviceId != null) {
				deviceCollection.putData(new DataMap("_id", deviceId), data);				
			} else if(username != null) {
				userCollection.putData(new DataMap("_id", username), data);
			}
		} else {
			throw new RedbackException("No data client was configured");
		}		
	}
	
	private String getFCMAccessToken() throws RedbackException {
		try {
			long now = System.currentTimeMillis();
			if(fcmAccessToken == null || fcmAccessTokenExpiry < now) {
				String keyStr = fcmConfig.getString("private_key");
				keyStr = keyStr.replaceAll("\\n", "").replace("-----BEGIN PRIVATE KEY-----", "").replace("-----END PRIVATE KEY-----", "");
				byte[] pkcs8EncodedKey = Base64.getDecoder().decode(keyStr);
				KeyFactory factory = KeyFactory.getInstance("RSA");
				KeySpec ks = new PKCS8EncodedKeySpec(pkcs8EncodedKey);//fcmAccountKey.getString("private_key").getBytes());
				final RSAPrivateKey privateKey = (RSAPrivateKey)factory.generatePrivate(ks);
				Algorithm algorithm = Algorithm.RSA256(new RSAKeyProvider() {
					public String getPrivateKeyId() {return fcmConfig.getString("private_key_id");}
					public RSAPublicKey getPublicKeyById(String keyId) {return null;}
					public RSAPrivateKey getPrivateKey() { return privateKey; }
				});
			    String token = JWT.create()
			    		.withIssuer(fcmConfig.getString("client_email"))
			    		.withAudience(fcmConfig.getString("token_uri"))
			    		.withExpiresAt(new Date((new Date()).getTime() + 1800000))
			    		.withClaim("scope", "https://www.googleapis.com/auth/cloud-platform")
			    		.withClaim("iat", ((new Date()).getTime() / 1000))
			    		.sign(algorithm);	
			    DataMap form = new DataMap();
			    form.put("grant_type", "urn:ietf:params:oauth:grant-type:jwt-bearer");
			    form.put("assertion", token);
			    DataMap resp = gatewayClient.postForm(fcmConfig.getString("token_uri"), form, null, null);
			    fcmAccessToken = resp.getString("access_token");
			    fcmAccessTokenExpiry = now + (resp.getNumber("expires_in").longValue() * 1000);
			} 
		    return fcmAccessToken;
		} catch(Exception e) {
			throw new RedbackException("Error generating FCM token", e);
		}
	}

	protected void sendFCMMessage(Session session, String username, String subject, String message, DataMap data) throws RedbackException {
		if(gatewayClient != null && dataClient != null) {
			try {
				String accessToken = getFCMAccessToken();	
				Date latestlogin = null;
				String fcmToken = null;
				String deviceId = null;
				DataMap deviceResults = deviceCollection.getData(new DataMap("username", username));
				DataList list = deviceResults.getList("result");
				if(list != null && list.size() > 0) {
					for(int i = 0; i < list.size(); i++) {
						String t = list.getObject(i).getString("fcmtoken");
						if(t != null) {
							Date lli = list.getObject(i).getDate("lastlogin");
							String did = list.getObject(i).getString("_id");
							if(latestlogin == null || (latestlogin != null && lli != null && latestlogin.getTime() < lli.getTime())) {
								latestlogin = lli;
								deviceId = did;
								fcmToken = t;
							}
						}
					}
				}
				if(fcmToken == null) {
					DataMap userResults = userCollection.getData(new DataMap("_id", username));
					list = userResults.getList("result");
					if(list != null && list.size() > 0) {
						for(int i = 0; i < list.size(); i++) {
							String t = list.getObject(i).getString("fcmtoken");
							if(t != null) {
								fcmToken = t;
								break;
							}
						}
					}
				}
				if(fcmToken != null) {
					DataMap fcmDataPart = data != null ? data : new DataMap();
					fcmDataPart.put("sound", "default");
					DataMap fcmNotificationPart = new DataMap();
					fcmNotificationPart.put("title", subject);
					fcmNotificationPart.put("body", message);
					DataMap fcmAndroidPart = new DataMap();
					fcmAndroidPart.put("priority", "high");
					DataMap fcmAPNSPart = new DataMap();
					fcmAPNSPart.put("headers", new DataMap("apns-priority", "5"));
					DataMap fcmMessage = new DataMap();
					fcmMessage.put("token", fcmToken);
					fcmMessage.put("data", fcmDataPart);
					fcmMessage.put("notification", fcmNotificationPart);
					fcmMessage.put("android", fcmAndroidPart);
					fcmMessage.put("apns", fcmAPNSPart);
					DataMap body = new DataMap("message", fcmMessage);
					DataMap headers = new DataMap();
					headers.put("Content-Type", "application/json");
					headers.put("Authorization", "Bearer " + accessToken);
					try {
						gatewayClient.post("https://fcm.googleapis.com/v1/projects/" + fcmConfig.getString("project_id") + "/messages:send", body, headers, null);
					} catch(Exception e) {
						Throwable t = e;
						while(t != null && !(t instanceof FunctionErrorException)) t = t.getCause();
						if(t != null) {
							if(t.getMessage().contains("\"errorCode\": \"UNREGISTERED\"") || t.getMessage().contains("\"errorCode\": \"INVALID_ARGUMENT\"")) {
								removeFCMToken(session, username, deviceId);
							}
						}								
					}
				} 		
			} catch(Exception e) {
				throw new RedbackException("Error sending FCM message", e);
			}
		} else {
			throw new RedbackException("Gateway client or Data client not configured");
		}
	}

	protected void sendSMSMessage(Session session, String phonenumber, String senderId, String message) throws RedbackException {
		if(smsServiceName != null) {
			try {
				DataMap request = new DataMap();
				request.put("phonenumber", phonenumber);
				request.put("message", message);
				if(senderId != null)
					request.put("senderid", senderId);
				Payload requestPayload = new Payload(request);
				requestPayload.metadata.put("session", session.getId());
				firebus.requestService(smsServiceName, requestPayload);
			} catch(Exception e) {
				throw new RedbackException("Error sending sms", e);
			}			
		} else {
			throw new RedbackException("SMS adapter not configured");
		}

	}

}
