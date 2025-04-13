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

import io.firebus.data.DataEntity;
import io.firebus.data.DataList;
import io.firebus.data.DataLiteral;
import io.firebus.data.DataMap;
import io.firebus.script.Expression;
import io.firebus.script.ScriptContext;
import io.firebus.script.exceptions.ScriptException;
import io.redback.client.ObjectClient;
import io.redback.client.RedbackObjectRemote;
import io.redback.client.js.ObjectClientJSWrapper;
import io.redback.client.js.RedbackObjectRemoteJSWrapper;
import io.redback.exceptions.RedbackException;
import io.redback.managers.jsmanager.ExpressionMap;
import io.redback.managers.reportmanager.Report;
import io.redback.managers.reportmanager.ReportConfig;
import io.redback.managers.reportmanager.ReportManager;
import io.redback.security.Session;

public class CSVReport extends Report {
	protected String object;
	protected List<String> jsParams;
	protected Expression filterExp;
	protected ExpressionMap filterExpMap;
	protected Expression sortExp;
	protected ExpressionMap sortExpMap;
	protected DataList columns;
	protected ExpressionMap varsExpMap;
	protected ScriptContext baseContext;
	protected String separator;
	protected byte[] bytes;
	
	public CSVReport(Session s, ReportManager rm, ReportConfig rc) throws RedbackException {
		super(s, rm, rc);
		try {
			DataMap c = rc.getData().getObject("content");
			jsParams = Arrays.asList(new String[] {"filter", "object"});
			object = c.getString("object");
			if(c.containsKey("filter")) {
				DataEntity filter = c.get("filter");
				if(filter instanceof DataMap)
					filterExpMap = new ExpressionMap(reportManager.getScriptFactory(), "csvdataset_filter", ((DataMap)filter));
				else if(filter instanceof DataLiteral)
					filterExp = reportManager.getScriptFactory().createExpression("csvdataset_filter", ((DataLiteral)filter).getString());	
			}
			if(c.containsKey("sort")) {
				DataEntity sort = c.get("sort");
				if(sort instanceof DataMap)
					sortExpMap = new ExpressionMap(reportManager.getScriptFactory(), "sort", ((DataMap)sort));
				else if(sort instanceof DataLiteral)
					sortExp = reportManager.getScriptFactory().createExpression("sort", ((DataLiteral)sort).getString());	
			}	
			if(c.containsKey("vars")) {
				varsExpMap = new ExpressionMap(reportManager.getScriptFactory(), "csvdataset_filter", c.getObject("vars"));
			}
			if(c.containsKey("columns")) {
				columns = c.getList("columns");
			}
			separator = c.containsKey("separator") ? c.getString("separator") : ",";
			baseContext = reportManager.getScriptFactory().createScriptContext();
		} catch(Exception e) {
			throw new RedbackException("Error initialising csv report", e);
		}
	}

	public void produce(String filterObject, DataMap filter, String search) throws RedbackException {
		try {
			ObjectClient oc = reportManager.getObjectClient();
			Map<String, Expression> exprCache = new HashMap<String, Expression>();
			baseContext.declare("filterobjectname", filterObject);
			baseContext.declare("filter", filter);
			baseContext.declare("search", search);
			baseContext.declare("oc", new ObjectClientJSWrapper(oc, this.session));
			if(varsExpMap != null) {
				DataMap vars = varsExpMap.eval(baseContext);
				baseContext.declare("vars", vars);
			}
			DataMap localFilter = (filterExp != null ? (DataMap)filterExp.eval(baseContext) : filterExpMap.eval(baseContext));
			DataMap localSort = (sortExp != null ? (DataMap)sortExp.eval(baseContext) : sortExpMap != null ? sortExpMap.eval(baseContext) : null);
			List<RedbackObjectRemote> rors = oc.listAllObjects(session, object, localFilter, localSort, true, false);
			StringBuilder sb = new StringBuilder();
			for(int i = 0; i < columns.size(); i++) {
				if(i > 0) sb.append(",");
				sb.append(columns.getObject(i).getString("label"));
			}
			for(int i = 0; i < rors.size(); i++) {
				sb.append("\r\n");
				ScriptContext rowContext = baseContext.createChild();
				rowContext.declare("object", new RedbackObjectRemoteJSWrapper(rors.get(i)));
				for(int j = 0; j < columns.size(); j++) {
					if(j > 0) sb.append(separator);
					DataMap colCfg = columns.getObject(j);
					String exprStr = colCfg.getString("expression");
					String attribute = colCfg.getString("attribute");
					String format = colCfg.getString("format");
					boolean quote = colCfg.getBoolean("quote");
					Object value = null;
					if(exprStr != null) {
						Expression expr = exprCache.get(exprStr);
						if(expr == null) {
							expr = this.reportManager.getScriptFactory().createExpression(exprStr);
							exprCache.put(exprStr, expr);
						}
						value = expr.eval(rowContext);
					} else if(attribute != null) {
						value = rors.get(i).getObject(attribute);
					}
					String valueStr = "";
					if(value != null) {
						if(format != null) {
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
						} else {
							valueStr = value.toString();
						}
					}
					if(quote)
						valueStr = "\"" + valueStr + "\"";
					sb.append(valueStr);
				}
			}
			bytes = sb.toString().getBytes();
		} catch(ScriptException e) {
			throw new RedbackException("Error producing csv report", e);
		}
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
