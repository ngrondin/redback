package io.redback.services.impl;

import java.util.List;

import io.firebus.Firebus;
import io.firebus.data.DataMap;
import io.redback.exceptions.RedbackException;
import io.redback.managers.reportmanager.Report;
import io.redback.managers.reportmanager.ReportInfo;
import io.redback.managers.reportmanager.ReportManager;
import io.redback.security.Session;
import io.redback.services.ReportServer;

public class RedbackReportServer extends ReportServer {
	
	protected ReportManager reportManager;

	public RedbackReportServer(String n, DataMap c, Firebus f) throws RedbackException {
		super(n, c, f);
		reportManager = new ReportManager(f, c);
	}
	
	public void configure() {
		reportManager.clearCaches();	
	}

	protected Report produce(Session session, String name, DataMap filter) throws RedbackException {
		return reportManager.produce(session, name, filter);
	}

	protected String produceAndStore(Session session, String name, DataMap filter) throws RedbackException {
		return reportManager.produceAndStore(session, name, filter);
	}

	protected List<ReportInfo> list(Session session, String category) throws RedbackException {
		return reportManager.list(session, category);
	}

	protected void clearDomainCache(Session session, String domain, String name) throws RedbackException {
		reportManager.clearDomainCache(session, domain, name);
	}

}
