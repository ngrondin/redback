package io.redback.services;

import java.util.List;
import java.util.logging.Logger;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.information.ServiceInformation;
import io.firebus.utils.DataException;
import io.firebus.utils.DataList;
import io.firebus.utils.DataMap;
import io.redback.RedbackException;
import io.redback.managers.reportmanager.Report;
import io.redback.managers.reportmanager.ReportInfo;
import io.redback.security.Session;
import io.redback.services.common.AuthenticatedServiceProvider;

public abstract class ReportServer extends AuthenticatedServiceProvider {

	private Logger logger = Logger.getLogger("io.redback");

	public ReportServer(String n, DataMap c, Firebus f) {
		super(n, c, f);
	}

	public Payload redbackAuthenticatedService(Session session, Payload payload) throws RedbackException {
		logger.finer("Report service start");
		Payload response = null;
		try
		{
			DataMap request = new DataMap(payload.getString());
			String get = request.getString("get");
			String action = request.getString("action");
			String reportName = get != null && get.length() > 1 ? get.substring(1) : request.getString("report");
			String domain = request.getString("domain");
			String category = request.getString("category");
			String timezone = request.getString("timezone");
			DataMap filter = request.containsKey("filter") ? (request.get("filter") instanceof DataMap ? request.getObject("filter") : new DataMap(request.getString("filter"))) : null;
			if(action == null) 
				action = "produce";
			if(timezone != null)
				session.setTimezone(timezone);
			
			if(action.equals("produce")) {
				Report report = produce(session, domain, reportName, filter);
				response = new Payload(report.getBytes());
				response.metadata.put("mime", report.getMime());
				response.metadata.put("filename", report.getFilename());
			} else if(action.equals("producestore")) {
				String fileUid = produceAndStore(session, domain, reportName, filter);
				response = new Payload(new DataMap("fileuid", fileUid).toString());
			} else if(action.equals("list")) {
				List<ReportInfo> reports = list(session, category);
				DataList result = new DataList();
				for(ReportInfo ri: reports)
					result.add(ri.toDataMap());
				response = new Payload(new DataMap("result", result).toString());
			} else if(action.equals("cleardomaincache")) {
				this.clearDomainCache(session, domain, reportName);
			} else {
				throw new RedbackException("No valid action was provided");
			}			
		}
		catch(DataException e)
		{
			throw new RedbackException("Error in report server", e);
		}		

		logger.finer("Report service finish");
		return response;	
	}

	public Payload redbackUnauthenticatedService(Session session, Payload payload)	throws RedbackException
	{
		throw new RedbackException("All requests need to be authenticated");
	}

	public ServiceInformation getServiceInformation() {
		return null;
	}
	
	protected abstract Report produce(Session session, String domain, String name, DataMap filter) throws RedbackException;
	
	protected abstract String produceAndStore(Session session, String domain, String name, DataMap filter) throws RedbackException;
	
	protected abstract List<ReportInfo> list(Session session, String category) throws RedbackException;
	
	protected abstract void clearDomainCache(Session session, String domain, String name) throws RedbackException;
}
