package io.redback.services.impl;

import io.firebus.Firebus;
import io.firebus.data.DataMap;
import io.firebus.logging.Logger;
import io.redback.client.AccessManagementClient;
import io.redback.client.DataClient;
import io.redback.client.ObjectClient;
import io.redback.security.Session;
import io.redback.services.Initializer;
import io.redback.tools.ImportData;

public class RedbackInitializer extends Initializer {
	protected DataClient dataClient;
	protected ObjectClient objectClient;
	protected AccessManagementClient amClient;
	protected String objectService;

	
	public RedbackInitializer(String n, DataMap c, Firebus f) {
		super(n, c, f);
		objectService = c.getString("objectservice");
		dataClient = new DataClient(firebus, c.getString("dataservice"));
		objectClient = new ObjectClient(firebus, objectService);
		amClient = new AccessManagementClient(firebus, c.getString("accessmanagementservice"));
	}


	public void start() {
		super.start();
		try {
    	    String token = amClient.getSysUserToken(new Session());
			ImportData importData = new ImportData(firebus, token, objectService, "root", "classpath:/io/redback/data/root.json");
			importData.setPreLoad(true);
            importData.importDataAsync();
		} catch(Exception e) {
			Logger.severe("sp.init.start", e);
		}
	}
}
