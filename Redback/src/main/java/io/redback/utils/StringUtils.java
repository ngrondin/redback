package io.redback.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MailDateFormat;
import javax.mail.internet.MimeMessage;

import io.firebus.data.DataEntity;
import io.firebus.data.DataList;
import io.firebus.data.DataMap;

public class StringUtils
{
	protected static Pattern htmlPattern = Pattern.compile("<([a-z]|\\/[a-z])(.|\\n)*?>", Pattern.CASE_INSENSITIVE);

	private static final int MIN_ESCAPE = 2;
    private static final int MAX_ESCAPE = 6;

    private static final String[][] HTMLESCAPES = {
            {"\"",     "quot"}, // " - double-quote
            {"&",      "amp"}, // & - ampersand
            {"<",      "lt"}, // < - less-than
            {">",      "gt"}, // > - greater-than

            // Mapping to escape ISO-8859-1 characters to their named HTML 3.x equivalents.
            {"\u00A0", "nbsp"},   // Non-breaking space
            {"\u00A1", "iexcl"},  // Inverted exclamation mark
            {"\u00A2", "cent"},   // Cent sign
            {"\u00A3", "pound"},  // Pound sign
            {"\u00A4", "curren"}, // Currency sign
            {"\u00A5", "yen"},    // Yen sign = yuan sign
            {"\u00A6", "brvbar"}, // Broken bar = broken vertical bar
            {"\u00A7", "sect"},   // Section sign
            {"\u00A8", "uml"},    // Diaeresis = spacing diaeresis
            {"\u00A9", "copy"},   // © - copyright sign
            {"\u00AA", "ordf"},   // Feminine ordinal indicator
            {"\u00AB", "laquo"},  // Left-pointing double angle quotation mark = left pointing guillemet
            {"\u00AC", "not"},    // Not sign
            {"\u00AD", "shy"},    // Soft hyphen = discretionary hyphen
            {"\u00AE", "reg"},    // ® - registered trademark sign
            {"\u00AF", "macr"},   // Macron = spacing macron = overline = APL overbar
            {"\u00B0", "deg"},    // Degree sign
            {"\u00B1", "plusmn"}, // Plus-minus sign = plus-or-minus sign
            {"\u00B2", "sup2"},   // Superscript two = superscript digit two = squared
            {"\u00B3", "sup3"},   // Superscript three = superscript digit three = cubed
            {"\u00B4", "acute"},  // Acute accent = spacing acute
            {"\u00B5", "micro"},  // Micro sign
            {"\u00B6", "para"},   // Pilcrow sign = paragraph sign
            {"\u00B7", "middot"}, // Middle dot = Georgian comma = Greek middle dot
            {"\u00B8", "cedil"},  // Cedilla = spacing cedilla
            {"\u00B9", "sup1"},   // Superscript one = superscript digit one
            {"\u00BA", "ordm"},   // Masculine ordinal indicator
            {"\u00BB", "raquo"},  // Right-pointing double angle quotation mark = right pointing guillemet
            {"\u00BC", "frac14"}, // Vulgar fraction one quarter = fraction one quarter
            {"\u00BD", "frac12"}, // Vulgar fraction one half = fraction one half
            {"\u00BE", "frac34"}, // Vulgar fraction three quarters = fraction three quarters
            {"\u00BF", "iquest"}, // Inverted question mark = turned question mark
            {"\u00C0", "Agrave"}, // А - uppercase A, grave accent
            {"\u00C1", "Aacute"}, // Б - uppercase A, acute accent
            {"\u00C2", "Acirc"},  // В - uppercase A, circumflex accent
            {"\u00C3", "Atilde"}, // Г - uppercase A, tilde
            {"\u00C4", "Auml"},   // Д - uppercase A, umlaut
            {"\u00C5", "Aring"},  // Е - uppercase A, ring
            {"\u00C6", "AElig"},  // Ж - uppercase AE
            {"\u00C7", "Ccedil"}, // З - uppercase C, cedilla
            {"\u00C8", "Egrave"}, // И - uppercase E, grave accent
            {"\u00C9", "Eacute"}, // Й - uppercase E, acute accent
            {"\u00CA", "Ecirc"},  // К - uppercase E, circumflex accent
            {"\u00CB", "Euml"},   // Л - uppercase E, umlaut
            {"\u00CC", "Igrave"}, // М - uppercase I, grave accent
            {"\u00CD", "Iacute"}, // Н - uppercase I, acute accent
            {"\u00CE", "Icirc"},  // О - uppercase I, circumflex accent
            {"\u00CF", "Iuml"},   // П - uppercase I, umlaut
            {"\u00D0", "ETH"},    // Р - uppercase Eth, Icelandic
            {"\u00D1", "Ntilde"}, // С - uppercase N, tilde
            {"\u00D2", "Ograve"}, // Т - uppercase O, grave accent
            {"\u00D3", "Oacute"}, // У - uppercase O, acute accent
            {"\u00D4", "Ocirc"},  // Ф - uppercase O, circumflex accent
            {"\u00D5", "Otilde"}, // Х - uppercase O, tilde
            {"\u00D6", "Ouml"},   // Ц - uppercase O, umlaut
            {"\u00D7", "times"},  // Multiplication sign
            {"\u00D8", "Oslash"}, // Ш - uppercase O, slash
            {"\u00D9", "Ugrave"}, // Щ - uppercase U, grave accent
            {"\u00DA", "Uacute"}, // Ъ - uppercase U, acute accent
            {"\u00DB", "Ucirc"},  // Ы - uppercase U, circumflex accent
            {"\u00DC", "Uuml"},   // Ь - uppercase U, umlaut
            {"\u00DD", "Yacute"}, // Э - uppercase Y, acute accent
            {"\u00DE", "THORN"},  // Ю - uppercase THORN, Icelandic
            {"\u00DF", "szlig"},  // Я - lowercase sharps, German
            {"\u00E0", "agrave"}, // а - lowercase a, grave accent
            {"\u00E1", "aacute"}, // б - lowercase a, acute accent
            {"\u00E2", "acirc"},  // в - lowercase a, circumflex accent
            {"\u00E3", "atilde"}, // г - lowercase a, tilde
            {"\u00E4", "auml"},   // д - lowercase a, umlaut
            {"\u00E5", "aring"},  // е - lowercase a, ring
            {"\u00E6", "aelig"},  // ж - lowercase ae
            {"\u00E7", "ccedil"}, // з - lowercase c, cedilla
            {"\u00E8", "egrave"}, // и - lowercase e, grave accent
            {"\u00E9", "eacute"}, // й - lowercase e, acute accent
            {"\u00EA", "ecirc"},  // к - lowercase e, circumflex accent
            {"\u00EB", "euml"},   // л - lowercase e, umlaut
            {"\u00EC", "igrave"}, // м - lowercase i, grave accent
            {"\u00ED", "iacute"}, // н - lowercase i, acute accent
            {"\u00EE", "icirc"},  // о - lowercase i, circumflex accent
            {"\u00EF", "iuml"},   // п - lowercase i, umlaut
            {"\u00F0", "eth"},    // р - lowercase eth, Icelandic
            {"\u00F1", "ntilde"}, // с - lowercase n, tilde
            {"\u00F2", "ograve"}, // т - lowercase o, grave accent
            {"\u00F3", "oacute"}, // у - lowercase o, acute accent
            {"\u00F4", "ocirc"},  // ф - lowercase o, circumflex accent
            {"\u00F5", "otilde"}, // х - lowercase o, tilde
            {"\u00F6", "ouml"},   // ц - lowercase o, umlaut
            {"\u00F7", "divide"}, // Division sign
            {"\u00F8", "oslash"}, // ш - lowercase o, slash
            {"\u00F9", "ugrave"}, // щ - lowercase u, grave accent
            {"\u00FA", "uacute"}, // ъ - lowercase u, acute accent
            {"\u00FB", "ucirc"},  // ы - lowercase u, circumflex accent
            {"\u00FC", "uuml"},   // ь - lowercase u, umlaut
            {"\u00FD", "yacute"}, // э - lowercase y, acute accent
            {"\u00FE", "thorn"},  // ю - lowercase thorn, Icelandic
            {"\u00FF", "yuml"},   // я - lowercase y, umlaut
        };

    
    private static final HashMap<String, CharSequence> htmlEscapeLookupMap;
    static {
        htmlEscapeLookupMap = new HashMap<String, CharSequence>();
        for (final CharSequence[] seq : HTMLESCAPES)
            htmlEscapeLookupMap.put(seq[1].toString(), seq[0]);
    }
    
	
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
	
