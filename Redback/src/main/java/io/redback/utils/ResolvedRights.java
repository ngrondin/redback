package io.redback.utils;

import io.redback.security.Session;

public class ResolvedRights {
	public boolean read;
	public boolean write;
	public boolean execute;
	
	public ResolvedRights(boolean r, boolean w, boolean x) {
		read = r;
		write = w;
		execute = x;
	}
	
	public static ResolvedRights calculate(Session session, String rightKey) {
		boolean read = session.getUserProfile().canRead(rightKey);
		boolean write = session.getUserProfile().canWrite(rightKey);
		boolean execute = session.getUserProfile().canExecute(rightKey);
		return new ResolvedRights(read, write, execute);
	}
	
	public ResolvedRights and(ResolvedRights other) {
		if(other != null)
			return new ResolvedRights(read && other.read, write && other.write, execute && other.execute);
		else
			return this;
	}
	
	public ResolvedRights or(ResolvedRights other) {
		if(other != null)
			return new ResolvedRights(read || other.read, write || other.write, execute || other.execute);
		else
			return this;
	}
	
	public ResolvedRights copy() {
		return new ResolvedRights(read, write, execute);
	}

}
