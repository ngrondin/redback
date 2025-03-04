package io.redback.managers.reportmanager.pdf;

import java.io.IOException;
import java.util.Map;

import io.firebus.data.DataMap;
import io.redback.exceptions.RedbackException;
import io.redback.managers.reportmanager.ReportConfig;
import io.redback.managers.reportmanager.ReportManager;

public class Checkbox extends DataUnit {
	
	public Checkbox(ReportManager rm, ReportConfig rc, DataMap c) throws RedbackException {
		super(rm, rc, c);
	}

	public Box produce(Map<String, Object> context) throws IOException, RedbackException {
		
		Object val = getValue(context);
		boolean boolVal = val instanceof Boolean && ((Boolean)val) == true;
		Box rb = Box.Checkbox(boolVal, 12, 12);

		return rb;
	}

}
