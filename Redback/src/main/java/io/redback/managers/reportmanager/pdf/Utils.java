package io.redback.managers.reportmanager.pdf;

import org.apache.pdfbox.pdmodel.font.PDFont;

public class Utils {
	public static float textWidth(PDFont font, float fontSize, String text) {
		if(text != null) {
			try {
				String str = text.replace("\t", "");
				return font.getStringWidth(str) / 1000f * fontSize;
			} catch(Exception e) {
				return 0;
			}
		} else {
			return 0;
		}
	}
}
