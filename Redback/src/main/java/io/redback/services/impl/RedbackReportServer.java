package io.redback.services.impl;

import io.firebus.Firebus;
import io.firebus.utils.DataMap;
import io.redback.RedbackException;
import io.redback.managers.reportmanager.Report;
import io.redback.managers.reportmanager.ReportManager;
import io.redback.security.Session;
import io.redback.services.ReportServer;

public class RedbackReportServer extends ReportServer {
	
	protected ReportManager reportManager;

	public RedbackReportServer(String n, DataMap c, Firebus f) {
		super(n, c, f);
		reportManager = new ReportManager(f, c);
	}

	protected Report produce(Session session, String name, DataMap filter) throws RedbackException {
		return reportManager.produce(session, name, filter);
	}

	protected String produceAndStore(Session session, String name, DataMap filter) throws RedbackException {
		return reportManager.produceAndStore(session, name, filter);
	}

	public void clearCaches() {
		reportManager.clearCaches();
	}


}
