package io.redback.managers.reportmanager.excel;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.firebus.script.Function;
import io.redback.client.js.ObjectClientJSWrapper;
import io.redback.exceptions.RedbackException;
import io.redback.managers.reportmanager.Report;
import io.redback.managers.reportmanager.ReportConfig;
import io.redback.managers.reportmanager.ReportManager;
import io.redback.managers.reportmanager.excel.js.ExcelWorkbookJSWrapper;
import io.redback.security.Session;
import io.redback.security.js.SessionJSWrapper;
import io.redback.utils.ReportFilter;
import jxl.Workbook;
import jxl.write.WritableWorkbook;

public class ExcelReport extends Report {
	protected ByteArrayOutputStream baos;
	protected Function script;

	public ExcelReport(Session s, ReportManager rm, ReportConfig rc) throws RedbackException {
		super(s, rm, rc);
		try	{
			script = rm.getScriptFactory().createFunction(rc.getName(), new String[] {"oc", "wb", "filter"}, rc.getData().getString("content"));
		} catch(Exception e) {
			throw new RedbackException("Error initialising excel report", e);
		}
	}

	public void produce(List<ReportFilter> filters) throws RedbackException {
		try {
			baos = new ByteArrayOutputStream();
			WritableWorkbook workbook = Workbook.createWorkbook(baos);
			Map<String, Object> context = new HashMap<String, Object>();
			context.put("session", new SessionJSWrapper(session));
			context.put("timezone", session.getTimezone());
			context.put("wb", new ExcelWorkbookJSWrapper(session, workbook));
			context.put("oc", new ObjectClientJSWrapper(reportManager.getObjectClient(), session));			
			if(filters.size() >= 1) {
				context.put("filterobjectname", filters.get(0).object);
				context.put("filter", filters.get(0).filter);
				context.put("search", filters.get(0).search);
				context.put("uid", filters.get(0).uid);
			}
			context.put("sets", ReportFilter.convertToDataList(filters));
			script.call(context);
			workbook.write();
			workbook.close();
		} catch(Exception e) {
			throw new RedbackException("Error producing Excel report", e);
		}
	}

	public String getMime() {

		return "application/excel";
	}
	
	public String getFilename() {
		return reportConfig.getName() + ".xls";
	}

	public byte[] getBytes() throws RedbackException {
		return baos.toByteArray();
	}

}
