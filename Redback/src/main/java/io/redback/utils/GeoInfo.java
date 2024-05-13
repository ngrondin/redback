package io.redback.utils;

import io.firebus.data.DataMap;

public class GeoInfo {
	public Geometry geometry;
	public String address;
	public DataMap addressParts;
	
	public GeoInfo(DataMap data) {
		geometry = new Geometry(data.getObject("geometry"));
		address = data.getString("address");
		addressParts = data.getObject("addressparts");
	}
	
	public GeoInfo(Geometry g, String a, DataMap ap ) {
		geometry = g;
		address = a;
		addressParts = ap;
	}
	
	public DataMap toDataMap() {
		return new DataMap("geometry", geometry.toDataMap(), "address", address, "addressparts", addressParts);
	}
}
