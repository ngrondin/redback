package io.redback.managers.reportmanager.pdf;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.Base64;
import java.util.Map;

import javax.imageio.ImageIO;

import io.firebus.data.DataMap;
import io.firebus.script.Expression;
import io.redback.exceptions.RedbackException;
import io.redback.managers.reportmanager.ReportConfig;
import io.redback.managers.reportmanager.ReportManager;
import io.redback.utils.ImageUtils;
import io.redback.utils.RedbackFile;

public class Image extends Unit {
	protected String base64;
	protected Expression base64Expr;
	
	public Image(ReportManager rm, ReportConfig rc, DataMap c) throws RedbackException {
		super(rm, rc, c);
		try {
			if(config.containsKey("base64expr")) {
				base64Expr = reportManager.getScriptFactory().createExpression(jsFunctionNameRoot + "_image_base64", config.getString("base64expr"));
			} else if(config.containsKey("base64")) {
				base64 = config.getString("base64");
			}
		} catch(Exception e) {
			throw new RedbackException("Error intialising image", e);
		}
	}

	public Box produce(Map<String, Object> context) throws RedbackException {
		try {
			Box rb = null;
			if(base64 != null || base64Expr != null) {
				if(base64Expr != null) {
					base64 = (String)base64Expr.eval(getJSContext(context));
				}
				String parts[] = base64.split(",");
				byte[] bytes = Base64.getDecoder().decode(parts[1]);
				BufferedImage img = ImageIO.read(new ByteArrayInputStream(bytes));
				rb = Box.Image(bytes, img.getWidth(), img.getHeight());
				rb.breakBefore = pagebreak;
			} else {
				RedbackFile file = (RedbackFile)context.get("file");
				if(file != null) {
					int ori = ImageUtils.getOrientation(file.bytes);
					BufferedImage img = ImageUtils.getImage(file.bytes, -1, -1, ori);
					rb = Box.Image(ImageUtils.getBytes(img, "png"), img.getWidth(), img.getHeight());
					rb.breakBefore = pagebreak;
				} 
			} 
			if(rb != null) {
				overrideWidth(rb, context);
				overrideHeight(rb, context);
			}
			return rb;
		} catch(Exception e) {
			throw new RedbackException("Error producing image", e);
		}
	}

}
