package io.redback.utils.js;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;


import io.firebus.data.DataEntity;
import io.firebus.data.DataMap;
import io.firebus.logging.Logger;
import io.redback.exceptions.RedbackException;
import io.redback.services.impl.RedbackUIServer;
import io.redback.utils.StringUtils;

public class RedbackUtilsJSWrapper extends ObjectJSWrapper
{
	public RedbackUtilsJSWrapper() {
		super(new String[] {"convertDataEntityToAttributeString", 
				"convertDataMapToAttributeString", 
				"convertFilterForClient", 
				"base64encode", 
				"base64decode", 
				"urlencode", 
				"urldecode",
				"encode",
				"getTimezoneOffset",
				"levenshtein",
				"base64MimeMessage"});
	}
	
	public Object get(String key) {
		if(key.equals("convertDataEntityToAttributeString")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					return StringUtils.convertDataEntityToAttributeString((DataEntity)arguments[0]);
				}
			};
		} else if(key.equals("convertDataMapToAttributeString")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					return StringUtils.convertDataEntityToAttributeString((DataMap)(arguments[0]));
				}
			};
		} else if(key.equals("convertFilterForClient")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					return RedbackUIServer.convertFilter((DataMap)(arguments[0]));
				}
			};
		} else if(key.equals("base64encode")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					return StringUtils.base64encode((String)arguments[0]);
				}
			};
		} else if(key.equals("base64decode")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					return StringUtils.base64decode((String)arguments[0]);
				}
			};
		} else if(key.equals("base64urlencode")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					return StringUtils.base64urlencode((String)arguments[0]);
				}
			};
		} else if(key.equals("base64urldecode")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					return StringUtils.base64urldecode((String)arguments[0]);
				}
			};
		} else if(key.equals("urlencode")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					return StringUtils.urlencode((String)arguments[0]);
				}
			};
		} else if(key.equals("urldecode")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					return StringUtils.urldecode((String)arguments[0]);
				}
			};
		} else if(key.equals("encode")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					return StringUtils.encode((String)arguments[0]);
				}
			};
		} else if(key.equals("getTimezoneOffset")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					String tzName = (String)arguments[0];
					if(tzName == null || (tzName != null && (tzName.equals("") || tzName.equals("null")))) {
						Logger.warning("rb.js.utils", "Problem in getTImezoneOffset, timezone argument was '" + tzName + "'");
						tzName = "UTC";
					}
					ZoneId here = ZoneId.of(tzName);
					ZonedDateTime hereAndNow = Instant.now().atZone(here);
					return -1 * hereAndNow.getOffset().getTotalSeconds() * 1000;
				}
			};
		} else if(key.equals("levenshtein")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					String s1 = (String)arguments[0];
					String s2 = (String)arguments[1];
					return StringUtils.levenshtein(s1, s2);
				}
			};
		} else if(key.equals("base64MimeMessage")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					try {
						String to = (String)arguments[0];
						String from = (String)arguments[1];
						String subject = (String)arguments[2];
						String body = (String)arguments[3];
						return StringUtils.base64MimeMessage(to, from, subject, body);
					} catch(Exception e) {
						throw new RuntimeException("Error creating Base64 Mime Message", e);
					}
				}
			};	
		} else if(key.equals("isHtml")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					try {
						String str = (String)arguments[0];
						return StringUtils.isHtml(str);
					} catch(Exception e) {
						throw new RuntimeException("Error determining if is html", e);
					}
				}
			};			
		} else if(key.equals("stripHtml")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					try {
						String str = (String)arguments[0];
						return StringUtils.stripHtml(str);
					} catch(Exception e) {
						throw new RuntimeException("Error stripping html", e);
					}
				}
			};		
		} else if(key.equals("parseMailDate")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					try {
						String str = (String)arguments[0];
						return StringUtils.parseMailDate(str);
					} catch(Exception e) {
						throw new RuntimeException("Error parsing Mail date", e);
					}
				}
			};	
		} else if(key.equals("sleep")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					try {
						long ms = (long)arguments[0];
						Thread.sleep(ms);
						return null;
					} catch(Exception e) {
						throw new RuntimeException("Error parsing Mail date", e);
					}
				}
			};				
		} else {	
			return null;
		}
	}


}