	public static final String unescapeHtml(final String input) {
        StringWriter writer = null;
        int len = input.length();
        int i = 1;
        int st = 0;
        while (true) {
            // Look for '&'
            while (i < len && input.charAt(i-1) != '&')
                i++;
            if (i >= len)
                break;

            // Found '&', look for ';'
            int j = i;
            while (j < len && j < i + MAX_ESCAPE + 1 && input.charAt(j) != ';')
                j++;
            if (j == len || j < i + MIN_ESCAPE || j == i + MAX_ESCAPE + 1) {
                i++;
                continue;
            }

            // Found escape
            if (input.charAt(i) == '#') {
                // Numeric escape
                int k = i + 1;
                int radix = 10;

                final char firstChar = input.charAt(k);
                if (firstChar == 'x' || firstChar == 'X') {
                    k++;
                    radix = 16;
                }

                try {
                    int entityValue = Integer.parseInt(input.substring(k, j), radix);

                    if (writer == null)
                        writer = new StringWriter(input.length());
                    writer.append(input.substring(st, i - 1));

                    if (entityValue > 0xFFFF) {
                        final char[] chrs = Character.toChars(entityValue);
                        writer.write(chrs[0]);
                        writer.write(chrs[1]);
                    } else {
                        writer.write(entityValue);
                    }

                } catch (NumberFormatException ex) {
                    i++;
                    continue;
                }
            }
            else {
                // Named escape
                CharSequence value = htmlEscapeLookupMap.get(input.substring(i, j));
                if (value == null) {
                    i++;
                    continue;
                }

                if (writer == null)
                    writer = new StringWriter(input.length());
                writer.append(input.substring(st, i - 1));

                writer.append(value);
            }

            // Skip escape
            st = j + 1;
            i = st;
        }

        if (writer != null) {
            writer.append(input.substring(st, len));
            return writer.toString();
        }
        return input;
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
		return new String(Base64.getEncoder().encode(s.getBytes()));
	}
	
