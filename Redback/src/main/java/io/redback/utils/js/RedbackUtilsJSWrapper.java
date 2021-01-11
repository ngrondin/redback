package io.redback.utils.js;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;

import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyArray;
import org.graalvm.polyglot.proxy.ProxyExecutable;
import org.graalvm.polyglot.proxy.ProxyObject;

import io.firebus.utils.DataEntity;
import io.firebus.utils.DataMap;
import io.redback.services.impl.RedbackUIServer;
import io.redback.utils.StringUtils;

public class RedbackUtilsJSWrapper implements ProxyObject
{
	protected String[] members = {
			"convertDataEntityToAttributeString", 
			"convertDataMapToAttributeString", 
			"convertFilterForClient", 
			"base64encode", 
			"base64decode", 
			"urlencode", 
			"urldecode",
			"getTimezoneOffset",
			"levenshtein",
			"base64MimeMessage"
		};
	
	public RedbackUtilsJSWrapper() {
	}
	
	public Object getMember(String key) {
		if(key.equals("convertDataEntityToAttributeString")) {
			return new ProxyExecutable() {
				public Object execute(Value... arguments) {
					return StringUtils.convertDataEntityToAttributeString((DataEntity)JSConverter.toJava(arguments[0]));
				}
			};
		} else if(key.equals("convertDataMapToAttributeString")) {
			return new ProxyExecutable() {
				public Object execute(Value... arguments) {
					return StringUtils.convertDataEntityToAttributeString((DataMap)JSConverter.toJava(arguments[0]));
				}
			};
		} else if(key.equals("convertFilterForClient")) {
			return new ProxyExecutable() {
				public Object execute(Value... arguments) {
					return RedbackUIServer.convertFilter((DataMap)JSConverter.toJava(arguments[0]));
				}
			};
		} else if(key.equals("base64encode")) {
			return new ProxyExecutable() {
				public Object execute(Value... arguments) {
					return JSConverter.toJS(StringUtils.base64encode(arguments[0].asString()));
				}
			};
		} else if(key.equals("base64decode")) {
			return new ProxyExecutable() {
				public Object execute(Value... arguments) {
					return JSConverter.toJS(StringUtils.base64decode(arguments[0].asString()));
				}
			};
		} else if(key.equals("urlencode")) {
			return new ProxyExecutable() {
				public Object execute(Value... arguments) {
					return JSConverter.toJS(StringUtils.urlencode(arguments[0].asString()));
				}
			};
		} else if(key.equals("urldecode")) {
			return new ProxyExecutable() {
				public Object execute(Value... arguments) {
					return JSConverter.toJS(StringUtils.urldecode(arguments[0].asString()));
				}
			};
		} else if(key.equals("getTimezoneOffset")) {
			return new ProxyExecutable() {
				public Object execute(Value... arguments) {
					String tzName = arguments[0].asString();
					ZoneId here = ZoneId.of(tzName);
					ZonedDateTime hereAndNow = Instant.now().atZone(here);
					return -1 * hereAndNow.getOffset().getTotalSeconds() * 1000;
				}
			};
		} else if(key.equals("levenshtein")) {
			return new ProxyExecutable() {
				public Object execute(Value... arguments) {
					String s1 = arguments[0].asString();
					String s2 = arguments[1].asString();
					return StringUtils.levenshtein(s1, s2);
				}
			};
		} else if(key.equals("base64MimeMessage")) {
			return new ProxyExecutable() {
				public Object execute(Value... arguments) {
					try {
						String to = arguments[0].asString();
						String from = arguments[1].asString();
						String subject = arguments[2].asString();
						String body = arguments[3].asString();
						return StringUtils.base64MimeMessage(to, from, subject, body);
					} catch(Exception e) {
						throw new RuntimeException("Error creating Base64 Mime Message", e);
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
