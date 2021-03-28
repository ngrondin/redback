package io.redback.utils;

import io.firebus.utils.DataMap;

public class GeoRoute {
	protected DataMap config;
	
	public GeoRoute() {
		config = new DataMap();
	}
	
	public GeoRoute(DataMap c) {
		config = c;
	}
	
	public Geometry getStart() {
		return new Geometry(config.getObject("start"));
	}
	
	public Geometry getEnd() {
		return new Geometry(config.getObject("end"));
	}
	
	public long getDuration() {
		return config.getNumber("duration").longValue();
	}
	
	public long getDistance() {
		return config.getNumber("distance").longValue();
	}
	
	public DataMap toDataMap() {
		return config;
	}
}
