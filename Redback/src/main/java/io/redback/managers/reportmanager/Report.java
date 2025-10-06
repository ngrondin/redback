package io.redback.managers.reportmanager;

import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;

import io.redback.exceptions.RedbackException;
import io.redback.security.Session;
import io.redback.utils.ReportFilter;

public abstract class Report {
	protected Session session;
	protected ReportManager reportManager;
	protected ReportConfig reportConfig;
	protected PDDocument document;
		
	public Report(Session s, ReportManager rm, ReportConfig rc) throws RedbackException {
		session = s;
		reportManager = rm;
		reportConfig = rc;
	}
	
	public abstract void produce(List<ReportFilter> filters) throws RedbackException;
	
	public abstract String getMime();
	
	public abstract String getFilename();
	
	public abstract byte[] getBytes() throws RedbackException;
}
