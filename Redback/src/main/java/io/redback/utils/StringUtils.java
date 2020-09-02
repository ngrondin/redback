package io.redback.utils;

import java.util.Date;

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
	
	public static String base16(int n) 
	{
		String s = "";
		for(int i = 0; i < 8; i++)
		{
			s = s + (char)(((n >> (i * 4)) & 0x0f) + 97);
		}
		return s;
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
}
