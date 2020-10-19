package io.redback.test;

import io.redback.utils.StringUtils;

public class LevenshteinTest {

	public static void main(String[] args) {
		String s = "Web DesignThis is a sample description...";
		String s1 = "Web Design";
		String s2 = "000003 - Simple Meter Fix";
		System.out.println(StringUtils.levenshtein(s, s1));
		System.out.println(StringUtils.levenshtein(s, s2));
	}
}
