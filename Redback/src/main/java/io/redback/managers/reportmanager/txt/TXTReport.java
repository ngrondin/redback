package io.redback.managers.reportmanager.txt;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

import io.firebus.data.DataMap;
import io.firebus.script.Function;
import io.redback.client.js.ObjectClientJSWrapper;
import io.redback.exceptions.RedbackException;
import io.redback.managers.reportmanager.Report;
import io.redback.managers.reportmanager.ReportConfig;
import io.redback.managers.reportmanager.ReportManager;
import io.redback.security.Session;
import io.redback.security.js.SessionJSWrapper;

public class TXTReport extends Report {
	protected ByteArrayOutputStream baos;
	protected Function script;

	public TXTReport(Session s, ReportManager rm, ReportConfig rc) throws RedbackException {
		super(s, rm, rc);
		try	{
			script = rm.getScriptFactory().createFunction(rc.getName(), new String[] {"oc", "wb", "filter"}, rc.getData().getString("content"));
		} catch(Exception e) {
			throw new RedbackException("Error initialising txt report", e);
		}
	}

	public void produce(String object, DataMap filter, String search) throws RedbackException {
		try {
			baos = new ByteArrayOutputStream();
			Map<String, Object> context = new HashMap<String, Object>();
			context.put("session", new SessionJSWrapper(session));
			context.put("oc", new ObjectClientJSWrapper(reportManager.getObjectClient(), session));
			context.put("filterobjectname", object);
			context.put("filter", filter);
			context.put("search", search);
			Object out = script.call(context);
			baos.write(out.toString().getBytes());
		} catch(Exception e) {
			throw new RedbackException("Error producing TXT report", e);
		}
	}

	public String getMime() {

		return "txt/plain";
	}
	
	public String getFilename() {
		return reportConfig.getName() + ".txt";
	}

	public byte[] getBytes() throws RedbackException {
		return baos.toByteArray();
	}

}
