package io.redback.utils;

import java.awt.Color;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML.Tag;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.ParserDelegator;

import io.redback.exceptions.RedbackException;

public abstract class HTMLParser extends HTMLEditorKit.ParserCallback {
	protected String html;
	protected ParserDelegator parser;
	protected List<Map<String, Object>> context;	

	public HTMLParser(String h) {
		html = h;
		parser = new ParserDelegator();	
		context = new ArrayList<Map<String, Object>>();
	}
	
	public void parse() throws RedbackException {
		try {
			parser.parse(new StringReader(html), this, false);
			onComplete();
		} catch(Exception e) {
			throw new RedbackException("Error parsing HTML", e);
		}
	}
	
	public Object getContext(String name) {
		Object o = null;
		for(int i = context.size() - 1; i >= 0; i--) {
			Map<String, Object> level = context.get(i);
			o = level.get(name);
			if(o != null) break;
		}
		return o;
	}
	
	public void handleText(char[] data, int pos) {
		onText(new String(data));
	}
	
	public void handleStartTag(Tag t, MutableAttributeSet a, int pos) {
		Map<String, Object> ctx = new HashMap<String, Object>();
		String tagName = t.toString();
		if(tagName.equals("b") || tagName.equals("strong")) {
			ctx.put("bold", true);
		}
		Enumeration<?> e = a.getAttributeNames();
		while(e.hasMoreElements()) {
			Object attrName = e.nextElement();
			if(attrName.toString().equals("style")) {
				String styleVal = (String)a.getAttribute(attrName);
				String[] styleParts = styleVal.split(";");
				for(String part: styleParts) {
					String[] subparts = part.split(":");
					if(subparts[0].equals("color")) {
						String valStr = subparts[1].trim();
						Object val = valStr;
						if(valStr.startsWith("rgb(") && valStr.endsWith(")")) {
							String[] colorParts = valStr.substring(4, valStr.length() - 1).split(",");
							Color color = new Color(Integer.parseInt(colorParts[0].trim()), Integer.parseInt(colorParts[1].trim()), Integer.parseInt(colorParts[2].trim()));
							val = color;
						}
						ctx.put("color", val);
					}
				}
			}
		}
		context.add(ctx);
		onTag(t.toString());
	}
	
	public void handleEndTag(Tag t, int pos) {
		if(context.size() > 0)
			context.remove(context.size() - 1);
	}
	
	public void handleSimpleTag(Tag t, MutableAttributeSet a, int pos) {
		onTag(t.toString());
	}
	
	public void handleEmptyTag(Tag t, MutableAttributeSet a, int pos) {

	}
	
	public abstract void onTag(String tag);
	
	public abstract void onText(String text);
	
	public abstract void onComplete();
	
	
	public static void main(String[] args) {
		//String html = "<html><p>Allo</p><br><p style=\"color:red\">This <b>text</b> is red</p></html>";
		String html = "this is just some text\r\nand some more";
		HTMLParser parser = new HTMLParser(html) {
			public void onTag(String tag) {
				if(tag.equals("br")) System.out.print("\r\n");
			}

			public void onText(String text) {
				System.out.print("(" + getContext("color") + ", " + getContext("bold") + ")");
				System.out.print(text);
			}	
			
			public void onComplete() {
				
			}
		};
		try {
			parser.parse();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

}
