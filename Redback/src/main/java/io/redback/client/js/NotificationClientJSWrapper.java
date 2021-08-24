package io.redback.client.js;

import java.util.Arrays;
import java.util.List;

import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyArray;
import org.graalvm.polyglot.proxy.ProxyExecutable;
import org.graalvm.polyglot.proxy.ProxyObject;

import io.firebus.utils.DataList;
import io.firebus.utils.DataMap;
import io.redback.client.NotificationClient;
import io.redback.security.Session;
import io.redback.utils.Email;
import io.redback.utils.js.JSConverter;

public class NotificationClientJSWrapper implements ProxyObject {
	
	//private Logger logger = Logger.getLogger("io.redback");
	protected NotificationClient notificationClient;
	protected Session session;
	protected String[] members = {"sendemail", "getemails", "sendfcmmessage"};

	public NotificationClientJSWrapper(NotificationClient nc, Session s)
	{
		notificationClient = nc;
		session = s;
	}
	
	public Object getMember(String key) {
		if(key.equals("sendemail")) {
			return new ProxyExecutable() {
				public Object execute(Value... arguments) {
					DataMap emailDataMap = (DataMap)JSConverter.toJava(arguments[0]);
					Email email = new Email(emailDataMap);
					try
					{
						notificationClient.sendEmail(session, email);
						return null;
					}
					catch(Exception e)
					{
						throw new RuntimeException("Error sending email", e);
					}
				}
			};
		} else if(key.equals("getemails")) {
			return new ProxyExecutable() {
				public Object execute(Value... arguments) {
					String server = arguments[0].asString();
					String username = arguments[1].asString();
					String password = arguments[2].asString();
					String folder = arguments[3].asString();
					try
					{
						List<Email> emails = notificationClient.getEmails(session, server, username, password, folder);
						DataList result = new DataList();
						for(Email email: emails)
							result.add(email.toDataMap());
						DataMap resp = new DataMap("result", result);
						return JSConverter.toJS(resp);
					}
					catch(Exception e)
					{
						throw new RuntimeException("Error sending email", e);
					}
				}
			};
		} else if(key.equals("sendfcmmessage")) {
			return new ProxyExecutable() {
				public Object execute(Value... arguments) {
					String username = arguments[0].asString();
					String subject = arguments[1].asString();
					String message = arguments[2].asString();
					DataMap data = arguments.length >= 4 ? (DataMap)JSConverter.toJava(arguments[3]) : null;
					try
					{
						notificationClient.sendFCMMessage(session, username, subject, message, data);
						return null;
					}
					catch(Exception e)
					{
						throw new RuntimeException("Error sending fcm message", e);
					}
				}
			};
		} else {
			return null;
		}
	}

	public Object getMemberKeys() {
		return ProxyArray.fromArray(((Object[])members));
	}
	
	public boolean hasMember(String key) {
		return Arrays.asList(members).contains(key);
	}

	public void putMember(String key, Value value) {
		
	}
	
	
}
