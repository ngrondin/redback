package io.redback.managers.reportmanager.excel;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.firebus.utils.DataMap;
import io.redback.RedbackException;
import io.redback.client.js.ObjectClientJSWrapper;
import io.redback.managers.jsmanager.Function;
import io.redback.managers.reportmanager.Report;
import io.redback.managers.reportmanager.ReportConfig;
import io.redback.managers.reportmanager.ReportManager;
import io.redback.managers.reportmanager.excel.js.ExcelWorkbookJSWrapper;
import io.redback.security.Session;
import io.redback.utils.js.JSConverter;
import jxl.Workbook;
import jxl.write.WritableWorkbook;

public class ExcelReport extends Report {
	protected ByteArrayOutputStream baos;
	protected Function script;

	public ExcelReport(Session s, ReportManager rm, ReportConfig rc) throws RedbackException {
		super(s, rm, rc);
		List<String> jsParams = Arrays.asList(new String[] {"oc", "wb", "filter"});
		script = new Function(rm.getJSManager(), rc.getName(), jsParams, rc.getData().getString("content"));
	}

	public void produce(DataMap filter) throws RedbackException {
		try {
			baos = new ByteArrayOutputStream();
			WritableWorkbook workbook = Workbook.createWorkbook(baos);
			Map<String, Object> context = new HashMap<String, Object>();
			context.put("wb", new ExcelWorkbookJSWrapper(workbook));
			context.put("oc", new ObjectClientJSWrapper(reportManager.getObjectClient(), session));
			context.put("filter", JSConverter.toJS(filter));
			script.execute(context);
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
