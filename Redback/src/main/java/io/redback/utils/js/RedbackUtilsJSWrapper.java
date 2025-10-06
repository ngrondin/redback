package io.redback.utils.js;

import java.awt.image.BufferedImage;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.firebus.data.DataEntity;
import io.firebus.data.DataList;
import io.firebus.data.DataMap;
import io.firebus.logging.Logger;
import io.redback.exceptions.RedbackException;
import io.redback.services.impl.RedbackUIServer;
import io.redback.utils.CronExpression;
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
		} else if(key.equals("hash")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					return StringUtils.hash((String)arguments[0]);
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
						throw new RuntimeException("Error sleeping", e);
					}
				}
			};	
		} else if(key.equals("decodeCSV")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					try {
						DataList lines = StringUtils.decodeCSV(arguments[0].toString());
						return lines;
					} catch(Exception e) {
						throw new RuntimeException("Error decoding CSV", e);
					}
				}
			};	
		} else if(key.equals("cronExpressionNext")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					try {
						String expression = arguments[0].toString();
						Date ref = arguments.length > 1 && arguments[1] instanceof Date ? (Date)arguments[1] : new Date();
						CronExpression ce = new CronExpression(expression);
						Date next = ce.getNextValidTimeAfter(ref);
						return next;
					} catch(Exception e) {
						throw new RuntimeException("Error in cron expression", e);
					}
				}	
			};
		} else if(key.equals("regexMatch")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					try {
						String expression = arguments[0].toString();
						String input = arguments[1].toString();
						Pattern pattern = Pattern.compile(expression);
						Matcher matcher = pattern.matcher(input);
						return matcher.matches();
					} catch(Exception e) {
						throw new RuntimeException("Error while running regex match", e);
					}
				}	
			};
		} else if(key.equals("regexGroups")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					try {
						String expression = arguments[0].toString();
						String input = arguments[1].toString();
						Pattern pattern = Pattern.compile(expression);
						Matcher matcher = pattern.matcher(input);
						DataList list = new DataList();
						if(matcher.find()) {
							int count = matcher.groupCount();
							for(int i = 1; i <= count; i++) {
								list.add(matcher.group(i));
							}
						}
						return list;
					} catch(Exception e) {
						throw new RuntimeException("Error while running regex group", e);
					}
				}	
			};	
		} else if(key.equals("createImage")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					long w = (long)arguments[0];
					long h = (long)arguments[1];
					BufferedImage image = new BufferedImage((int)w, (int)h, BufferedImage.TYPE_INT_ARGB);
					return new BufferedImageJSWrapper(image);
				}	
			};			
		} else {	
			return null;
		}
	}


}
