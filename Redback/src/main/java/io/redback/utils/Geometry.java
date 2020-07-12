package io.redback.utils;

import io.firebus.utils.DataMap;

public class Geometry {
	protected DataMap config;
	
	public Geometry(DataMap c) {
		config = c;
	}
	
	public double getLatitude() {
		return config.getNumber("coords.latitude").doubleValue();
	}
	
	public double getLongitude() {
		return config.getNumber("coords.longitude").doubleValue();
	}
	
	public DataMap toDataMap() {
		return config;
	}
}
