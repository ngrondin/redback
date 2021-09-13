package io.redback.managers.reportmanager.pdf;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Map;

import javax.imageio.ImageIO;

import io.firebus.data.DataMap;
import io.redback.exceptions.RedbackException;
import io.redback.managers.reportmanager.ReportConfig;
import io.redback.managers.reportmanager.ReportManager;
import io.redback.utils.ImageUtils;
import io.redback.utils.RedbackFile;

public class Image extends Unit {
	protected String base64;
	//protected Expression fileUidExpr;
	protected float width;
	protected float height;
	
	public Image(ReportManager rm, ReportConfig rc, DataMap c) throws RedbackException {
		super(rm, rc, c);
		/*jsParams = Arrays.asList(new String[] {"file", "master"});
		if(c.containsKey("fileuid")) {
			fileUidExpr = new Expression(reportManager.getJSManager(), jsFunctionNameRoot + "_filter", jsParams, c.getString("fileuid"));	
		}
		*/
		base64 = config.containsKey("base64") ? config.getString("base64") : null;
		width = config.containsKey("width") ? config.getNumber("width").floatValue() : -1;
		height = config.containsKey("height") ? config.getNumber("height").floatValue() : -1;
	}

	public Box produce(Map<String, Object> context) throws IOException, RedbackException {
		if(base64 != null) {
			String parts[] = base64.split(",");
			byte[] bytes = Base64.getDecoder().decode(parts[1]);
			BufferedImage img = ImageIO.read(new ByteArrayInputStream(bytes));
			Box rb = Box.Image(bytes, width == -1 ? img.getWidth() : width, height == -1 ? img.getHeight() : height);
			rb.breakBefore = pagebreak;
			return rb;
		} else {
			RedbackFile file = (RedbackFile)context.get("file");
			if(file != null) {
				int ori = ImageUtils.getOrientation(file.bytes);
				BufferedImage img = ImageUtils.getImage(file.bytes, (int)width, (int)height, ori);
				Box rb = Box.Image(ImageUtils.getBytes(img, "png"), img.getWidth(), img.getHeight());
				rb.breakBefore = pagebreak;
				return rb;
			} else {
				return null;
			}
		} 
	}

}
