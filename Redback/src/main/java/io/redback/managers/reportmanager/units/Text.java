package io.redback.managers.reportmanager.units;

import java.io.IOException;
import java.util.Map;

import io.firebus.utils.DataMap;
import io.redback.RedbackException;
import io.redback.managers.reportmanager.ReportBox;
import io.redback.managers.reportmanager.ReportConfig;
import io.redback.managers.reportmanager.ReportDataUnit;
import io.redback.managers.reportmanager.ReportManager;

public class Text extends ReportDataUnit {

	public Text(ReportManager rm, ReportConfig rc, DataMap c) throws RedbackException {
		super(rm, rc, c);
	}

	public ReportBox produce(Map<String, Object> context) throws IOException, RedbackException {
		String valueStr = getSringValue(context);
		ReportBox rb = ReportBox.Text(valueStr, font, fontSize);
		if(width > -1)
			rb.width = width;
		rb.height = height;
		rb.color = color;
		return rb;
	}

}
