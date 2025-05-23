package io.redback.managers.reportmanager;

import org.apache.pdfbox.pdmodel.PDDocument;

import io.firebus.data.DataMap;
import io.redback.exceptions.RedbackException;
import io.redback.security.Session;

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
	
	public abstract void produce(String object, DataMap filter, String search) throws RedbackException;
	
	public abstract String getMime();
	
	public abstract String getFilename();
	
	public abstract byte[] getBytes() throws RedbackException;
}
