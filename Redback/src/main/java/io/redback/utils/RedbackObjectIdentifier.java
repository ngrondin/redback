package io.redback.utils;

public class RedbackObjectIdentifier {
	public String objectname;
	public String uid;
	private String label;
	
	public RedbackObjectIdentifier(String on, String i) {
		objectname = on;
		uid = i;
		label = (objectname + ":" + uid);
	}
	
    @Override
    public int hashCode() {
        return label.hashCode();
    }
    
    public boolean equals(Object o) {
    	return o instanceof RedbackObjectIdentifier && ((RedbackObjectIdentifier)o).label.equals(label);
    }
    
    public String toString() {
    	return label;
    }
}
