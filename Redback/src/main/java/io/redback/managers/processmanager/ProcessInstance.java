package io.redback.managers.processmanager;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.UUID;

import io.firebus.data.DataList;
import io.firebus.data.DataMap;
import io.firebus.script.ScriptContext;
import io.firebus.script.exceptions.ScriptValueException;
import io.redback.client.js.ObjectClientJSWrapper;
import io.redback.exceptions.RedbackException;
import io.redback.managers.processmanager.js.ProcessManagerJSWrapper;
import io.redback.security.Session;
import io.redback.utils.js.FirebusJSWrapper;

public class ProcessInstance 
{
	protected Actionner inboundActionner;
	protected Actionner outboundActionner;
	protected ProcessManager processManager;
	protected String processName;
	protected int processVersion;
	protected String domain;
	protected UUID id;
	protected String objectUid;
	protected String objectName;
	protected String groupKey;
	protected DataMap data;
	protected String currentNode;
	protected boolean complete;
	protected DataMap interactionDetails;
	protected DataList assignees;
	protected Actionner lastActioner;
	protected Date lastActioned;
	protected ScriptContext scriptContext;
	protected boolean updated;
	
	protected ProcessInstance(Actionner a, ProcessManager pm, String pn, int v, String dom, String on, String ouid, String gk, DataMap d) throws RedbackException
	{
		inboundActionner = a;
		processManager = pm;
		processName = pn;
		processVersion = v;
		domain = dom;
		id = UUID.randomUUID();
		objectName = on;
		objectUid = ouid;
		groupKey = gk;
		data = d;	
		complete = false;
		assignees = new DataList();
		createScriptBindings();
		updated = true;
	}
	
	protected ProcessInstance(Actionner a, ProcessManager pm, DataMap c) throws RedbackException
	{
		inboundActionner = a;
		processManager = pm;
		processName = c.getString("process");
		processVersion = c.getNumber("version").intValue();
		domain = c.getString("domain");
		id = UUID.fromString(c.getString("_id"));
		objectName = c.getString("objectname");
		objectUid = c.getString("objectuid");
		groupKey = c.getString("groupkey");
		currentNode = c.getString("currentnode");
		complete = c.getBoolean("complete");
		data = c.getObject("data");
		if(c.containsKey("interaction"))
			interactionDetails = c.getObject("interaction");
		if(c.containsKey("assignees")  &&  c.get("assignees") instanceof DataList)
			assignees = c.getList("assignees");
		else
			assignees = new DataList();
		
		if(c.containsKey("lastactioner")  &&  c.get("lastactioner") instanceof DataMap)
			lastActioner = new Actionner(c.getObject("lastactioner"));
		if(c.containsKey("lastactioned"))
			lastActioned = c.getDate("lastactioned");
		createScriptBindings();
		updated = false;
	}
	
	protected void createScriptBindings() throws RedbackException
	{
		Session outboundSession = processManager.getSysUserManager().getSession(inboundActionner.getSession().getId());
		outboundSession.setScriptContext(inboundActionner.getSession().getScriptContext());
		outboundSession.setDomainLock(domain);
		outboundActionner = new Actionner(this, outboundSession);
		scriptContext = inboundActionner.getSession().getScriptContext().createChild();
		try {
			scriptContext.put("pid", getId());
			scriptContext.put("objectname", objectName);
			scriptContext.put("objectuid", objectUid);
			scriptContext.put("groupkey", groupKey);
			scriptContext.put("pm", new ProcessManagerJSWrapper(outboundActionner, processManager));
			scriptContext.put("firebus", new FirebusJSWrapper(processManager.getFirebus(), outboundActionner.getSession()));
			scriptContext.put("oc", new ObjectClientJSWrapper(processManager.getObjectClient(), outboundActionner.getSession()));
			scriptContext.put("processuser", outboundActionner.getUserProfile().getUsername());
			updateScriptBindings();
		} catch(Exception e) {
		}
	}
	
	public void updateScriptBindings()
	{
		try {
			scriptContext.put("data", data);
			scriptContext.put("lastactioned", lastActioned);
		} catch(ScriptValueException e) {
			//TODO: Throw this
		}
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
	
	public String getObjectName() 
	{
		return objectName;
	}
	
	public String getObjectUid()
	{
		return objectUid;
	}
	
	public String getGroupKey()
	{
		return groupKey;
	}
	
	public Actionner getOutboundActionner()
	{
		return outboundActionner;
	}
	
	public Actionner getInboundActionner()
	{
		return inboundActionner;
	}	
	
	public DataMap getData()
	{
		return data;
	}
	
	public ScriptContext getScriptContext()
	{
		return scriptContext;
	}
	
	public ProcessManager getProcessManager()
	{
		return processManager;
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
		updated = true;
	}
	
	public void setOriginator(String id)
	{
		data.put("originator", id);
		updateScriptBindings();
		updated = true;
	}
	
	public void setCurrentNode(String cn)
	{
		if(cn == null)
			complete = true;
		currentNode = cn;
		updated = true;
	}
	
	public String getCurrentNode()
	{
		return currentNode;
	}
	
	public void setInteractionDetails(DataMap d)
	{
		interactionDetails = d;
		updated = true;
	}
	
	public void clearInteractionDetails()
	{
		interactionDetails = null;
		updated = true;
	}
	
	public void addAssignee(Assignee a)
	{
		assignees.add(a.getJSON());
		updated = true;
	}
	
	public void clearAssignees()
	{
		assignees = new DataList();
		updated = true;
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
	
	public void setLastActioner(Actionner la, Date dt)
	{
		lastActioner = la;
		lastActioned = dt != null ? dt : new Date();
		updateScriptBindings();
		updated = true;
	}
	
	public boolean isComplete()
	{
		return complete;
	}
	
	public boolean isUpdated()
	{
		return updated;
	}
	
	public DataMap getJSON()
	{
		DataMap retVal = new DataMap();
		retVal.put("_id", id.toString());
		retVal.put("process", processName);
		retVal.put("version", processVersion);
		retVal.put("domain", domain);
		retVal.put("objectname", objectName);
		retVal.put("objectuid", objectUid);
		retVal.put("groupkey", groupKey);
		retVal.put("complete", complete);
		retVal.put("currentnode", currentNode);
		retVal.put("lastupdate", new Date());
		if(interactionDetails != null)
			retVal.put("interaction", interactionDetails);
		if(assignees.size() > 0)
			retVal.put("assignees", assignees);
		if(lastActioner != null)
			retVal.put("lastactioner", lastActioner.getJSON());
		if(lastActioned != null)
			retVal.put("lastactioned", lastActioned);
		retVal.put("data", data);
		return retVal;
	}
	
}
