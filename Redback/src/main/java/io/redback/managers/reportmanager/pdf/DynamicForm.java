package io.redback.managers.reportmanager.pdf;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.firebus.data.DataList;
import io.firebus.data.DataMap;
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

	public DynamicForm(ReportManager rm, ReportConfig rc, DataMap c) throws RedbackException {
		super(rm, rc, c);
		//font = PDType1Font.HELVETICA;
		//fontSize = 12f;
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
					/*if(catOrderAttribute != null && orderAttribute != null)
						return (o1.getNumber(catOrderAttribute).intValue() * rors.size() + o1.getNumber(orderAttribute).intValue()) - (o2.getNumber(catOrderAttribute).intValue() * rors.size() + o2.getNumber(orderAttribute).intValue()); 
					else if(orderAttribute != null)
						return o1.getNumber(orderAttribute).intValue() - o2.getNumber(orderAttribute).intValue();
					else
						return 0;*/
				} catch(RedbackException e) {
					return 0;
				}
			}
		});	
		Map<String, RedbackObjectRemote> orderMap = new HashMap<String, RedbackObjectRemote>();

		float marginWidth = 5;
		Box container = Box.VContainer(true);
		container.breakBefore = pagebreak;
		String lastCatOrder = "";
		float fontSize = fontSize(context);
		float maxLabelWidth = 0;
		for(RedbackObjectRemote ror: rors) {
			String label = ror.getString(labelAttribute);
			float labelWidth = font.getStringWidth(label) / 1000f * fontSize;
			if(labelWidth > maxLabelWidth) maxLabelWidth = labelWidth;
		}
		if(maxLabelWidth < 150) maxLabelWidth = 200;
		float maxAnswerWidth = width > -1 ? width - maxLabelWidth - 20 - (2 * marginWidth) : -1;
		
		for(RedbackObjectRemote ror: rors) {
			String cat = catAttribute != null ? ror.getString(catAttribute) : null;
			String catOrder = catOrderAttribute != null ? ror.getString(catOrderAttribute) : null;
			String order = orderAttribute != null ? ror.getString(orderAttribute) : "";
			orderMap.put(order, ror);
			if(cat == null) cat = "";
			if(catOrder == null) catOrder = "";
			if(!catOrder.equals(lastCatOrder) && !cat.equals("")) {
				Box catRb = Box.VContainer(false);
				catRb.color = Color.decode("#0277bc");
				if(width > -1)
					catRb.width = width;
				Box catTextRb = Box.Text(cat, font, fontSize);
				catTextRb.height = 12;
				catTextRb.color = Color.WHITE;
				catRb.addChild(catTextRb);
				catRb.height += 4;
				catTextRb.x += marginWidth;
				catTextRb.y += 3;
				container.addChild(catRb);
				container.addChild(Box.Empty(10, 5));
				lastCatOrder = catOrder;
			}
			boolean showItem = true;
			String depOrder = dependencyAttribute != null ? ror.getString(dependencyAttribute) : null;
			if(depOrder != null) {
				showItem = false;
				String depRorValue = ror.getString(dependencyAttribute + "." + valueAttribute);
				if(depRorValue != null) {
					String depOp = ror.getString(dependecyOperatorAttribute);
					String depVal = ror.getString(dependecyValueAttribute);
					if(depOp != null && depVal != null) {
						if(depOp.equals("eq")) {
							if(depVal.equals(depRorValue)) {
								showItem = true;
							}
						}
					}
				}
			}
			if(showItem) {
				Box formItemRb = Box.VContainer(false);
				String type = ror.getString(typeAttribute);
				String label = ror.getString(labelAttribute);
				Box labelAnswerRowRb = Box.HContainer(false);
				labelAnswerRowRb.addChild(Box.Empty(marginWidth, 5));
				Box labelRb = Box.Text(label, font, fontSize);
				labelRb.color = Color.decode("#666666");
				labelRb.width = maxLabelWidth;
				labelAnswerRowRb.addChild(labelRb);
				labelAnswerRowRb.addChild(Box.Empty(20, 5));

				if(type.equals("string") || type.equals("address") || type.equals("phone") || type.equals("email")) {
					String value = ror.getString(valueAttribute);
					if(value != null) {
						String valueStr = value.toString();
						Box rb = Box.Text(valueStr, font, fontSize);
						labelAnswerRowRb.addChild(rb);
						//addChildAlignedLeft(labelAnswerRowRb, rb, answerMaxWidth);
					}
					
				} else if(type.equals("textarea")) {
					String value = ror.getString(valueAttribute);
					if(value != null) {
						Box col = Box.VContainer(false);
						String[] lines = value.split("\\n");
						for(int i = 0; i < lines.length; i++) {
							String line = lines[i];
							List<String> sublines = cutToLines(fontSize, line, maxAnswerWidth > -1 ? maxAnswerWidth : 200);
							for(String subline : sublines) {
								col.addChild(Box.Text(subline, font, fontSize));
							}		
							
						}
						labelAnswerRowRb.addChild(col);
						//addChildAlignedLeft(labelAnswerRowRb, col, answerMaxWidth);
					}
					
				} else if(type.equals("number")) {
					Number value = ror.getNumber(valueAttribute);
					if(value != null) {
						String valueStr = value.toString();
						if(Math.floor(value.doubleValue()) == value.doubleValue()) {
							valueStr = "" + value.intValue();
						}
						Box rb = Box.Text(valueStr, font, fontSize);
						labelAnswerRowRb.addChild(rb);
						//addChildAlignedLeft(labelAnswerRowRb, rb, answerMaxWidth);
					}
					
				} else if(type.equals("date")) {
					Date value = ror.getDate(valueAttribute);
					if(value != null) {
						DateFormat formatter = DateFormat.getDateTimeInstance();
						String valueStr = formatter.format(value);
						Box rb = Box.Text(valueStr, font, fontSize);
						labelAnswerRowRb.addChild(rb);
						//addChildAlignedLeft(labelAnswerRowRb, rb, answerMaxWidth);
					}
					
				} else if(type.equals("choice")) {
					String value = ror.getString(valueAttribute);
					if(value != null) {
						String display = value;
						if(ror.hasAttribute(optionsAttribute)) {
							DataList options = (DataList)ror.getObject(optionsAttribute);
							for(int i = 0; i < options.size(); i++) {
								DataMap option = options.getObject(i);
								if(option.getString("value").equals(value)) {
									display = option.getString("display");
								}
							}					
						}
						Box rb = Box.Text(display, font, fontSize);
						labelAnswerRowRb.addChild(rb);
						//addChildAlignedLeft(labelAnswerRowRb, rb, answerMaxWidth);
					}
					
				} else if(type.equals("checkbox")) {
					boolean value = ror.getBool(valueAttribute);
					Box rb = Box.Checkbox(value, 12, 12);
					labelAnswerRowRb.addChild(rb);
					//addChildAlignedLeft(labelAnswerRowRb, rb, answerMaxWidth);

				} else if(type.equals("signature")) {
					Object de = ror.getObject(valueAttribute);
					if(de != null && de instanceof DataMap) {
						FileClient fc = reportManager.getFileClient();
						RedbackFile file = fc.getFile(session, ((DataMap)de).getString("fileuid"));
						Box rb = reportBoxFromFile(file, 50);
						labelAnswerRowRb.addChild(rb);
						//addChildAlignedLeft(labelAnswerRowRb, rb, answerMaxWidth);
					}

				}
				formItemRb.addChild(labelAnswerRowRb);
				String detail = ror.getString(detailAttribute);
				if(detail != null) {
					Box row = Box.HContainer(false);
					row.addChild(Box.Empty(marginWidth + 1, 5));
					Box col = Box.VContainer(false);
					String[] lines = detail.split("\\n");
					for(int i = 0; i < lines.length; i++) {
						String line = lines[i];
						List<String> sublines = cutToLines(fontSize - 4, line, maxLabelWidth > -1 ? maxLabelWidth : 200);
						for(String subline : sublines) {
							Box detailLine = Box.Text(subline, font, fontSize - 4);
							detailLine.color = Color.lightGray;
							col.addChild(detailLine);
						}		
					}
					row.addChild(col);
					formItemRb.addChild(row);
				}
				if(type.equals("files") || type.equals("photos")) {
					FileClient fc = reportManager.getFileClient();
					List<RedbackFile> files = fc.listFilesFor(session, "formitem", ror.getUid());
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
			int newWidth = origWidth > origHeight ? maxSize : -1;
			int newHeight = origHeight >= origWidth ? maxSize : -1;
			BufferedImage img = ImageUtils.getImage(originalImage, newWidth, newHeight, ori);
			ret = Box.Image(ImageUtils.getBytes(img, "png"), img.getWidth(), img.getHeight());
		} catch(Exception e) {
			throw new RedbackException("Error getting ReportBox image", e);
		}
		return ret;	
	}
	
	protected void addChildAlignedLeft(Box hcontainer, Box child, float maxRemainingWidth) {
		if(maxRemainingWidth > -1) {
			float space = Math.max(0, maxRemainingWidth - child.width);
			hcontainer.addChild(Box.Empty(space, 5));
		}
		if(child.width > maxRemainingWidth) {
			child.width = maxRemainingWidth;
		}
		hcontainer.addChild(child);
	}

}
