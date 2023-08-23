package io.redback.utils;

import io.firebus.data.DataMap;

public class HTMLMetaParser {

	public static DataMap parse(String html) {
		String title = null;
		String description = null;
		String image = null;
		int start = -1;
		while((start = html.indexOf("<meta ", start + 1)) > -1) {
			int end = html.indexOf(">", start);
			String sub = html.substring(start, end + 1);
			String name = parseAttribute(sub, "name");
			if(name == null) name = parseAttribute(sub, "property");
			if(name != null) {
				String content = parseAttribute(sub, "content");
				if(content != null) {
					if(name.equals("og:site_name")) title = content;
					if(name.equals("og:title")) title = content;
					if(name.equals("twitter:title")) title = content;
					if(name.equals("og:description")) description = content;
					if(name.equals("twitter:description")) description = content;
					if(name.equals("og:image")) image = content;
					if(name.equals("twitter:image")) image = content;
				}
			}
		}
		DataMap resp = new DataMap();
		if(title != null) resp.put("title", title);
		if(description != null) resp.put("description", description);
		if(image != null) resp.put("image", image);
		return resp;
	}
	
	public static String parseAttribute(String t, String a) {
		String ret = null;
		String startStr = " " + a + "=\"";
		int start = t.indexOf(startStr);
		if(start > -1) {
			int valStart = start + startStr.length();
			int end = t.indexOf("\"", valStart);
			ret = t.substring(valStart, end);
		}
		return ret;
	}
}
