package io.redback.client;

import java.util.ArrayList;
import java.util.List;

import io.redback.exceptions.RedbackException;

public class RedbackObjectRemoteList extends ArrayList<RedbackObjectRemote> {

	private static final long serialVersionUID = 1L;
	
	public RedbackObjectRemoteList() {
		
	}
	
	public RedbackObjectRemoteList(List<RedbackObjectRemote> list) {
		this.addAll(list);
	}
	
	public RedbackObjectRemoteList filter(String attribute, String val) throws RedbackException {
		RedbackObjectRemoteList ret = new RedbackObjectRemoteList();
		if(val != null && attribute != null) {
			for(RedbackObjectRemote obj: this) {
				if(val.equals(obj.getString(attribute))) ret.add(obj);
			}
		}
		return ret;
	}
	
	public RedbackObjectRemote find(String attribute, String val) throws RedbackException {
		if(val == null || attribute == null) return null;
		for(RedbackObjectRemote obj: this) {
			if(val.equals(obj.getString(attribute))) return obj;
		}
		return null;
	}
	
	public List<String> uniqueListOfAttribute(String attribute) throws RedbackException {
		List<String> ret = new ArrayList<String>();
		for(RedbackObjectRemote ror: this) {
			String val = ror.getString(attribute);
			if(!ret.contains(val)) ret.add(val);
		}
		return ret;
	}
	
	public List<String> getHierarchyOf(RedbackObjectRemote ror, String attribute, String parentAttribute, String childAttribute) throws RedbackException {
		List<String> ret = new ArrayList<String>();
		String val = ror.getString(attribute);
		ret.add(val);
		String parentId = ror.getString(parentAttribute);
		RedbackObjectRemote parent = this.find(childAttribute, parentId);
		if(parent != null) {
			List<String> phier = getHierarchyOf(parent, attribute, parentAttribute, childAttribute);
			ret.addAll(phier);
		}
		return ret;
	}
}
