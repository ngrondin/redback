package io.redback.managers.reportmanager.csv;

import java.text.NumberFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import io.firebus.utils.DataEntity;
import io.firebus.utils.DataList;
import io.firebus.utils.DataLiteral;
import io.firebus.utils.DataMap;
import io.redback.RedbackException;
import io.redback.client.ObjectClient;
import io.redback.client.RedbackObjectRemote;
import io.redback.managers.jsmanager.Expression;
import io.redback.managers.jsmanager.ExpressionMap;
import io.redback.managers.reportmanager.Report;
import io.redback.managers.reportmanager.ReportConfig;
import io.redback.managers.reportmanager.ReportManager;
import io.redback.security.Session;
import io.redback.utils.js.JSConverter;

public class CSVReport extends Report {
	protected String object;
	protected List<String> jsParams;
	protected Expression filterExp;
	protected ExpressionMap filterExpMap;
	protected Expression sortExp;
	protected ExpressionMap sortExpMap;
	protected DataList columns;
	protected byte[] bytes;
	
	public CSVReport(Session s, ReportManager rm, ReportConfig rc) throws RedbackException {
		super(s, rm, rc);
		DataMap c = rc.getData().getObject("content");
		jsParams = Arrays.asList(new String[] {"filter", "object"});
		object = c.getString("object");
		if(c.containsKey("filter")) {
			DataEntity filter = c.get("filter");
			if(filter instanceof DataMap)
				filterExpMap = new ExpressionMap(reportManager.getJSManager(), "csvdataset_filter", jsParams, ((DataMap)filter));
			else if(filter instanceof DataLiteral)
				filterExp = new Expression(reportManager.getJSManager(), "csvdataset_filter", jsParams, ((DataLiteral)filter).getString());	
		}
		if(c.containsKey("sort")) {
			DataEntity sort = c.get("sort");
			if(sort instanceof DataMap)
				sortExpMap = new ExpressionMap(reportManager.getJSManager(), "sort", jsParams, ((DataMap)sort));
			else if(sort instanceof DataLiteral)
				sortExp = new Expression(reportManager.getJSManager(), "sort", jsParams, ((DataLiteral)sort).getString());	
		}	
		if(c.containsKey("columns")) {
			columns = c.getList("columns");
		}
	}

	public void produce(DataMap filter) throws RedbackException {
		Map<String, Object> jsContext = new HashMap<String, Object>();
		jsContext.put("filter", JSConverter.toJS(filter));
		ObjectClient oc = reportManager.getObjectClient();
		DataMap localFilter = (filterExp != null ? (DataMap)filterExp.eval(jsContext) : filterExpMap.eval(jsContext));
		DataMap localSort = (sortExp != null ? (DataMap)sortExp.eval(jsContext) : sortExpMap != null ? sortExpMap.eval(jsContext) : null);
		List<RedbackObjectRemote> rors = oc.listAllObjects(session, object, localFilter, localSort, true);
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < columns.size(); i++) {
			if(i > 0) sb.append(",");
			sb.append(columns.getObject(i).getString("label"));
		}
		for(int i = 0; i < rors.size(); i++) {
			sb.append("\r\n");
			for(int j = 0; j < columns.size(); j++) {
				if(j > 0) sb.append(",");
				DataMap colCfg = columns.getObject(j);
				String attribute = colCfg.getString("attribute");
				String format = colCfg.getString("format");
				DataEntity e = rors.get(i).get(attribute);
				Object value = null;
				if(e instanceof DataLiteral) {
					value = ((DataLiteral)e).getObject();
				} else {
					value = e;
				}
				String valueStr = value != null ? value.toString() : "";
				if(value != null && format != null) {
					if(format.equals("currency")) {
						try {
							NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.US);
							valueStr = formatter.format(Float.parseFloat(valueStr));
						} catch(Exception ex) {
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
						} catch(Exception ex) {
							valueStr = "Bad data for duration";
						}
					} else if(format.equals("date") && value != null && value instanceof Date) {
						try {
							ZoneId zoneId = session.timezone != null ? ZoneId.of(session.timezone) : ZoneId.systemDefault();
							ZonedDateTime zdt = ZonedDateTime.ofInstant(((Date)value).toInstant(), zoneId);
							valueStr = zdt.format(DateTimeFormatter.ISO_LOCAL_DATE);
						} catch(Exception ex) {
							valueStr = "Bad data for date";
						}
					} else if(format.equals("datetime") && value != null && value instanceof Date) {
						try {
							ZoneId zoneId = session.timezone != null ? ZoneId.of(session.timezone) : ZoneId.systemDefault();
							ZonedDateTime zdt = ZonedDateTime.ofInstant(((Date)value).toInstant(), zoneId);
							valueStr = zdt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
						} catch(Exception ex) {
							valueStr = "Bad data for datetime";
						}
					} else if(format.equals("time") && value != null && value instanceof Number) {
						try {
							Long ms = ((Long)value).longValue();
							Date date = new Date(1000 * (ms / 1000));
							ZonedDateTime zdt = ZonedDateTime.ofInstant(date.toInstant(), ZoneId.of("UTC"));
							valueStr = zdt.format(DateTimeFormatter.ISO_LOCAL_TIME);
						} catch(Exception ex) {
							valueStr = "Bad data for duration";
						}
					}
				}
				sb.append(valueStr);
			}
		}
		bytes = sb.toString().getBytes();
	}

	public String getMime() {
		return "application/csv";
	}

	public byte[] getBytes() throws RedbackException {
		return bytes;
	}
	
	public String getFilename() {
		return reportConfig.getName() + ".csv";
	}

}
