package io.redback.managers.reportmanager.pdf;

import java.io.IOException;
import java.util.Map;

import io.firebus.data.DataMap;
import io.redback.exceptions.RedbackException;
import io.redback.managers.reportmanager.ReportConfig;
import io.redback.managers.reportmanager.ReportManager;

public class Text extends DataUnit {
	public static int ALIGN_LEFT = 0;
	public static int ALIGN_RIGHT = 1;
	public static int ALIGN_CENTER = 2;
	protected int align = 0;
	protected int valign = 0;
	protected boolean bold = false;
	
	public Text(ReportManager rm, ReportConfig rc, DataMap c) throws RedbackException {
		super(rm, rc, c);
		String a = config.getString("align");
		if(a != null) {
			if(a.equalsIgnoreCase("right"))
				align = ALIGN_RIGHT;
			else if(a.equalsIgnoreCase("center"))
				align = ALIGN_CENTER;
		}
		if(config.containsKey("bold")) bold = config.getBoolean("bold");
	}

	public Box produce(Map<String, Object> context) throws IOException, RedbackException {
		String valueStr = getSringValue(context);
		Box rb = Box.Text(valueStr, bold ? boldFont : font, fontSize(context));
		rb.color = color;
		rb.breakBefore = pagebreak;
		overrideHeight(rb, context);
		float widthOverride = width(context);
		if(widthOverride > -1) {
			Box text = rb;
			rb = Box.HContainer(false);
			rb.addChild(text);
			rb.width = widthOverride;
			if(align == ALIGN_RIGHT) {
				text.x = rb.width - text.width;
			} else if(align == ALIGN_CENTER) {
				text.x = (rb.width - text.width) / 2;
			}
		}
		return rb;
	}

}
