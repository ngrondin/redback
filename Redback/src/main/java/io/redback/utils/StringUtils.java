package io.redback.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Date;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.bson.internal.Base64;
import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.SourceSection;

import io.firebus.utils.DataEntity;
import io.firebus.utils.DataList;
import io.firebus.utils.DataMap;

public class StringUtils
{
	public static String unescape(String st)
	{
		 StringBuilder sb = new StringBuilder(st.length());
		 
		 for (int i = 0; i < st.length(); i++) 
		 {
			 char ch = st.charAt(i);
			 if (ch == '\\') 
			 {
				 char nextChar = (i == st.length() - 1) ? '\\' : st.charAt(i + 1);
				 // Octal escape?
				 if (nextChar >= '0' && nextChar <= '7') 
				 {
					 String code = "" + nextChar;
					 i++;
					 if ((i < st.length() - 1) && st.charAt(i + 1) >= '0'	&& st.charAt(i + 1) <= '7') 
					 {
						 code += st.charAt(i + 1);
						 i++;
						 if ((i < st.length() - 1) && st.charAt(i + 1) >= '0'	&& st.charAt(i + 1) <= '7') 
						 {
							 code += st.charAt(i + 1);
							 i++;
						 }
					 }
					 sb.append((char) Integer.parseInt(code, 8));
					 continue;
				 }
				 switch (nextChar) 
				 {
				 case '\\':
					 ch = '\\';
					 break;
				 case 'b':
					 ch = '\b';
					 break;
				 case 'f':
					 ch = '\f';
					 break;
				 case 'n':
					 ch = '\n';
					 break;
				 case 'r':
					 ch = '\r';
					 break;
				 case 't':
					 ch = '\t';
					 break;
				 case '\"':
					 ch = '\"';
					 break;
				 case '\'':
					 ch = '\'';
					 break;
					 // Hex Unicode: u????
				 case 'u':
					 if (i >= st.length() - 5) 
					 {
						 ch = 'u';
						 break;
					 }
					 int code = Integer.parseInt("" + st.charAt(i + 2) + st.charAt(i + 3)+ st.charAt(i + 4) + st.charAt(i + 5), 16);
					 sb.append(Character.toChars(code));
					 i += 5;
					 continue;
				 }
		        i++;
		    }
		    sb.append(ch);
		}
		return sb.toString();
	}
	
	
	public static String convertDataEntityToAttributeString(DataEntity obj)
	{
		if(obj != null)
		{
			String ret = obj.toString();
			ret = ret.replaceAll("\r", "");
			ret = ret.replaceAll("\n", "");
			ret = ret.replaceAll("\t", "");
			ret = ret.replaceAll("\"", "&#34;");
			ret = ret.replaceAll("\'", "&#39;");
			return ret;
		}
		else
		{
			return "";
		}
	}
	
	public static String base64encode(String s) 
	{
		return Base64.encode(s.getBytes());
	}
	
	public static String base64decode(String s) 
	{
		return new String(Base64.decode(s));
	}


	public static String base16(int n) 
	{
		String s = "";
		for(int i = 0; i < 8; i++)
		{
			s = s + (char)(((n >> (i * 4)) & 0x0f) + 97);
		}
		return s;
	}
	
	public static String urlencode(String s) {
		try {
			return URLEncoder.encode(s, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			return s;
		}
	}
	
	public static String urldecode(String s) {
		try {
			return URLDecoder.decode(s, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			return s;
		}
	}
		
	
	public static String convertObjectToCSVField(Object obj) {
		String ret = "";
		if(obj instanceof String) {
			ret = (String)obj;
			ret = ret.replaceAll("\r", "");
			ret = ret.replaceAll("\n", "");
			ret = ret.replaceAll("\t", "");
			ret = ret.replaceAll("\"", "\\\"");
			ret = "\"" + ret + "\"";
		} else if(obj instanceof Date) {
			ret = "\"" + ((Date)obj).toInstant().toString() + "\""; 
		} else if(obj instanceof Number) {
			ret = ((Number)obj).toString();
		} else if(obj instanceof Boolean) {
			if(((Boolean)obj) == true) 
				ret = "true";
			else
				ret = "false";
		} else if(obj instanceof DataMap) {
			ret = ((DataMap)obj).toString(0, true);
			ret = "\"" + ret.replaceAll("\"", "\"\"") + "\"";
		} else if(obj instanceof DataList) {
			ret = ((DataList)obj).toString(0, true);
			ret = "\"" + ret.replaceAll("\"", "\"\"") + "\"";
		}
		return ret;
	}
	
	public static String rollUpExceptions(Throwable e) {
		String msg = "";
		Throwable t = e;
		while(t != null) {
			if(msg.length() > 0)
				msg = msg + ": ";
			msg = msg + t.getMessage();
			if(t instanceof PolyglotException) {
				PolyglotException pge = (PolyglotException)t;
				SourceSection ss = pge.getSourceLocation();
				if(ss != null) {
					msg = msg + " (" + ss.toString() + ")";
				}
				if(pge.isHostException())
					t = pge.asHostException();
			} 
			t = t.getCause();
		}
		return msg;
	}
	
	public static String getStackTrace(Exception e)
	{
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		String sStackTrace = sw.toString(); 
		return sStackTrace;
	}
	
	public static int levenshtein(String s1, String s2) 
	{
	    int[][] dp = new int[s1.length() + 1][s2.length() + 1];
	    for (int i = 0; i <= s1.length(); i++) {
	    	for (int j = 0; j <= s2.length(); j++) {
	    		if (i == 0) {
	    			dp[i][j] = j;
	    		}
	    		else if (j == 0) {
	    			dp[i][j] = i;
	    		} else {
	                dp[i][j] = min(
	                		dp[i - 1][j - 1] + (s1.charAt(i - 1) ==  s2.charAt(j - 1) ? 0 : 1), 
	                		dp[i - 1][j] + 1, 
	                		dp[i][j - 1] + 1
	                	);
	            }
	        }
	    }
	 
	    return dp[s1.length()][s2.length()];
	}
	
    public static int min(int... numbers) {
        return Arrays.stream(numbers).min().orElse(Integer.MAX_VALUE);
    }
    
    public static String base64MimeMessage(String to, String from, String subject, String body) throws AddressException, MessagingException, IOException {
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);
        MimeMessage email = new MimeMessage(session);
        email.setFrom(new InternetAddress(from));
        email.addRecipient(javax.mail.Message.RecipientType.TO, new InternetAddress(to));
        email.setSubject(subject);
        email.setContent(body, "text/html");
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        email.writeTo(buffer);
        byte[] bytes = buffer.toByteArray();
        return Base64.encode(bytes);
    }
	
}
