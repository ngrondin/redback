package io.redback.utils;

import java.util.List;

public class NLCommandResponse {
	public String text;
	public String sequence;
	public List<String> uiactions;
	
	public NLCommandResponse(String t, String seq, List<String> a) {
		text = t;
		sequence = seq;
		uiactions = a;
	}
}
