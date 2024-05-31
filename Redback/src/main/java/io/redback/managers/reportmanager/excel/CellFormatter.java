package io.redback.managers.reportmanager.excel;

import io.firebus.data.DataMap;
import jxl.biff.DisplayFormat;
import jxl.format.Alignment;
import jxl.format.Colour;
import jxl.format.VerticalAlignment;
import jxl.write.DateFormat;
import jxl.write.NumberFormat;
import jxl.write.NumberFormats;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;

public class CellFormatter extends WritableCellFormat {

	public static WritableCellFormat createFormat(Object cfg) {
		if(cfg instanceof String) 
			return createFormat((String)cfg);
		else if(cfg instanceof DataMap) 
			return createFormat((DataMap)cfg);
		else
			return new WritableCellFormat();
	}
	
	public static WritableCellFormat createFormat(String colorName) {
		WritableCellFormat wcf = new WritableCellFormat();
		try {
			Colour c = getColor(colorName);
			if(c != null)
				wcf.setBackground(c);
		} catch(Exception e) {}
		return wcf;
	}
	
	public static WritableCellFormat createFormat(DataMap config) {
		WritableCellFormat wcf = null;
		try {
			DisplayFormat format = null;
			if(config.containsKey("format")) {
				String formatStr = config.getString("format");
				if(formatStr.equals("currency"))
					format = new NumberFormat(NumberFormat.CURRENCY_DOLLAR + "###,###.00", NumberFormat.COMPLEX_FORMAT); 
				else if(formatStr.equals("percent"))
					format = NumberFormats.PERCENT_FLOAT;
				else if(formatStr.equals("datetime"))
					format = new DateFormat(config.containsKey("datetimeformat") ? config.getString("datetimeformat") : "dd/MM/yyyy hh:mm");
				else if(formatStr.equals("date"))
					format = new DateFormat(config.containsKey("dateformat") ? config.getString("dateformat") : "dd/MM/yyyy");
				else if(formatStr.equals("time"))
					format = new DateFormat(config.containsKey("timeformat") ? config.getString("timeformat") : "hh:mm");
			}
			if(format != null) wcf = new WritableCellFormat(format);
			else wcf = new WritableCellFormat();
			WritableFont wf = new WritableFont(WritableFont.TAHOMA);
			if(config.containsKey("fontsize")) {
				int fs = config.getNumber("fontsize").intValue();
				wf.setPointSize(fs);
			}
			if(config.getBoolean("bold"))
				wf.setBoldStyle(WritableFont.BOLD);
			if(config.getBoolean("italic"))
				wf.setItalic(true);
			if(config.containsKey("color")) {
				Colour c = getColor(config.getString("color"));
				if(c != null) wf.setColour(c);
			}			
			wcf.setFont(wf);
			if(config.containsKey("background")) {
				Colour c = getColor(config.getString("background"));
				if(c != null) wcf.setBackground(c);
			}
			if(config.getBoolean("wrap")) {
				wcf.setWrap(true);
			}
			if(config.containsKey("align")) {
				switch(config.getString("align")) {
				case "left": wcf.setAlignment(Alignment.LEFT); break;
				case "center": wcf.setAlignment(Alignment.CENTRE); break;
				case "right": wcf.setAlignment(Alignment.RIGHT); break;
				}
			}
			if(config.containsKey("valign")) {
				switch(config.getString("valign")) {
				case "top": wcf.setVerticalAlignment(VerticalAlignment.TOP); break;
				case "center": wcf.setVerticalAlignment(VerticalAlignment.CENTRE); break;
				case "bottom": wcf.setVerticalAlignment(VerticalAlignment.BOTTOM); break;
				}
			}			
		} catch(Exception e) {
			wcf = new WritableCellFormat();
		}
		return wcf;
	}
	
	public static Colour getColor(String name) {
		Colour[] colours = Colour.getAllColours();
		for(Colour c : colours) {
			if(c.getDescription().equalsIgnoreCase(name))
				return c;
		}
		return null;
	}
}
