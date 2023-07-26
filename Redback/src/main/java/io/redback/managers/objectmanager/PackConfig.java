package io.redback.managers.objectmanager;

import java.util.ArrayList;
import java.util.List;

import io.firebus.data.DataList;
import io.firebus.data.DataMap;
import io.firebus.script.Function;
import io.firebus.script.exceptions.ScriptBuildException;
import io.redback.exceptions.RedbackException;
import io.redback.exceptions.RedbackInvalidConfigException;

public class PackConfig {
	protected ObjectManager objectManager;
	protected List<QueryConfig> queries;
	protected Function script;
	
	public PackConfig(ObjectManager om, DataMap d) throws RedbackException {
		objectManager = om;
		String name = d.getString("name");
		if(d.containsKey("queries")) {
			queries = new ArrayList<QueryConfig>();
			DataList list = d.getList("queries");
			for(int i = 0; i < list.size(); i++) {
				queries.add(new QueryConfig(objectManager, (name + "_" + i), list.getObject(i)));
			}
		} else if(d.containsKey("script")) {
			try {
				script = objectManager.getScriptFactory().createFunction(name + "_script", d.getString("script"));
			} catch(ScriptBuildException e) {
				throw new RedbackInvalidConfigException("Error reading pack script", e);
			}
		}
	}
	
	public boolean hasQueries() {
		return queries != null;
	}
	
	public List<QueryConfig> getQueries() {
		return queries;
	}
	
	public Function getScript() {
		return script;
	}

}