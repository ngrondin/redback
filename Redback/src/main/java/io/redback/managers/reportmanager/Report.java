package io.redback.managers.reportmanager;

import java.util.List;
import java.util.Map;

import io.firebus.script.Expression;
import io.firebus.script.exceptions.ScriptException;
import io.redback.exceptions.RedbackException;
import io.redback.security.Session;
import io.redback.utils.ReportFilter;

public abstract class Report {
	protected Session session;
	protected ReportManager reportManager;
	protected ReportConfig reportConfig;
	protected Expression nameExpression;
		
	public Report(Session s, ReportManager rm, ReportConfig rc) throws RedbackException {
		session = s;
		reportManager = rm;
		reportConfig = rc;
		try	{
			if(rc.getData().containsKey("outputname")) {
				nameExpression = rm.getScriptFactory().createExpression(rc.getName() + "_name", rc.getData().getString("outputname")) ;				
			}
		} catch(Exception e) {
			throw new RedbackException("Error initialising report", e);
		}
	}
	
	protected String produceOutputName(Map<String, Object> context, String def) throws RedbackException {
		String name = def;
		if(nameExpression != null) {
			try {
				Object ret = nameExpression.eval(context);
				if(ret != null && ret instanceof String)
					name = (String)ret;
			} catch (ScriptException e) {
				throw new RedbackException("Error producing report outputname", e);
			}
		}
		return name;
	}
	
	public abstract ProducedReport produce(List<ReportFilter> filters) throws RedbackException;
	
}
