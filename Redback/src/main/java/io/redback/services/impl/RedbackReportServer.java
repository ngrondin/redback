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

	protected Report produce(Session session, String name, String object, DataMap filter, String search) throws RedbackException {
		return reportManager.produce(session, name, object, filter, search);
	}

	protected String produceAndStore(Session session, String name, String object, DataMap filter, String search) throws RedbackException {
		return reportManager.produceAndStore(session, name, object, filter, search);
	}

	protected List<ReportInfo> list(Session session, String category) throws RedbackException {
		return reportManager.list(session, category);
	}

	protected void clearDomainCache(Session session, String domain, String name) throws RedbackException {
		reportManager.clearDomainCache(session, domain, name);
	}

}
