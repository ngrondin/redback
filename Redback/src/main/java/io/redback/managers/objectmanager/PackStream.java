package io.redback.managers.objectmanager;

import java.util.ArrayList;
import java.util.List;

import io.firebus.data.DataMap;
import io.firebus.logging.Logger;
import io.redback.security.Session;
import io.redback.utils.stream.DataStream;
import io.redback.utils.stream.DataStreamNextHandler;

public class PackStream extends DataStream<RedbackObject> implements DataStreamNextHandler {
	protected Session session;
	protected ObjectManager objectManager;
	protected DataStream<RedbackObject> upStream;
	protected List<RedbackObject> objects = new ArrayList<RedbackObject>();
	protected List<DataMap> queries = new ArrayList<DataMap>();
	protected int phase = 0;
	protected int i = 0;
	
	public PackStream(Session s, ObjectManager om, DataStream<RedbackObject> ds) {
		session = s;
		objectManager = om;
		upStream = ds;
		upStream.setNextHandler(this);
	}
	
	public void addObjects(List<RedbackObject> list) {
		objects.addAll(list);
	}
	
	public void addQuery(String object, DataMap filter) {
		queries.add(new DataMap("objectname", object, "filter", filter));
	}

	public void sendNext() {
		if(phase == 0) {
			if(i < objects.size()) {
				RedbackObject rbo = objects.get(i++);
				upStream.send(rbo);
			} else {
				phase = 1;
				i = 0;
				createNextStream();
			}
		} else if(phase == 1) {
			if(i <= queries.size()) {
				requestNext();
			} 
		}
	}
	
	protected void received(RedbackObject data) {
		upStream.send(data);
	}

	protected void completed() {
		createNextStream();
	}
	
	protected void createNextStream() {
		try {
			if(phase == 1 && i < queries.size()) {
				DataMap query = queries.get(i++);
				objectManager.streamObjects(session, query.getString("objectname"), query.getObject("filter"), null, null, -1, 0, this);
			} else {
				upStream.complete();
			}
		} catch(Exception e) {
			Logger.severe("rb.om.packstreamer", e);
		}
	}
	
	public void start() {
		sendNext();
	}
}