	public static String base64decode(String s) 
	{
		return new String(Base64.getDecoder().decode(s));
	}

	public static String base64urlencode(String s) 
	{
		return new String(Base64.getUrlEncoder().encode(s.getBytes()));
	}
	
	public static String base64urldecode(String s) 
	{
		return new String(Base64.getUrlDecoder().decode(s));
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
		} catch (Exception e) {
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
	
	public static String encode(String s) {
		String r = null;
		if(s != null) {
			r = s.replaceAll(" ", "%20").replaceAll("\"", "%22").replaceAll("'", "%27");
		}
		return r;
	}
	
	public static String escapeRegex(String s) {
		String chars = "\\<([{^-=$!|]})?*+.>";
		String ret = s;
		for(int i = 0; i < chars.length(); i++) {
			String c = chars.substring(i, i + 1);
			ret = ret.replace(c, "\\" + c);
		}
		return ret;		
	}
	
	public static String hash(String s) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			md.update(s.getBytes());
			byte[] hashBytes = md.digest();
			Formatter formatter = new Formatter();
		    for (byte b : hashBytes) 
		        formatter.format("%02x", b);
		    String hashStr = formatter.toString();
		    formatter.close();
			return hashStr;
		} catch(Exception e) {
			return "";
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
			ret = ((DataMap)obj).toString(true);
			ret = "\"" + ret.replaceAll("\"", "\"\"") + "\"";
		} else if(obj instanceof DataList) {
			ret = ((DataList)obj).toString(true);
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
        return new String(Base64.getEncoder().encode(bytes));
    }
    
    public static boolean isHtml(String str) {
    	Matcher matcher = htmlPattern.matcher(str);
        return  matcher.find();
    }
	
    public static String stripHtml(String str) {
    	Matcher matcher = htmlPattern.matcher(str);
    	String ret = matcher.replaceAll("");
    	return ret;
    }
    
    public static Date parseMailDate(String str) throws ParseException {
    	MailDateFormat mdf = new MailDateFormat();
    	Date dt = mdf.parse(str);
    	return dt;
    }
    
    public static DataList decodeCSV(String str)  {
    	DataList ret = new DataList();
    	String strm = str.replaceAll("\r", "");
    	int endFirstLine = strm.indexOf("\n");
    	String headerLine = strm.substring(0, endFirstLine).trim();
		String[] headers = headerLine.split(",");
		String body = strm.substring(endFirstLine).trim();
		
		StringBuilder buffer = new StringBuilder();
		DataMap map = new DataMap();
		int col = 0;
		boolean inQuote = false;
		boolean escapeNext = false;
		for(int ptr = 0; ptr <= body.length(); ptr++) {
			char c = ptr < body.length() ? body.charAt(ptr) : 0;
			if(inQuote) {
				if(c == '\\' && escapeNext == false) {
					escapeNext = true;
				} else if(escapeNext) {
					if(c == 'n') buffer.append("\n");
					if(c == '"') buffer.append("\"");
					escapeNext = false;
				} else if(c == '"') {
					inQuote = !inQuote;
				} else {
					buffer.append(c);
				}				
			} else {
				boolean lastCharOfBody = ptr == body.length();
				boolean lastCharOfLine = lastCharOfBody || c == '\n';
				boolean lastCharOfField = lastCharOfLine || c == ',';
				if(lastCharOfField) {
					String val = buffer.length() > 0 ? buffer.toString() : null;
					if(col < headers.length) map.put(headers[col], val);    						
					buffer = new StringBuilder();
					col++;
					if(lastCharOfLine) {
						ret.add(map);
						map = new DataMap();
						col = 0;						
					}
				} else if(c == '"') {
					inQuote = true;
				} else {
					buffer.append(c);
				}
			}
		}
    	return ret;
    }
}
