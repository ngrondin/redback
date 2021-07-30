package io.redback.managers.reportmanager.pdf;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import io.firebus.utils.DataEntity;
import io.firebus.utils.DataList;
import io.firebus.utils.DataMap;
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
				if(catOrderAttribute != null && orderAttribute != null)
					return (o1.getNumber(catOrderAttribute).intValue() * rors.size() + o1.getNumber(orderAttribute).intValue()) - (o2.getNumber(catOrderAttribute).intValue() * rors.size() + o2.getNumber(orderAttribute).intValue()); 
				else if(orderAttribute != null)
					return o1.getNumber(orderAttribute).intValue() - o2.getNumber(orderAttribute).intValue();
				else
					return 0;
			}
		});	
		Map<String, RedbackObjectRemote> orderMap = new HashMap<String, RedbackObjectRemote>();

		Box container = Box.VContainer(true);
		String lastCatOrder = "";
		for(RedbackObjectRemote ror: rors) {
			String cat = ror.getString(catAttribute);
			String catOrder = ror.getString(catOrderAttribute);
			String order = ror.getString(orderAttribute);
			orderMap.put(order, ror);
			if(cat == null) cat = "";
			if(catOrder == null) catOrder = "";
			if(!catOrder.equals(lastCatOrder)) {
				Box catRb = Box.VContainer(false);
				catRb.color = Color.decode("#3f51b5");
				if(width > -1)
					catRb.width = width;
				Box catTextRb = Box.Text(cat, font, fontSize);
				catTextRb.height = 12;
				catTextRb.color = Color.WHITE;
				catRb.addChild(catTextRb);
				catRb.height += 4;
				catTextRb.x += 5;
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
				float marginWidth = 10;
				labelAnswerRowRb.addChild(Box.Empty(marginWidth, 5));
				float labelWidth = 0;
				if(label != null) {
					Box rb = Box.Text(label, font, fontSize);
					rb.color = Color.decode("#666666");
					rb.fontSize = 11f;
					labelWidth = rb.width;
					labelAnswerRowRb.addChild(rb);
					labelAnswerRowRb.addChild(Box.Empty(20, 5));
				}
				float answerMaxWidth = width > -1 ? width - labelWidth - (2 * marginWidth) - 20 : -1;
				
				if(type.equals("string") || type.equals("address") || type.equals("phone") || type.equals("email")) {
					String value = ror.getString(valueAttribute);
					if(value != null) {
						String valueStr = value.toString();
						Box rb = Box.Text(valueStr, font, fontSize);
						addChildAlignedLeft(labelAnswerRowRb, rb, answerMaxWidth);
					}
					
				} else if(type.equals("textarea")) {
					String value = ror.getString(valueAttribute);
					if(value != null) {
						Box col = Box.VContainer(false);
						String[] lines = value.split("\\n");
						for(int i = 0; i < lines.length; i++) {
							String line = lines[i];
							List<String> sublines = cutToLines(line, answerMaxWidth > -1 ? answerMaxWidth : 200);
							for(String subline : sublines) {
								col.addChild(Box.Text(subline, font, fontSize));
							}		
							
						}
						addChildAlignedLeft(labelAnswerRowRb, col, answerMaxWidth);
					}
					
				} else if(type.equals("number")) {
					Number value = ror.getNumber(valueAttribute);
					if(value != null) {
						String valueStr = value.toString();
						if(Math.floor(value.doubleValue()) == value.doubleValue()) {
							valueStr = "" + value.intValue();
						}
						Box rb = Box.Text(valueStr, font, fontSize);
						addChildAlignedLeft(labelAnswerRowRb, rb, answerMaxWidth);
					}
					
				} else if(type.equals("date")) {
					Date value = ror.getDate(valueAttribute);
					if(value != null) {
						DateFormat formatter = DateFormat.getDateTimeInstance();
						String valueStr = formatter.format(value);
						Box rb = Box.Text(valueStr, font, fontSize);
						addChildAlignedLeft(labelAnswerRowRb, rb, answerMaxWidth);
					}
					
				} else if(type.equals("choice")) {
					String value = ror.getString(valueAttribute);
					if(value != null) {
						String display = value;
						if(ror.get(optionsAttribute) != null) {
							DataList options = (DataList)ror.get(optionsAttribute);
							for(int i = 0; i < options.size(); i++) {
								DataMap option = options.getObject(i);
								if(option.getString("value").equals(value)) {
									display = option.getString("display");
								}
							}					
						}
						Box rb = Box.Text(display, font, fontSize);
						addChildAlignedLeft(labelAnswerRowRb, rb, answerMaxWidth);
					}
					
				} else if(type.equals("checkbox")) {
					boolean value = ror.getBool(valueAttribute);
					Box rb = Box.Checkbox(value, 12, 12);
					addChildAlignedLeft(labelAnswerRowRb, rb, answerMaxWidth);

				} else if(type.equals("signature")) {
					DataEntity de = ror.get(valueAttribute);
					if(de != null && de instanceof DataMap) {
						FileClient fc = reportManager.getFileClient();
						RedbackFile file = fc.getFile(session, ((DataMap)de).getString("fileuid"));
						Box rb = reportBoxFromFile(file, 50);
						addChildAlignedLeft(labelAnswerRowRb, rb, answerMaxWidth);
					}

				}
				formItemRb.addChild(labelAnswerRowRb);
				String detail = ror.getString(detailAttribute);
				if(detail != null) {
					Box detailRb = Box.Text(detail, font, fontSize - 4);
					detailRb.color = Color.lightGray;
					formItemRb.addChild(detailRb);
				}
				if(type.equals("files")) {
					FileClient fc = reportManager.getFileClient();
					List<RedbackFile> files = fc.listFilesFor(session, "formitem", ror.getUid());
					for(RedbackFile file: files) {
						Box rb = reportBoxFromFile(file, 250);
						if(rb != null) {
							float margin = width > -1 ? (width - rb.width) / 2f : 0;
							Box row = Box.HContainer(false);
							row.addChild(Box.Empty(margin, 5));
							row.addChild(rb);
							formItemRb.addChild(row);
						}
					}
					formItemRb.canBreak = true;
				}				
				formItemRb.height += 12;
				container.addChild(formItemRb);				
			}

		}
		return container;	
	}
	
	protected Box reportBoxFromFile(RedbackFile file, int newHeight) throws RedbackException {
		Box ret = null;
			try {
			int ori = ImageUtils.getOrientation(file.bytes);
			BufferedImage img = ImageUtils.getImage(file.bytes, -1, newHeight, ori);
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
