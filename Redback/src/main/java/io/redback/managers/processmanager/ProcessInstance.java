package io.redback.managers.processmanager;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;


import io.firebus.utils.DataList;
import io.firebus.utils.DataMap;
import io.redback.managers.processmanager.js.ProcessManagerJSWrapper;
import io.redback.utils.js.FirebusJSWrapper;
import io.redback.utils.js.JSConverter;

public class ProcessInstance 
{
	protected ProcessManager processManager;
	protected String processName;
	protected int processVersion;
	protected String domain;
	protected UUID id;
	protected DataMap data;
	protected String currentNode;
	protected boolean complete;
	protected DataList assignees;
	protected Actionner lastActioner;
	protected Map<String, Object> scriptContext;
	//protected JSONList receivedNotifications;
	
	protected ProcessInstance(ProcessManager pm, String pn, int v, String dom, DataMap d)
	{
		processManager = pm;
		processName = pn;
		processVersion = v;
		domain = dom;
		id = UUID.randomUUID();
		data = d;	
		complete = false;
		assignees = new DataList();
		createScriptBindings();
		//receivedNotifications = new JSONList();
	}
	
	protected ProcessInstance(ProcessManager pm, DataMap c)
	{
		processManager = pm;
		processName = c.getString("process");
		processVersion = c.getNumber("version").intValue();
		domain = c.getString("domain");
		id = UUID.fromString(c.getString("_id"));
		currentNode = c.getString("currentnode");
		complete = c.getBoolean("compelte");
		data = c.getObject("data");
		if(c.containsKey("assignees")  &&  c.get("assignees") instanceof DataList)
			assignees = c.getList("assignees");
		else
			assignees = new DataList();
		
		if(c.containsKey("lastactioner")  &&  c.get("lastactioner") instanceof DataMap)
			lastActioner = new Actionner(c.getObject("lastactioner"));
		createScriptBindings();
	}
	
	protected void createScriptBindings()
	{
		scriptContext = new HashMap<String, Object>();
		try {
			scriptContext.put("pid", getId());
			scriptContext.put("pm", new ProcessManagerJSWrapper(processManager, this));
			scriptContext.put("firebus", new FirebusJSWrapper(processManager.getFirebus(), processManager.getProcessUserSession(domain)));
			scriptContext.put("processuser", processManager.getProcessUserSession(domain).getUserProfile().getUsername());
			updateScriptBindings();
		} catch(Exception e) {
		}
	}
	
	public void updateScriptBindings()
	{
		scriptContext.put("data", JSConverter.toJS(data));
	}
	
	public String getId()
	{
		return id.toString();
	}
	
	public String getProcessName()
	{
		return processName;
	}
	
	public int getProcessVersion()
	{
		return processVersion;
	}
	
	public String getDomain()
	{
		return domain;
	}
	
	public DataMap getData()
	{
		return data;
	}
	
	public Map<String, Object> getScriptContext()
	{
		return scriptContext;
	}
	
	public void setData(DataMap d)
	{
		Iterator<String> it = d.keySet().iterator();
		while(it.hasNext())
		{
			String key = it.next();
			data.put(key, d.get(key));
		}
		updateScriptBindings();
	}
	
	public void setOriginator(String id)
	{
		data.put("originator", id);
		updateScriptBindings();
	}
	
	public void setCurrentNode(String cn)
	{
		if(cn == null)
			complete = true;
		currentNode = cn;
	}
	
	public String getCurrentNode()
	{
		return currentNode;
	}
	
	public void addAssignee(Assignee a)
	{
		assignees.add(a.getJSON());
	}
	
	public void clearAssignees()
	{
		assignees = new DataList();
	}
	
	public ArrayList<Assignee> getAssignees()
	{
		ArrayList<Assignee> ret = new ArrayList<Assignee>();
		for(int i = 0; i < assignees.size(); i++)
			ret.add(new Assignee(assignees.getObject(i)));
		return ret;
	}
	
	public Assignee getAssigneeById(String id) 
	{
		for(int i = 0; i < assignees.size(); i++)
			if(assignees.getObject(i).getString("id").equals(id))
				return new Assignee(assignees.getObject(i));
		return null;
	}
	
	public void setLastActioner(Actionner la)
	{
		lastActioner = la;
	}
	
	public boolean isComplete()
	{
		return complete;
	}
	
	public DataMap getJSON()
	{
		DataMap retVal = new DataMap();
		retVal.put("_id", id.toString());
		retVal.put("process", processName);
		retVal.put("version", processVersion);
		retVal.put("domain", domain);
		retVal.put("currentnode", currentNode);
		retVal.put("lastupdate", new Date());
		if(assignees.size() > 0)
			retVal.put("assignees", assignees);
		if(lastActioner != null)
			retVal.put("lastactioner", lastActioner.getJSON());
		//if(receivedNotifications.size() > 0)
		//	retVal.put("receivednotifications", receivedNotifications);
		retVal.put("data", data);
		return retVal;
	}
	
}
