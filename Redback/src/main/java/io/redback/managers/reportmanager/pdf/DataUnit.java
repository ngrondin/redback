package io.redback.managers.reportmanager.pdf;

import java.awt.Color;
import java.io.IOException;
import java.text.NumberFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import io.firebus.data.DataMap;
import io.redback.exceptions.RedbackException;
import io.redback.managers.jsmanager.Expression;
import io.redback.managers.reportmanager.ReportConfig;
import io.redback.managers.reportmanager.ReportManager;
import io.redback.security.Session;
import io.redback.utils.js.JSConverter;

public abstract class DataUnit extends Unit {
	protected Expression valueExpr;
	protected PDFont font;
	protected PDFont boldFont;
	protected float fontSize;
	protected Color color;
	protected float height;
	protected float width;
	protected String format;
	protected boolean commaToLine;

	public DataUnit(ReportManager rm, ReportConfig rc, DataMap c) throws RedbackException {
		super(rm, rc, c);
		jsParams = Arrays.asList(new String[] {"dataset", "object", "master", "page"});
		valueExpr = new Expression(reportManager.getJSManager(), jsFunctionNameRoot + "_text_value", jsParams, c.getString("value"));
		width = config.containsKey("width") ? config.getNumber("width").floatValue() : -1;
		font = PDType1Font.HELVETICA;
		boldFont = PDType1Font.HELVETICA_BOLD;
		fontSize = config.containsKey("fontsize") ? config.getNumber("fontsize").floatValue() : 12f;
		height = config.containsKey("height") ? config.getNumber("height").floatValue() : 2f * (font.getFontDescriptor().getCapHeight()) / 1000 * fontSize;
		color = config.containsKey("color") ? getColor(config.getString("color")) : Color.DARK_GRAY;
		format = config.getString("format");
		commaToLine = config.containsKey("commatoline") ? config.getBoolean("commatoline") : false;
	}


	public abstract Box produce(Map<String, Object> context) throws IOException, RedbackException;
	
	protected float getStringWidth(String text) throws IOException {
		return font.getStringWidth(text) / 1000f * fontSize;
	}
	
	protected List<String> cutToLines(String text, float width) throws IOException {
		List<String> lines = new ArrayList<String>();
	    int lastSpace = -1;
	    while (text.length() > 0)
	    {
	        int spaceIndex = text.indexOf(' ', lastSpace + 1);
	        if (spaceIndex < 0)
	            spaceIndex = text.length();
	        String subString = text.substring(0, spaceIndex);
	        float size = getStringWidth(subString);
	        if (size > width)
	        {
	            if (lastSpace < 0)
	                lastSpace = spaceIndex;
	            subString = text.substring(0, lastSpace);
	            lines.add(subString);
	            text = text.substring(lastSpace).trim();
	            lastSpace = -1;
	        }
	        else if (spaceIndex == text.length())
	        {
	            lines.add(text);
	            text = "";
	        }
	        else
	        {
	            lastSpace = spaceIndex;
	        }
	    }
	    return lines;
	}
	
	protected String getSringValue(Map<String, Object> context) throws RedbackException {
		Session session = (Session)context.get("session");
		Map<String, Object> jsContext = new HashMap<String, Object>();
		jsContext.put("dataset", JSConverter.toJS(context.get("dataset")));
		jsContext.put("object", JSConverter.toJS(context.get("object")));
		jsContext.put("master", JSConverter.toJS(context.get("master")));
		jsContext.put("page", context.get("page"));
		Object value = null;
		try {
			value = valueExpr.eval(jsContext);
		} catch(Exception e) {}
		String valueStr = value != null ? value.toString() : "";
		if(commaToLine) 
			valueStr = valueStr.replaceAll(", ", "\r\n").replaceAll(",", "\r\n");
		if(value != null && format != null) {
			if(format.equals("currency")) {
				try {
					NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.US);
					valueStr = formatter.format(Float.parseFloat(valueStr));
				} catch(Exception e) {
					valueStr = "Bad data for currency";
				}
			} else if(format.equals("duration")) {
				try {
					Long dur = Long.parseLong(valueStr);
					valueStr = "";
					if(dur >= 3600000) {
						valueStr += Math.abs(dur / 3600000) + "h";
					}
					if(dur > 60000 && (dur % 3600000) != 0) {
						long mins = Math.abs((dur % 3600000) / 60000);
						if(dur > 3600000 && mins < 10)
							valueStr += "0";
						valueStr += mins + "m";
					}
					if(dur > 0 && dur < 60000) {
						valueStr += Math.abs((dur % 60000) / 1000) + "s";
					}
				} catch(Exception e) {
					valueStr = "Bad data for duration";
				}
			} else if(format.equals("shortdate") && value != null && value instanceof Date) {
				try {
					ZonedDateTime zdt = ZonedDateTime.ofInstant(((Date)value).toInstant(), session.getTimezone() != null ? ZoneId.of(session.getTimezone()) : ZoneId.systemDefault());
					valueStr = zdt.format(DateTimeFormatter.ofPattern("d MMM"));
				} catch(Exception e) {
					valueStr = "Bad data for date";
				}
			} else if(format.equals("date") && value != null && value instanceof Date) {
				try {
					ZonedDateTime zdt = ZonedDateTime.ofInstant(((Date)value).toInstant(), session.getTimezone() != null ? ZoneId.of(session.getTimezone()) : ZoneId.systemDefault());
					valueStr = zdt.format(DateTimeFormatter.ofPattern("d MMM yy"));
				} catch(Exception e) {
					valueStr = "Bad data for date";
				}
			} else if(format.equals("datetime") && value != null && value instanceof Date) {
				try {
					ZonedDateTime zdt = ZonedDateTime.ofInstant(((Date)value).toInstant(), session.getTimezone() != null ? ZoneId.of(session.getTimezone()) : ZoneId.systemDefault());
					valueStr = zdt.format(DateTimeFormatter.ofPattern("d MMM yy HH:mm a"));
				} catch(Exception e) {
					valueStr = "Bad data for datetime";
				}
			} 
		}
		return valueStr;
	}
	


}
