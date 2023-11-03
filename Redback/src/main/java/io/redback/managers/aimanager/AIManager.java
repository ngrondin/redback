package io.redback.managers.aimanager;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import io.firebus.Firebus;
import io.firebus.data.DataMap;
import io.redback.client.AccessManagementClient;
import io.redback.client.ConfigClient;
import io.redback.client.DataClient;
import io.redback.client.GatewayClient;
import io.redback.client.ObjectClient;
import io.redback.client.ProcessClient;
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
	protected String processServiceName;
	protected String gatewayServiceName;
	protected ConfigCache<ModelConfig> modelConfigs;
	protected AccessManagementClient accessManagementClient;
	protected DataClient dataClient;
	protected ConfigClient configClient;
	protected ObjectClient objectClient;
	protected ProcessClient processClient;
	protected GatewayClient gatewayClient;
	protected SequenceExecuter sequenceExecuter;
	protected DataMap urlMap;

	public AIManager(String n, DataMap config, Firebus fb) throws RedbackException
	{
		try {
			name = n;
			firebus = fb;
			configServiceName = config.getString("configservice");
			accessManagerServiceName = config.getString("accessmanagementservice");
			dataServiceName = config.getString("dataservice");
			objectServiceName = config.getString("objectservice");
			processServiceName = config.getString("processservice");
			gatewayServiceName = config.getString("gatewayservice");
			accessManagementClient = new AccessManagementClient(firebus, accessManagerServiceName);
			dataClient = new DataClient(firebus, dataServiceName);
			configClient = new ConfigClient(firebus, configServiceName);
			objectClient = new ObjectClient(firebus, objectServiceName);
			processClient = new ProcessClient(firebus, processServiceName);
			gatewayClient = new GatewayClient(firebus, gatewayServiceName);
			sequenceExecuter = new SequenceExecuter(objectClient, processClient);
			urlMap = config.getObject("urlmap");
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
	
	public NLCommandResponse runNLCommand(Session session, String model, String text, DataMap context) throws RedbackException {
		ModelConfig modelConfig = modelConfigs.get(session, model);
		if(modelConfig.getType().equals("nlcommand")) {
			ObjectContext oc = new ObjectContext(context.getString("objectname"), context.getString("uid"), context.getObject("filter"), context.getString("search"));
			List<String> seq = null;
			String respText = null;
			if(text.startsWith("[seq]:")) {
				String[] arr = text.substring(6).split(" ");
				seq = Arrays.asList(arr);
				respText = "Ok, here you go";
			} else {
				TimeZone tz = TimeZone.getTimeZone(session.getTimezone());
				DataMap request = new DataMap("text", text, "tz", tz.getOffset(new Date().getTime()));
				DataMap headers = new DataMap("apikey", modelConfig.getAPIKey());
				DataMap response = gatewayClient.call("post", modelConfig.getUrl(), request, headers, null);
				respText = response.getString("text");
				String actionsStr = response.getString("actions");
				if(actionsStr != null)
					seq = Arrays.asList(actionsStr.split(" "));
			}
			NLCommandResponse nlcr = null;
			if(seq != null)
				nlcr = sequenceExecuter.runSequence(session, seq, oc);
			else
				nlcr = new NLCommandResponse(null, null);
			if(!(nlcr.text != null && nlcr.text.length() > 0)) 
				nlcr.text = respText != null ? respText : "Ok.";
			return nlcr;
		} else {
			throw new RedbackInvalidRequestException("Model " + model + " is not of type nlcommand");
		}
	}
}
