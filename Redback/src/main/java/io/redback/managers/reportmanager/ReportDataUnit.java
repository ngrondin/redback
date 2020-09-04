package io.redback.managers.reportmanager;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
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
	protected float height;
	protected float width;

	public ReportDataUnit(ReportManager rm, ReportConfig rc, DataMap c) throws RedbackException {
		super(rm, rc, c);
		jsParams = Arrays.asList(new String[] {"params", "object"});
		valueExpr = new Expression(reportManager.getJSManager(), jsFunctionNameRoot + "_text_value", jsParams, c.getString("value"));
		width = config.containsKey("width") ? config.getNumber("width").floatValue() : -1;
		font = PDType1Font.TIMES_ROMAN;
		fontSize = 12f;
		height = 20f;
	}

	public abstract ReportBox produce(Map<String, Object> context) throws IOException, RedbackException;
	
	protected String getSringValue(Map<String, Object> context) throws RedbackException {
		Map<String, Object> jsContext = new HashMap<String, Object>();
		RedbackObjectRemote object = (RedbackObjectRemote)context.get("object");
		jsContext.put("object", new RedbackObjectRemoteJSWrapper(object));
		Object value = valueExpr.eval(jsContext);
		String valueStr = value.toString();
		return valueStr;
	}
	
	

}
