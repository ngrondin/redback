package io.redback.utils.js;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;

import javax.imageio.ImageIO;

import io.redback.exceptions.RedbackException;

public class BufferedImageJSWrapper  extends ObjectJSWrapper {
	private BufferedImage image;
	private Graphics2D graphics;

	public BufferedImageJSWrapper(BufferedImage i) {
		super(new String[] {"rect", 
				"text"});
		image = i;
		graphics = (Graphics2D) image.getGraphics();
	}

	public Object get(String key) throws RedbackException {
		BufferedImageJSWrapper self = this;
		if(key.equals("text")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					String t = arguments[0].toString();
					long x = (long)arguments[1];
					long y = (long)arguments[2];
					long w = arguments.length >= 4 ? (long)arguments[3] : -1;
					String txt = t;
					if(w > -1) {
						FontMetrics fm = graphics.getFontMetrics();
						while(fm.stringWidth(txt) > w) 
							txt = txt.substring(0, txt.length() - 1);
					}
					graphics.drawString(txt, (int)x, (int)y);
					return self;
				}
			};
		} else if(key.equals("fillrect")) {
				return new CallableJSWrapper() {
					public Object call(Object... arguments) throws RedbackException {
						long x = (long)arguments[0];
						long y = (long)arguments[1];
						long w = (long)arguments[2];
						long h = (long)arguments[3];
						graphics.fillRect((int)x, (int)y, (int)w, (int)h);
						return self;
					}
				};	
		} else if(key.equals("line")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					long x1 = (long)arguments[0];
					long y1 = (long)arguments[1];
					long x2 = (long)arguments[2];
					long y2 = (long)arguments[3];
					graphics.drawLine((int)x1, (int)y1, (int)x2, (int)y2);
					return self;
				}
			};					
		} else if(key.equals("setColor")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					String colorStr = arguments[0].toString();
					Color color = Color.decode(colorStr);
					graphics.setColor(color);
					return self;
				}
			};
		} else if(key.equals("setFontSize")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					long size = (long)arguments[0];
					Font currentFont = graphics.getFont();
					Font newFont = currentFont.deriveFont((float)size);
					graphics.setFont(newFont);
					return self;
				}
			};	
		} else if(key.equals("setStroke")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					String type = arguments[0].toString();
					BasicStroke stroke = null;
					if(type.equals("dash")) {
						float dash1[] = {3.0f}; 
						stroke = new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 2f, dash1, 0f);
					} else {
						stroke = new BasicStroke();
					}
					graphics.setStroke(stroke);
					return self;
				}
			};				
		} else if(key.equals("getTextWidth")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					String text = arguments[0].toString();
					int w = graphics.getFontMetrics().stringWidth(text);
					return w;
				}
			};				
		} else if(key.equals("getBase64")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					ByteArrayOutputStream os = new ByteArrayOutputStream();
					try {
					    ImageIO.write(image, "png", os);
					    return "data:image/png;base64," + Base64.getEncoder().encodeToString(os.toByteArray());
					} catch(Exception e) {
						throw new RuntimeException("Error getting base64 data of image", e);
					}
				}
			};
		} else {	
			return null;
		}
	}
	
}
