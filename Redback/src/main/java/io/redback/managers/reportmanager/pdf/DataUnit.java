package io.redback.managers.reportmanager.pdf;

import java.io.IOException;
import java.text.NumberFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import io.firebus.data.DataMap;
import io.firebus.script.Expression;
import io.redback.exceptions.RedbackException;
import io.redback.managers.reportmanager.ReportConfig;
import io.redback.managers.reportmanager.ReportManager;
import io.redback.security.Session;

public abstract class DataUnit extends Unit {
	protected Expression valueExpr;
	protected PDFont font;
	protected PDFont boldFont;
	protected Expression fontSizeExpr;
	protected String format;
	protected boolean commaToLine;

	public DataUnit(ReportManager rm, ReportConfig rc, DataMap c) throws RedbackException {
		super(rm, rc, c);
		try {
			jsParams = Arrays.asList(new String[] {"dataset", "object", "master", "page"});
			valueExpr = reportManager.getScriptFactory().createExpression(jsFunctionNameRoot + "_text_value", c.getString("value"));
			font = PDType1Font.HELVETICA;
			boldFont = PDType1Font.HELVETICA_BOLD;
			fontSizeExpr = config.containsKey("fontsize") ? reportManager.getScriptFactory().createExpression(jsFunctionNameRoot + "_dataunit_fontsize", config.getString("fontsize")) : null;
			//color = config.containsKey("color") ? decodeColor(config.getString("color")) : Color.DARK_GRAY;
			format = config.getString("format");
			commaToLine = config.containsKey("commatoline") ? config.getBoolean("commatoline") : false;
		} catch(Exception e) {
			throw new RedbackException("Error intialising container unit", e);
		}
	}


	public abstract Box produce(Map<String, Object> context) throws IOException, RedbackException;
	
	protected float getStringWidth(float fontSize, String text) throws IOException {
		return Utils.textWidth(font, fontSize, text);
	}
	
	protected float getFontHeight(float fontSize) {
		return  2f * (font.getFontDescriptor().getCapHeight()) / 1000 * fontSize;
	}
	
	protected List<String> cutToLines(float fontSize, String text, float width) throws IOException {
		List<String> lines = new ArrayList<String>();
		if(text.length() > 0) {
		    int lastSpace = -1;
		    while (text.length() > 0)
		    {
		        int spaceIndex = text.indexOf(' ', lastSpace + 1);
		        if (spaceIndex < 0)
		            spaceIndex = text.length();
		        String subString = text.substring(0, spaceIndex);
		        float size = getStringWidth(fontSize, subString);
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
		} else {
			lines.add("");
		}
	    return lines;
	}
	
	protected String cutToWidth(String text, float fontSize, float width) throws IOException {
		if(width > 0) {
			int pos = 0;
			while(true) {
				int nextPos = text.indexOf(" ", pos + 1);
				if(nextPos == -1) nextPos = text.length();
				float size = getStringWidth(fontSize, text.substring(0, nextPos));
				if(size > width) {
					return text.substring(0, pos);
				} else if(nextPos == text.length()) {
					return text;
				} else {
					pos = nextPos;
				}
			}			
		} else {
			return text;
		}
	}
	
	protected String getSringValue(Map<String, Object> context) throws RedbackException {
		Session session = (Session)context.get("session");
		Object value = getValue(context);
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
					valueStr = zdt.format(DateTimeFormatter.ofPattern("d MMM yy HH:mm"));
				} catch(Exception e) {
					valueStr = "Bad data for datetime";
				}
			} 
		}
		return valueStr;
	}
	
	protected Object getValue(Map<String, Object> context) throws RedbackException {
		Map<String, Object> jsContext = getJSContext(context);
		jsContext.put("dataset", DataSet.convertToScript(context.get("dataset")));
		jsContext.put("master", DataSet.convertToScript(context.get("master")));
		Object value = null;
		try {
			value = valueExpr.eval(jsContext);
		} catch(Exception e) {}
		return value;
	}
	
	protected float fontSize(Map<String, Object> context) throws RedbackException {
		if(fontSizeExpr != null) {
			try {
				return ((Number)fontSizeExpr.eval(getJSContext(context))).floatValue();
			} catch(Exception e) {
				return 12f;
			}
		} else {
			return 12f;
		}
	}
	
	protected String cleanString(String str) {
		return str;
	}
}
