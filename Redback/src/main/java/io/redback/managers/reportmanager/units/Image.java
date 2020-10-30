package io.redback.managers.reportmanager.units;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Map;

import javax.imageio.ImageIO;


import io.firebus.utils.DataMap;
import io.redback.RedbackException;
import io.redback.managers.reportmanager.ReportBox;
import io.redback.managers.reportmanager.ReportConfig;
import io.redback.managers.reportmanager.ReportManager;
import io.redback.managers.reportmanager.ReportUnit;

public class Image extends ReportUnit {
	protected String base64;
	protected float width;
	protected float height;
	
	public Image(ReportManager rm, ReportConfig rc, DataMap c) throws RedbackException {
		super(rm, rc, c);
		base64 = config.containsKey("base64") ? config.getString("base64") : null;
		width = config.containsKey("width") ? config.getNumber("width").floatValue() : -1;
		height = config.containsKey("height") ? config.getNumber("height").floatValue() : -1;
	}

	public ReportBox produce(Map<String, Object> context) throws IOException, RedbackException {
		if(base64 != null) {
		String parts[] = base64.split(",");
			byte[] bytes = Base64.getDecoder().decode(parts[1]);
			BufferedImage img = ImageIO.read(new ByteArrayInputStream(bytes));
			ReportBox rb = ReportBox.Image(bytes, width == -1 ? img.getWidth() : width, height == -1 ? img.getHeight() : height);
			return rb;
		} else {
			return null;
		}
	}

}
