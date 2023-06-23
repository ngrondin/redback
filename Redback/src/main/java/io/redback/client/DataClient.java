package io.redback.client;

import java.util.List;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.StreamEndpoint;
import io.firebus.data.DataList;
import io.firebus.data.DataMap;
import io.firebus.interfaces.StreamHandler;
import io.redback.exceptions.RedbackException;

public class DataClient extends Client
{
	public class DataTransaction 
	{
		protected DataMap req;
		
		protected DataTransaction(DataMap r) {
			req = r;
		}
	}
	
	public DataClient(Firebus fb, String sn) 
	{
		super(fb, sn);
	}
	
	public DataMap runTransaction(DataTransaction tx) throws RedbackException 
	{
		return requestDataMap(tx.req);
	}


	public DataTransaction createGet(String object, DataMap filter, DataMap sort, int page, int pageSize) {
		DataMap req = new DataMap();
		req.put("object", object);
		req.put("filter", filter);
		if(sort != null)
			req.put("sort", sort);
		req.put("page", page);
		req.put("pagesize", pageSize);
		return new DataTransaction(req);
	}
	
	public DataMap getData(String object, DataMap filter, DataMap sort, int page, int pageSize) throws RedbackException
	{
		if(pageSize > -1) {
			DataTransaction tx = createGet(object, filter, sort, page, pageSize);
			return runTransaction(tx);			
		} else {
			DataMap req = new DataMap();
			req.put("object", object);
			req.put("filter", filter);
			if(sort != null) req.put("sort", sort);
			DataList list = new DataList();
			StreamEndpoint sep = this.requestStream(null, req);
			sep.setHandler(new StreamHandler() {
				public void receiveStreamData(Payload payload, StreamEndpoint streamEndpoint) {
					try {
						list.add(payload.getDataMap());
						sep.send(new Payload("next"));
					} catch(Exception e) { }
				}

				public void streamClosed(StreamEndpoint streamEndpoint) {
					sep.close();
					sep.notify();
				}
			});
			try {
				synchronized(sep) {
					sep.wait();
				}
			} catch(Exception e) {}
			return new DataMap("result", list);
		}
	}
	
	public DataMap getData(String object, DataMap filter, DataMap sort) throws RedbackException
	{
		return getData(object, filter, sort, 0, 50);
	}	

	public DataMap getData(String object, DataMap filter) throws RedbackException
	{
		return getData(object, filter, null, 0, 50);
	}	
	
	
	public DataTransaction createPut(String object, DataMap key, DataMap data, boolean replace) {
		DataMap req = new DataMap();
		req.put("object", object);
		req.put("key", key);
		req.put("data", data);
		if(replace) 
			req.put("operation", "replace");
		return new DataTransaction(req);
	}
	
	public DataMap putData(String object, DataMap key, DataMap data, boolean replace) throws RedbackException
	{
		DataTransaction tx = createPut(object, key, data, replace);
		return requestDataMap(tx.req);
	}

	public DataMap putData(String object, DataMap key, DataMap data) throws RedbackException
	{
		return putData(object, key, data, false);
	}

	public DataMap aggregateData(String object, DataMap filter, DataList tuple, DataList metrics, DataMap sort, int page, int pageSize) throws RedbackException
	{
		DataMap req = new DataMap();
		req.put("object", object);
		req.put("filter", filter);
		req.put("tuple", tuple);
		req.put("metrics", metrics);
		if(sort != null)
			req.put("sort", sort);
		req.put("page", page);
		req.put("pagesize", pageSize);
		return requestDataMap(req);
	}

	public DataTransaction createPublish(String object, DataMap key, DataMap data) throws RedbackException
	{
		DataMap req = new DataMap();
		req.put("object", object);
		req.put("key", key);
		req.put("data", data);
		return new DataTransaction(req);
	}
	
	public void publishData(String object, DataMap key, DataMap data) throws RedbackException
	{
		DataTransaction tx = createPublish(object, key, data);
		runTransaction(tx);
	}
	
	public DataTransaction createDelete(String object, DataMap key) {
		DataMap req = new DataMap();
		req.put("object", object);
		req.put("key", key);
		req.put("operation", "delete");
		return new DataTransaction(req);
	}
	
	public DataMap deleteData(String object, DataMap key) throws RedbackException
	{
		DataTransaction tx = createDelete(object, key);
		return runTransaction(tx);
	}	
	
	public DataMap multi(List<DataTransaction> list) throws RedbackException
	{
		DataMap req = new DataMap();
		DataList multi = new DataList();
		for(DataTransaction tx: list)
			multi.add(tx.req);
		req.put("multi", multi);
		return requestDataMap(req);
	}
	
	
}
