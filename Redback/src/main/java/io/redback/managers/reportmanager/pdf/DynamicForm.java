package io.redback.managers.reportmanager.pdf;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.firebus.data.DataList;
import io.firebus.data.DataMap;
import io.firebus.logging.Logger;
import io.redback.client.FileClient;
import io.redback.client.RedbackObjectRemote;
import io.redback.exceptions.RedbackException;
import io.redback.managers.reportmanager.ReportConfig;
import io.redback.managers.reportmanager.ReportManager;
import io.redback.security.Session;
import io.redback.utils.ImageUtils;
import io.redback.utils.RedbackFile;

public class DynamicForm extends DataUnit {
	//protected PDFont font;
	//protected float fontSize;
	protected float width;
	protected String orderAttribute;
	protected String valueAttribute;
	protected String optionsAttribute;
	protected String typeAttribute;
	protected String catOrderAttribute;
	protected String catAttribute;
	protected String labelAttribute;
	protected String detailAttribute;
	protected String dependencyAttribute;
	protected String dependecyOperatorAttribute;
	protected String dependecyValueAttribute;
	
	protected class Item {
		String uid;
		String label;
		String type;
		String answerString;
		Boolean answerBool;
		String answerFileUid;
		float labelWidth;
		float answerWidth;
		String detail;
		String cat;
		String catOrder;
		String order;
		String depOrder;
		String depRorValue;
		String depOp;
		String depVal;
	}

	public DynamicForm(ReportManager rm, ReportConfig rc, DataMap c) throws RedbackException {
		super(rm, rc, c);
		width = c.containsKey("width") ? c.getNumber("width").floatValue() : -1;
		orderAttribute = config.getString("orderattribute");
		valueAttribute = config.getString("valueattribute");
		optionsAttribute = config.getString("optionsattribute");
		typeAttribute = config.getString("typeattribute");
		catOrderAttribute = config.getString("categoryorderattribute");
		catAttribute = config.getString("categoryattribute");
		labelAttribute = config.getString("labelattribute");
		detailAttribute = config.getString("detailattribute");
		dependencyAttribute = config.getString("dependencyattribute");
		dependecyOperatorAttribute = config.getString("dependencyoperatorattribute");
		dependecyValueAttribute = config.getString("dependencyvalueattribute");
	}
	
	protected void cutLinesToColumn(Box col, float fontSize, Color color, String longLine, float width) throws IOException {
		if(longLine != null) {
			String[] lines = longLine.split("\\n");
			for(int i = 0; i < lines.length; i++) {
				String line = lines[i];
				List<String> sublines = cutToLines(fontSize, line, width);
				for(String subline : sublines) {
					Box lineBox = Box.Text(subline, font, fontSize);
					if(color != null) lineBox.color = color;
					col.addChild(lineBox);
				}		
			}			
		}
	}
	

