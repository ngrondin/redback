package io.redback.managers.reportmanager.pdf;

import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDSimpleFont;
import org.apache.pdfbox.pdmodel.font.encoding.Encoding;

public class Utils {
	public static float textWidth(PDFont font, float fontSize, String text) {
		if(text != null) {
			try {
				String str = text.replaceAll("\t", "").replaceAll("\u00A0", "").replaceAll("\uFEFF", "");
				return font.getStringWidth(str) / 1000f * fontSize;
			} catch(Exception e) {
				return 0;
			}
		} else {
			return 0;
		}
	}
	
	public static String cleanText(PDFont font, String text) {
		Encoding encoding = ((PDSimpleFont) font).getEncoding();
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			if (".notdef".equals(encoding.getName(c)) == false)
				sb.append(c);
			else
				sb.append(' ');
		}
		return sb.toString();
	}
}
