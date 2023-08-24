package io.redback.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import io.firebus.data.DataMap;
import io.firebus.logging.Logger;

public class HTMLMetaParser {
	
	public static DataMap parseUrl(String url) {
		String _url = url;
		if(!_url.startsWith("http"))
			_url = "https://" + _url;
		int hoststart = _url.indexOf("//") + 2;
		int hostend = _url.indexOf("/", hoststart);
		if(hostend == -1) hostend = _url.length();
		String host = _url.substring(hoststart, hostend);
    	HttpURLConnection con = null;
    	try {
    		URL u = new URL(_url);
	    	con = (HttpURLConnection) u.openConnection();
	    	con.setRequestMethod("GET");
	    	con.setRequestProperty("Accept", "text/html");
	    	con.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:109.0) Gecko/20100101 Firefox/116.0");
	    	con.setRequestProperty("Host", host);
	    	int status = con.getResponseCode();
	    	BufferedReader in = null;
	    	if(status >= 100 && status < 400) {
	    		in = new BufferedReader(new InputStreamReader(con.getInputStream()));
	    	} else {
	    		in = new BufferedReader(new InputStreamReader(con.getErrorStream()));
	    	}
			StringBuffer content = new StringBuffer();
	    	if(in != null) {
				String inputLine;
				while ((inputLine = in.readLine()) != null) content.append(inputLine);	    		
				in.close();
	    	}
			//System.out.println(content.toString());
			DataMap meta = parseHtml(content.toString());
			return meta;
    	} catch(Exception e) {
    		Logger.warning("rb.ui.geturlpreview", new DataMap("url", url), e);
    		return new DataMap();
    	} finally {
    		if(con != null) {
    			try {con.disconnect();} catch(Exception e) {}
    		}
    	}
	}

	public static DataMap parseHtml(String html) {
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
		if(ret != null)
			ret = StringUtils.unescapeHtml(ret);
		return ret;
	}
}