	public Box produce(Map<String, Object> context) throws IOException, RedbackException {
		Session session = (Session)context.get("session");

		List<RedbackObjectRemote> rors = ((List<?>)context.get("dataset"))
				.stream()
				.map(o -> (RedbackObjectRemote)o)
				.collect(Collectors.toList());
		Collections.sort(rors, new Comparator<RedbackObjectRemote>() {
			public int compare(RedbackObjectRemote o1, RedbackObjectRemote o2) {
				try {
					return o1.getString(orderAttribute).compareTo(o2.getString(orderAttribute));
				} catch(RedbackException e) {
					return 0;
				}
			}
		});	

		float fontSize = fontSize(context);
		float maxLabelWidth = 0;
		float maxAnswerWidth = 0;
		List<Item> items = new ArrayList<Item>();
		for(RedbackObjectRemote ror: rors) {
			Item item = new Item();
			item.uid = ror.getUid();
			item.label = ror.getString(labelAttribute);
			item.labelWidth = getStringWidth(fontSize, item.label);
			item.type = ror.getString(typeAttribute);
			if(item.type.equals("infoonly")) {
				item.labelWidth = 0;
			} else if(item.type.equals("string") || item.type.equals("address") || item.type.equals("phone") || item.type.equals("email")) {
				item.answerString = ror.getString(valueAttribute);
				item.answerWidth = getStringWidth(fontSize, item.answerString);
			} else if(item.type.equals("textarea")) {
				item.answerString = ror.getString(valueAttribute);
				if(item.answerString != null) {
					String[] lines = item.answerString.split("\\n");
					for(int i = 0; i < lines.length; i++) {
						float thisLineWidth = getStringWidth(fontSize, lines[i]);
						if(thisLineWidth > item.answerWidth) item.answerWidth = thisLineWidth;
					}
				}				
			} else if(item.type.equals("number")) {			
				if(ror.getObject(valueAttribute) != null)
					item.answerString = ror.getString(valueAttribute);
				else
					item.answerString = "";
				item.answerWidth = getStringWidth(fontSize, item.answerString);
			} else if(item.type.equals("date")) {
				Date value = ror.getDate(valueAttribute);
				if(value != null) {
					ZonedDateTime zdt = ZonedDateTime.ofInstant(((Date)value).toInstant(), session.getTimezone() != null ? ZoneId.of(session.getTimezone()) : ZoneId.systemDefault());
					item.answerString = zdt.format(DateTimeFormatter.ofPattern("d MMM yy HH:mm"));
					item.answerWidth = getStringWidth(fontSize, item.answerString);
				}
			} else if(item.type.equals("choice")) {
				String value = ror.getString(valueAttribute);
				if(value != null) {
					item.answerString = value;
					if(ror.hasAttribute(optionsAttribute)) {
						DataList options = (DataList)ror.getObject(optionsAttribute);
						for(int i = 0; i < options.size(); i++) {
							DataMap option = options.getObject(i);
							if(value.equals(option.getString("value"))) {
								item.answerString = option.getString("display");
							}
						}					
					}
					item.answerWidth = getStringWidth(fontSize, item.answerString);
				}
			} else if(item.type.equals("checkbox")) {
				item.answerBool = ror.getBool(valueAttribute);
				item.answerWidth = 12;
			} else if(item.type.equals("signature")) {
				Object de = ror.getObject(valueAttribute);
				if(de != null && de instanceof DataMap) {
					item.answerFileUid = ((DataMap)de).getString("fileuid");
				}
				item.answerWidth = 100;
			} else if(item.type.equals("files") || item.type.equals("photos")) {
				item.answerWidth = 0;
			}
			item.detail = ror.getString(detailAttribute);
			item.cat = catAttribute != null ? ror.getString(catAttribute) : null;
			if(item.cat == null) item.cat = "";
			item.catOrder = catOrderAttribute != null ? ror.getString(catOrderAttribute) : null;
			if(item.catOrder == null) item.catOrder = "";
			item.order = orderAttribute != null ? ror.getString(orderAttribute) : "";
			item.depOrder = dependencyAttribute != null ? ror.getString(dependencyAttribute) : null;
			if(item.depOrder != null) {
				item.depRorValue = ror.getString(dependencyAttribute + "." + valueAttribute);
				item.depOp = ror.getString(dependecyOperatorAttribute);
				item.depVal = ror.getString(dependecyValueAttribute);				
			}
			items.add(item);
			if(item.labelWidth > maxLabelWidth) maxLabelWidth = item.labelWidth;
			if(item.answerWidth > maxAnswerWidth) maxAnswerWidth = item.answerWidth;
		}
		float marginWidth = 5;
		float totalWidth = 490 - (2 * marginWidth);
		float minLabelWidth = 180;
		float minAnswerWidth = 12;
		float labelWidth = 0;
		float answerWidth = 0;
		if(maxLabelWidth + maxAnswerWidth > totalWidth) {
			float remLabelWidth = Math.max(0, maxLabelWidth - minLabelWidth);
			float remAnswerWidth = Math.max(0, maxAnswerWidth - minAnswerWidth);
			labelWidth = minLabelWidth + ((remLabelWidth / (remLabelWidth + remAnswerWidth)) * (totalWidth - minLabelWidth - minAnswerWidth));
			answerWidth = minAnswerWidth + ((remAnswerWidth / (remLabelWidth + remAnswerWidth)) * (totalWidth - minLabelWidth - minAnswerWidth));
		} else {
			labelWidth = Math.max(maxLabelWidth, minLabelWidth);
			answerWidth = Math.max(maxAnswerWidth, minAnswerWidth);
		}

		Box container = Box.VContainer(true);
		container.breakBefore = pagebreak;
		String lastCatOrder = "";

		for(Item item: items) {
			if(!item.catOrder.equals(lastCatOrder) && !item.cat.equals("")) {
				Box catRb = Box.VContainer(false);
				catRb.color = Color.decode("#0277bc");
				if(width > -1)
					catRb.width = width;
				Box catTextRb = Box.Text(item.cat, font, fontSize);
				catTextRb.height = 12;
				catTextRb.color = Color.WHITE;
				catRb.addChild(catTextRb);
				catRb.height += 4;
				catTextRb.x += marginWidth;
				catTextRb.y += 3;
				container.addChild(catRb);
				container.addChild(Box.Empty(10, 5));
				lastCatOrder = item.catOrder;
			}
			boolean showItem = true;
			if(item.depOrder != null) {
				showItem = false;
				if(item.depRorValue != null) {
					if(item.depOp != null && item.depVal != null) {
						if(item.depOp.equals("eq")) {
							if(item.depVal.equals(item.depRorValue)) {
								showItem = true;
							}
						}
					}
				}
			}
			if(showItem) {
				Box formItemRb = Box.VContainer(false);
				Box labelAnswerRowRb = Box.HContainer(false);
				labelAnswerRowRb.addChild(Box.Empty(marginWidth, 5));
				Box labelColRb = Box.VContainer(false);
				float thisLabelwidth = item.type.equals("infoonly") ? totalWidth : labelWidth;
				cutLinesToColumn(labelColRb, fontSize, Color.decode("#666666"), item.label, thisLabelwidth);
				labelColRb.width = thisLabelwidth;
				if(item.detail != null) {
					cutLinesToColumn(labelColRb, fontSize - 4, Color.lightGray, item.detail, labelWidth);
				}
				labelAnswerRowRb.addChild(labelColRb);
				labelAnswerRowRb.addChild(Box.Empty(20, 5));

				
				if(item.answerString != null) {
					Box col = Box.VContainer(false);
					cutLinesToColumn(col, fontSize, null, item.answerString, answerWidth);
					labelAnswerRowRb.addChild(col);
				} else if(item.answerBool != null) {
					Box rb = Box.Checkbox(item.answerBool, 12, 12);
					labelAnswerRowRb.addChild(rb);
				} else if(item.answerFileUid != null) {
					FileClient fc = reportManager.getFileClient();
					RedbackFile file = fc.getFile(session, item.answerFileUid);
					Box rb = reportBoxFromFile(file, (int)item.answerWidth);
					labelAnswerRowRb.addChild(rb);
				}

				formItemRb.addChild(labelAnswerRowRb);

				if(item.type.equals("files") || item.type.equals("photos")) {
					FileClient fc = reportManager.getFileClient();
					List<RedbackFile> files = fc.listFilesFor(session, "formitem", item.uid);
					Box row = Box.HContainer(false);
					row.addChild(Box.Empty(marginWidth + 3, 5));
					for(RedbackFile file: files) {
						Box rb = reportBoxFromFile(file, 150);
						if(rb != null) {
							if(width > -1 && row.width + rb.width > width) {
								formItemRb.addChild(row);
								formItemRb.addChild(Box.Empty(5, 5));
								row = Box.HContainer(false);
								row.addChild(Box.Empty(marginWidth + 3, 5));
							}
							row.addChild(rb);
							row.addChild(Box.Empty(5, 5));
						}
					}
					formItemRb.canBreak = true;
					formItemRb.addChild(row);
				}
				
				formItemRb.height += 12;
				container.addChild(formItemRb);				
			}

		}
		return container;	
	}
	
	protected Box reportBoxFromFile(RedbackFile file, int maxSize) throws RedbackException {
		Box ret = null;
		try {
			int ori = ImageUtils.getOrientation(file.bytes);
			BufferedImage originalImage = ImageUtils.getImage(file.bytes);
			int origWidth = ori < 5 ? originalImage.getWidth() : originalImage.getHeight();
			int origHeight = ori < 5 ? originalImage.getHeight() : originalImage.getWidth();
			int newWidth = origWidth > origHeight ? maxSize * 2 : -1;
			int newHeight = origHeight >= origWidth ? maxSize * 2 : -1;
			BufferedImage img = ImageUtils.getImage(originalImage, newWidth, newHeight, ori);
			ret = Box.Image(ImageUtils.getBytes(img, "png"), img.getWidth() / 2, img.getHeight() / 2);
		} catch(Exception e) {
			Logger.warning("Error getting ReportBox image", e);
			ret = Box.Empty(50, 50);
		}
		return ret;	
	}


}
