package io.redback.test;

import io.firebus.utils.DataMap;
import io.redback.utils.CollectionConfig;

public class CollectionConfigTest {

	public static void main(String[] args) {
		DataMap specific = new DataMap();
		specific.put("guid", "0001");
		specific.put("attachmentname", "text.txt");
		specific.put("other", "othervalue");
		
		DataMap config = new DataMap();
		config.put("name", "link");
		DataMap map = new DataMap();
		map.put("filuid", "guid");
		map.put("linkid", "guid");
		map.put("filename", "attachmentname");
		config.put("map", map);
		CollectionConfig cc = new CollectionConfig(config);
		
		DataMap canonical = cc.convertObjectToCanonical(specific); 
		System.out.println(canonical);

		DataMap newSpecific = cc.convertObjectToSpecific(canonical); 
		System.out.println(newSpecific);
	}
}
