package io.redback.client;


import java.util.Base64;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.data.DataException;
import io.firebus.data.DataList;
import io.firebus.data.DataMap;
import io.redback.exceptions.RedbackException;

public class GatewayClient extends Client
{

	public GatewayClient(Firebus fb, String sn) 
	{
		super(fb, sn);
		setTimeout(60000);
	}
	
	public DataMap get(String url) throws RedbackException {
		DataMap req = new DataMap();
		req.put("method", "get");
		req.put("url", url);
		return call(req);
	}

	public DataMap get(String url, DataMap headers, DataMap cookie) throws RedbackException {
		DataMap req = new DataMap();
		req.put("method", "get");
		req.put("url", url);
		if(headers != null)
			req.put("headers", headers);
		if(cookie != null)
			req.put("cookie", cookie);
		return call(req);	
	}

	public DataMap post(String url, Object body) throws RedbackException {
		DataMap req = new DataMap();
		req.put("method", "post");
		req.put("url", url);
		req.put("body", body);
		return call(req);	
	}
	
	public DataMap postForm(String url, DataMap form) throws RedbackException {
		DataMap req = new DataMap();
		req.put("method", "post");
		req.put("url", url);
		req.put("form", form);
		return call(req);	
	}

	public DataMap post(String url, Object body, DataMap headers, DataMap cookie) throws RedbackException {
		DataMap req = new DataMap();
		req.put("method", "post");
		req.put("url", url);
		req.put("body", body);
		if(headers != null)
			req.put("headers", headers);
		if(cookie != null)
			req.put("cookie", cookie);
		return call(req);	
	}

	public DataMap postForm(String url, DataMap form, DataMap headers, DataMap cookie) throws RedbackException {
		DataMap req = new DataMap();
		req.put("method", "post");
		req.put("url", url);
		req.put("form", form);
		if(headers != null)
			req.put("headers", headers);
		if(cookie != null)
			req.put("cookie", cookie);
		return call(req);
	}
	
	public DataMap put(String url, Object body) throws RedbackException {
		DataMap req = new DataMap();
		req.put("method", "put");
		req.put("url", url);
		req.put("body", body);
		return call(req);	
	}
	
	public DataMap putForm(String url, DataMap form) throws RedbackException {
		DataMap req = new DataMap();
		req.put("method", "put");
		req.put("url", url);
		req.put("form", form);
		return call(req);	
	}

	public DataMap put(String url, Object body, DataMap headers, DataMap cookie) throws RedbackException {
		DataMap req = new DataMap();
		req.put("method", "put");
		req.put("url", url);
		req.put("body", body);
		if(headers != null)
			req.put("headers", headers);
		if(cookie != null)
			req.put("cookie", cookie);
		return call(req);	
	}

	public DataMap putForm(String url, DataMap form, DataMap headers, DataMap cookie) throws RedbackException {
		DataMap req = new DataMap();
		req.put("method", "put");
		req.put("url", url);
		req.put("form", form);
		if(headers != null)
			req.put("headers", headers);
		if(cookie != null)
			req.put("cookie", cookie);
		return call(req);
	}
	
	public DataMap patch(String url, Object body) throws RedbackException {
		DataMap req = new DataMap();
		req.put("method", "patch");
		req.put("url", url);
		req.put("body", body);
		return call(req);	
	}
	
	public DataMap patchForm(String url, DataMap form) throws RedbackException {
		DataMap req = new DataMap();
		req.put("method", "patch");
		req.put("url", url);
		req.put("form", form);
		return call(req);	
	}

	public DataMap patch(String url, Object body, DataMap headers, DataMap cookie) throws RedbackException {
		DataMap req = new DataMap();
		req.put("method", "patch");
		req.put("url", url);
		req.put("body", body);
		if(headers != null)
			req.put("headers", headers);
		if(cookie != null)
			req.put("cookie", cookie);
		return call(req);	
	}

	public DataMap patchForm(String url, DataMap form, DataMap headers, DataMap cookie) throws RedbackException {
		DataMap req = new DataMap();
		req.put("method", "patch");
		req.put("url", url);
		req.put("form", form);
		if(headers != null)
			req.put("headers", headers);
		if(cookie != null)
			req.put("cookie", cookie);
		return call(req);
	}	
	
	public DataMap call(String method, String url, Object body, DataMap headers, DataMap cookie) throws RedbackException {
		DataMap req = new DataMap();
		req.put("method", method);
		req.put("url", url);
		req.put("body", body);
		if(headers != null)
			req.put("headers", headers);
		if(cookie != null)
			req.put("cookie", cookie);
		return call(req);
	}	

	protected DataMap call(DataMap req) throws RedbackException
	{
		Payload resp = requestPayload(null, req, false);
		Object ret = resp.getDataObject();
		if(ret instanceof DataMap) {
			return (DataMap)ret;
		} if(ret instanceof DataList) {
			return new DataMap("list", ret);
		} else if(ret instanceof String) {
			String str = (String)ret;
			try {
				DataMap map = new DataMap(str);
				return map;
			} catch(DataException e1) {
				try {
					DataList list = new DataList(str);
					return new DataMap("list", list);
				} catch(DataException e2) {
					return new DataMap("result", str);					
				}
			}
		} else if(ret instanceof byte[]) {
			byte[] bytes = (byte[])ret;
			String str = new String(bytes);
			try {
				DataMap map = new DataMap(str);
				return map;
			} catch(DataException e1) {
				try {
					DataList list = new DataList(str);
					return new DataMap("list", list);
				} catch(DataException e2) {
					return new DataMap("base64", Base64.getEncoder().encode(bytes));		
				}
			}
		} else {
			return new DataMap();
		}
	}


}
