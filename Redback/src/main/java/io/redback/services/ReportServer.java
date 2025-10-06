package io.redback.services;

import java.util.ArrayList;
import java.util.List;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.data.DataException;
import io.firebus.data.DataList;
import io.firebus.data.DataMap;
import io.firebus.information.ServiceInformation;
import io.firebus.logging.Logger;
import io.redback.exceptions.RedbackException;
import io.redback.managers.reportmanager.Report;
import io.redback.managers.reportmanager.ReportInfo;
import io.redback.security.Session;
import io.redback.services.common.AuthenticatedServiceProvider;
import io.redback.utils.ReportFilter;

public abstract class ReportServer extends AuthenticatedServiceProvider {

	public ReportServer(String n, DataMap c, Firebus f) {
		super(n, c, f);
	}

	public Payload redbackAuthenticatedService(Session session, Payload payload) throws RedbackException {
		Logger.finer("rb.report.start", null);
		Payload response = null;
		try
		{
			DataMap request = payload.getDataMap();
			String get = request.getString("get");
			String action = request.getString("action");
			String reportName = get != null && get.length() > 1 ? get.substring(1) : request.getString("report");
			String domain = request.getString("domain");
			String category = request.getString("category");
			String timezone = request.getString("timezone");
			String object = request.getString("object");
			DataMap filter = request.containsKey("filter") ? (request.get("filter") instanceof DataMap ? request.getObject("filter") : new DataMap(request.getString("filter"))) : null;
			String search = request.getString("search");
			String uid = request.getString("uid");
			DataList sets = request.containsKey("sets") ? (request.get("sets") instanceof DataList ? request.getList("sets") : new DataList(request.getString("sets"))) : null;
			List<ReportFilter> reportFilters = new ArrayList<ReportFilter>();
			if(sets != null) {
				for(int i = 0; i < sets.size(); i++) {
					DataMap setItem = sets.getObject(i);
					ReportFilter reportFilter = new ReportFilter(setItem.getString("object"), setItem.getObject("filter"), setItem.getString("search"), setItem.getString("uid"));
					reportFilters.add(reportFilter);
				}
			} else {
				ReportFilter reportFilter = new ReportFilter(object, filter, search, uid);
				reportFilters.add(reportFilter);
			}
			if(action == null) 
				action = "produce";
			if(timezone != null)
				session.setTimezone(timezone);
			
			if(action.equals("produce")) {
				Report report = produce(session, reportName, reportFilters);
				response = new Payload(report.getBytes());
				response.metadata.put("mime", report.getMime());
				response.metadata.put("filename", report.getFilename());
			} else if(action.equals("producestore")) {
				String fileUid = produceAndStore(session, reportName, reportFilters);
				response = new Payload(new DataMap("fileuid", fileUid));
			} else if(action.equals("list")) {
				List<ReportInfo> reports = list(session, category);
				DataList result = new DataList();
				for(ReportInfo ri: reports)
					result.add(ri.toDataMap());
				response = new Payload(new DataMap("result", result));
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
		Logger.finer("rb.report.finish", null);
		return response;	
	}

	public Payload redbackUnauthenticatedService(Session session, Payload payload)	throws RedbackException
	{
		throw new RedbackException("All requests need to be authenticated");
	}

	public ServiceInformation getServiceInformation() {
		return null;
	}
	
	protected abstract Report produce(Session session, String name, List<ReportFilter> filters) throws RedbackException;
	
	protected abstract String produceAndStore(Session session, String name, List<ReportFilter> filters) throws RedbackException;
	
	protected abstract List<ReportInfo> list(Session session, String category) throws RedbackException;
	
	protected abstract void clearDomainCache(Session session, String domain, String name) throws RedbackException;
}
