package io.redback.utils;

public class ResolvedRights {
	public boolean read;
	public boolean write;
	public boolean execute;
	
	public ResolvedRights(boolean r, boolean w, boolean x) {
		read = r;
		write = w;
		execute = x;
	}
	
	public ResolvedRights and(ResolvedRights other) {
		return new ResolvedRights(read && other.read, write && other.write, execute && other.execute);
	}

}
