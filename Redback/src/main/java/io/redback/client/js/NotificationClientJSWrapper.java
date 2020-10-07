package io.redback.client.js;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
//import java.util.logging.Logger;

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
	protected String[] members = {"sendemail", "getemails"};

	public NotificationClientJSWrapper(NotificationClient nc, Session s)
	{
		notificationClient = nc;
		session = s;
	}
	
	public Object getMember(String key) {
		if(key.equals("sendemail")) {
			return new ProxyExecutable() {
				public Object execute(Value... arguments) {
					DataList addresses = (DataList)JSConverter.toJava(arguments[0]);
					String fromAddress = arguments[1].asString();
					String fromName = arguments[2].asString();
					String subject = arguments[3].asString();
					String body = arguments[4].asString();
					DataList attachments = arguments.length >= 6 ? (DataList)JSConverter.toJava(arguments[5]) : null;
					try
					{
						List<String> addList = new ArrayList<String>();
						for(int i = 0; i < addresses.size(); i++) {
							addList.add(addresses.getString(i));
						}
						List<String> attList = null;
						if(attachments != null) {
							attList = new ArrayList<String>();
							for(int i = 0; i < attachments.size(); i++) {
								attList.add(attachments.getString(i));
							}
						}
						notificationClient.sendEmail(session, addList, fromAddress, fromName, subject, body, attList);
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
