package io.redback.utils;

import java.util.List;

public class NLCommandResponse {
	public String text;
	public List<String> actions;
	
	public NLCommandResponse(String t, List<String> a) {
		text = t;
		actions = a;
	}
}
