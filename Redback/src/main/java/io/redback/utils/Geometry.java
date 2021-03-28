package io.redback.utils;

import io.firebus.utils.DataMap;

public class Geometry {
	protected DataMap config;
	
	public Geometry(DataMap c) {
		config = c;
	}
	
	public double getLatitude() {
		Number num = config.getNumber("coords.latitude");
		if(num != null)
			return num.doubleValue();
		else
			return 0;
	}
	
	public double getLongitude() {
		Number num = config.getNumber("coords.longitude");
		if(num != null)
			return num.doubleValue();
		else
			return 0;
	}
	
	public DataMap toDataMap() {
		return config;
	}
}
