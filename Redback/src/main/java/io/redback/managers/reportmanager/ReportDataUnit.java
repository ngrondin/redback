package io.redback.managers.reportmanager;

import java.awt.Color;
import java.io.IOException;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import io.firebus.utils.DataMap;
import io.redback.RedbackException;
import io.redback.client.RedbackObjectRemote;
import io.redback.client.js.RedbackObjectRemoteJSWrapper;
import io.redback.managers.jsmanager.Expression;

public abstract class ReportDataUnit extends ReportUnit {
	protected Expression valueExpr;
	protected PDFont font;
	protected float fontSize;
	protected Color color;
	protected float height;
	protected float width;
	protected String format;

	public ReportDataUnit(ReportManager rm, ReportConfig rc, DataMap c) throws RedbackException {
		super(rm, rc, c);
		jsParams = Arrays.asList(new String[] {"params", "object", "page"});
		valueExpr = new Expression(reportManager.getJSManager(), jsFunctionNameRoot + "_text_value", jsParams, c.getString("value"));
		width = config.containsKey("width") ? config.getNumber("width").floatValue() : -1;
		font = PDType1Font.HELVETICA;
		fontSize = config.containsKey("fontsize") ? config.getNumber("fontsize").floatValue() : 12f;
		height = 20f;
		color = config.containsKey("color") ? getColor(config.getString("color")) : Color.DARK_GRAY;
		format = config.getString("format");
	}


	public abstract ReportBox produce(Map<String, Object> context) throws IOException, RedbackException;
	
	
	protected String getSringValue(Map<String, Object> context) throws RedbackException {
		Map<String, Object> jsContext = new HashMap<String, Object>();
		RedbackObjectRemote object = (RedbackObjectRemote)context.get("object");
		jsContext.put("object", new RedbackObjectRemoteJSWrapper(object));
		jsContext.put("page", context.get("page"));
		Object value = valueExpr.eval(jsContext);
		String valueStr = value != null ? value.toString() : "";
		if(format != null) {
			if(format.equals("currency")) {
				NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.US);
				valueStr = formatter.format(Float.parseFloat(valueStr));
			} else if(format.equals("duration")) {
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
			} else if(format.equals("date") && value != null && value instanceof Date) {
				DateFormat formatter = DateFormat.getDateInstance();
				valueStr = formatter.format(value);
			} else if(format.equals("datetime") && value != null && value instanceof Date) {
				DateFormat formatter = DateFormat.getDateTimeInstance();
				valueStr = formatter.format(value);
			}
		}
		return valueStr;
	}
	


}
