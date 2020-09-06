package io.redback.client.js;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyArray;
import org.graalvm.polyglot.proxy.ProxyExecutable;
import org.graalvm.polyglot.proxy.ProxyObject;

import io.firebus.utils.DataList;
import io.redback.client.NotificationClient;
import io.redback.security.Session;
import io.redback.utils.js.JSConverter;

public class NotificationClientJSWrapper implements ProxyObject {
	
	private Logger logger = Logger.getLogger("io.redback");
	protected NotificationClient notificationClient;
	protected Session session;
	protected String[] members = {"email"};

	public NotificationClientJSWrapper(NotificationClient nc, Session s)
	{
		notificationClient = nc;
		session = s;
	}
	
	public Object getMember(String key) {
		if(key.equals("email")) {
			return new ProxyExecutable() {
				public Object execute(Value... arguments) {
					DataList addresses = (DataList)JSConverter.toJava(arguments[0]);
					String subject = arguments[1].asString();
					String body = arguments[2].asString();
					DataList attachments = (DataList)JSConverter.toJava(arguments[3]);
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
						notificationClient.email(session, addList, subject, body, attList);
					}
					catch(Exception e)
					{
						logger.severe("Error putting report :" + e);
					}
					return null;
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
