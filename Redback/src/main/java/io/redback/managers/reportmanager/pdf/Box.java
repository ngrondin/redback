package io.redback.managers.reportmanager.pdf;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.pdmodel.font.PDFont;

public class Box {
	public String type;
	public String text;
	public PDFont font;
	public float fontSize;
	public Color color;
	public Color borderColor;
	public float lineWidth;
	public byte[] bytes;
	public boolean checked;
	public boolean canBreak;
	public boolean breakBefore;
	public boolean vertical;
	public float x;
	public float y;
	public float width;
	public float height;
	public List<Box> children;
	
	private Box() {
		children = new ArrayList<Box>();
	}
	
	public static Box VContainer(boolean canBreak) {
		Box rb = new Box();
		rb.type = "container";
		rb.vertical = true;
		rb.canBreak = canBreak;
		rb.width = 0;
		rb.height = 0;
		return rb;
	}
	
	public static Box HContainer(boolean canBreak) {
		Box rb = new Box();
		rb.type = "container";
		rb.vertical = false;
		rb.canBreak = canBreak;
		rb.width = 0;
		rb.height = 0;
		return rb;
	}
	
	public static Box HLine(float w, float h) {
		Box rb = new Box();
		rb.type = "hline";
		rb.canBreak = false;
		rb.width = w;
		rb.height = h;
		rb.color = Color.DARK_GRAY;
		return rb;
	}
	
	public static Box Text(String text, PDFont font, float fontSize, float w, float h) {
		Box rb = new Box();
		rb.type = "text";
		rb.text = text;
		rb.font = font;
		rb.fontSize = fontSize;
		rb.canBreak = false;
		rb.width = w;
		rb.height = h;
		rb.color = Color.DARK_GRAY;
		return rb;
	}
	
	public static Box Text(String text, PDFont font, float fontSize) {
		Box rb = new Box();
		rb.type = "text";
		rb.text = text;
		rb.font = font;
		rb.fontSize = fontSize;
		rb.canBreak = false;
		rb.width = Utils.textWidth(font, fontSize, text);
		rb.height = 2f * (font.getFontDescriptor().getCapHeight()) / 1000 * fontSize;;
		rb.color = Color.DARK_GRAY;
		return rb;
	}
	
	public static Box Checkbox(boolean checked, float w, float h) {
		Box rb = new Box();
		rb.type = "checkbox";
		rb.checked = checked;
		rb.canBreak = false;
		rb.width = w;
		rb.height = h;
		rb.color = Color.DARK_GRAY;
		return rb;
	}
	
	public static Box Image(byte[] bytes, float w, float h) {
		Box rb = new Box();
		rb.type = "image";
		rb.bytes = bytes;
		rb.canBreak = false;
		rb.width = w;
		rb.height = h;
		return rb;
	}
	
	public static Box Empty(float w, float h) {
		Box rb = new Box();
		rb.type = "empty";
		rb.canBreak = false;
		rb.width = w;
		rb.height = h;
		return rb;
	}
	
	public void addChild(Box c) {
		if(c != null) {
			children.add(c);
			if(vertical) {
				c.x = 0;
				c.y = height;
				height += c.height;
				if(c.width > width)
					width = c.width;
			} else {
				c.x = width;
				c.y = 0;
				width += c.width;
				if(c.height > height)
					height = c.height;
			}
		}
	}
	
	public Box cloneWithoutChildren() {
		Box clone = new Box();
		clone.canBreak = canBreak;
		clone.checked = checked;
		clone.font = font;
		clone.fontSize = fontSize;
		clone.lineWidth = lineWidth;
		clone.text = text;
		clone.type = type;
		clone.vertical = vertical;
		clone.width = 0;
		clone.height = 0;
		clone.x = 0;
		clone.y = 0;
		return clone;
	}
	
	public void resolveBreakPoints(List<Float> breakPoints, float offset) {
		float h = offset;
		if(breakBefore)
			breakPoints.add(h);
		for(Box child: children) {
			child.resolveBreakPoints(breakPoints, h);
			h += child.height;
		}
	}
	
	public Box breakAt(float bp) {
		if(height > bp && canBreak) {
			if(vertical) {
				float y = 0;
				int i = 0;
				while(i < children.size() && y + children.get(i).height <= bp) {
					y += children.get(i).height;
					i++;
				}
				if(i < children.size()) {
					Box clone = cloneWithoutChildren();
					Box limitRb = children.get(i);
					if( bp > limitRb.y && limitRb.canBreak) {
						float prevHeight = limitRb.height;
						Box newChild = limitRb.breakAt(bp - y);
						height -= (prevHeight - limitRb.height);
						clone.addChild(newChild);
						i++;
					} 
					while(children.size() > i) {
						Box child = children.remove(i);
						height -= child.height;
						clone.addChild(child);
					}
					return clone;
				}
			}
		}
		return null;
	}
	
	public String toString() {
		return toString(0);
	}
	
	public String toString(int indent) {
		String s = "";
		for(int i = 0; i < indent; i++)
			s = s + "  ";
		s = s  + type + " [" + width + ", " + height;
		if(type.equals("text"))
			s = s + ", " + text;
		s = s + ", " + canBreak + "]";
		for(Box rb: children) {
			s = s + "\r\n" + rb.toString(indent + 1);
		}
		return s;
	}

}
