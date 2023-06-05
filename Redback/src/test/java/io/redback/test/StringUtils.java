package io.redback.test;

public class StringUtils {

	public static void main(String[] args) {
		String s = "allo mon coco";
		System.out.println(io.redback.utils.StringUtils.isHtml(s));
		String r = io.redback.utils.StringUtils.stripHtml(s);
		System.out.println(r);
	}
}
