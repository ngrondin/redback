package io.redback.client;

import java.util.ArrayList;
import java.util.List;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.utils.DataList;
import io.firebus.utils.DataMap;
import io.redback.RedbackException;
import io.redback.managers.reportmanager.ReportInfo;
import io.redback.security.Session;

public class ReportClient extends Client {

	public ReportClient(Firebus fb, String sn) {
		super(fb, sn);
	}
	
	public byte[] produce(Session session, String name, DataMap filter) throws RedbackException {
		return produce(session, null, name, filter);
	}
		
	public byte[] produce(Session session, String domain, String name, DataMap filter) throws RedbackException {
		try {
			DataMap req = new DataMap();
			req.put("action", "produce");
			if(domain != null)
				req.put("domain", domain);
			req.put("report", name);
			req.put("filter", filter);
			Payload resp = requestPayload(session, new Payload(req.toString()));
			return resp.data;
		} catch(Exception e) {
			throw new RedbackException("Error producing report", e);
		}
	}

	public String produceAndStore(Session session, String name, DataMap filter) throws RedbackException {
		return produceAndStore(session, null, name, filter);
	}
		
	public String produceAndStore(Session session, String domain, String name, DataMap filter) throws RedbackException {
		try {
			DataMap req = new DataMap();
			req.put("action", "producestore");
			if(domain != null)
				req.put("domain", domain);
			req.put("report", name);
			req.put("filter", filter);
			DataMap resp = request(session, req);
			return resp.getString("fileuid");
		} catch(Exception e) {
			throw new RedbackException("Error producing and storing report", e);
		}
	}
	
	public List<ReportInfo> list(Session session, String category) throws RedbackException {
		try {
			DataMap req = new DataMap();
			req.put("action", "list");
			req.put("category", category);
			DataMap resp = request(session, req);
			DataList result = resp.getList("result");
			List<ReportInfo> list = new ArrayList<ReportInfo>();
			for(int i = 0; i < list.size(); i++)
				list.add(new ReportInfo(result.getObject(i)));
			return list;
		} catch(Exception e) {
			throw new RedbackException("Error producing and storing report", e);
		}		
	}

}
