package io.redback.services;

import java.util.logging.Logger;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.information.ServiceInformation;
import io.firebus.utils.DataException;
import io.firebus.utils.DataMap;
import io.redback.RedbackException;
import io.redback.managers.reportmanager.Report;
import io.redback.security.Session;

public abstract class ReportServer extends AuthenticatedServiceProvider {

	private Logger logger = Logger.getLogger("io.redback");

	public ReportServer(String n, DataMap c, Firebus f) {
		super(n, c, f);
	}

	public Payload authenticatedService(Session session, Payload payload) throws RedbackException {
		logger.finer("Report service start");
		Payload response = null;
		try
		{
			DataMap request = new DataMap(payload.getString());
			String get = request.getString("get");
			String action = request.getString("action");
			String reportName = get != null && get.length() > 1 ? get.substring(1) : request.getString("report");
			String domain = request.getString("domain");
			String format = request.getString("format");
			DataMap filter = request.get("filter") instanceof DataMap ? request.getObject("filter") : new DataMap(request.getString("filter"));
			if(action == null) 
				action = "produce";
			
			if(action.equals("produce")) {
				Report report = produce(session, domain, reportName, filter);
				if(format != null && format.equals("json")) {
					//TODO Add json format
				} else {
					response = new Payload(report.getBytes());
					response.metadata.put("mime", "application/pdf");
				}
			} else if(action.equals("producestore")) {
				String fileUid = produceAndStore(session, domain, reportName, filter);
				response = new Payload(new DataMap("fileuid", fileUid).toString());
			} else{
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

	public Payload unAuthenticatedService(Session session, Payload payload)	throws RedbackException
	{
		throw new RedbackException("All requests need to be authenticated");
	}

	public ServiceInformation getServiceInformation() {
		return null;
	}
	
	protected abstract Report produce(Session session, String domain, String name, DataMap filter) throws RedbackException;
	
	protected abstract String produceAndStore(Session session, String domain, String name, DataMap filter) throws RedbackException;
}
