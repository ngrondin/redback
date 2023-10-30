package io.redback.managers.aimanager;

import java.util.Arrays;
import java.util.List;

import io.firebus.Firebus;
import io.firebus.data.DataMap;
import io.redback.client.AccessManagementClient;
import io.redback.client.ConfigClient;
import io.redback.client.DataClient;
import io.redback.client.ObjectClient;
import io.redback.exceptions.RedbackException;
import io.redback.exceptions.RedbackInvalidRequestException;
import io.redback.security.Session;
import io.redback.utils.ConfigCache;
import io.redback.utils.NLCommandResponse;

public class AIManager {
	protected String name;
	protected Firebus firebus;
	protected String configServiceName;
	protected String accessManagerServiceName;
	protected String dataServiceName;
	protected String objectServiceName;
	protected ConfigCache<ModelConfig> modelConfigs;
	protected AccessManagementClient accessManagementClient;
	protected DataClient dataClient;
	protected ConfigClient configClient;
	protected ObjectClient objectClient;
	protected SequenceExecuter sequenceExecuter;

	public AIManager(String n, DataMap config, Firebus fb) throws RedbackException
	{
		try {
			name = n;
			firebus = fb;
			configServiceName = config.getString("configservice");
			accessManagerServiceName = config.getString("accessmanagementservice");
			dataServiceName = config.getString("dataservice");
			objectServiceName = config.getString("objectservice");
			accessManagementClient = new AccessManagementClient(firebus, accessManagerServiceName);
			dataClient = new DataClient(firebus, dataServiceName);
			configClient = new ConfigClient(firebus, configServiceName);
			objectClient = new ObjectClient(firebus, objectServiceName);
			sequenceExecuter = new SequenceExecuter(objectClient);
			AIManager aim = this;
			modelConfigs = new ConfigCache<ModelConfig>(configClient, "rbai", "model", 3600000, new ConfigCache.ConfigFactory<ModelConfig>() {
				public ModelConfig createConfig(DataMap map) throws Exception {
					return new ModelConfig(aim, map);
				}
			});
		} catch(Exception e) {
			throw new RedbackException("Error initialising AI Manager", e);
		}
	}
	
	public NLCommandResponse runNLCommand(Session session, String model, String text) throws RedbackException {
		ModelConfig modelConfig = modelConfigs.get(session, model);
		if(modelConfig.getType().equals("nlcommand")) {
			List<String> seq = null;
			List<String> residual = null;
			String respText = null;
			if(text.startsWith("[seq]:")) {
				String[] arr = text.substring(6).split(" ");
				seq = Arrays.asList(arr);
				respText = "Ok, here you go";
			} else {
				respText = "Would call model now";
				//Call model
			}
			if(seq != null) {
				residual = sequenceExecuter.runSequence(session, seq);
			}
			NLCommandResponse nlcr = new NLCommandResponse(respText, residual);
			return nlcr;
		} else {
			throw new RedbackInvalidRequestException("Model " + model + " is not of type nlcommand");
		}
	}
}
