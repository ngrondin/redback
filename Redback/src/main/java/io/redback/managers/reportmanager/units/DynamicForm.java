package io.redback.managers.reportmanager.units;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import io.firebus.utils.DataEntity;
import io.firebus.utils.DataList;
import io.firebus.utils.DataMap;
import io.redback.RedbackException;
import io.redback.client.FileClient;
import io.redback.client.RedbackObjectRemote;
import io.redback.managers.reportmanager.ReportBox;
import io.redback.managers.reportmanager.ReportConfig;
import io.redback.managers.reportmanager.ReportManager;
import io.redback.managers.reportmanager.ReportUnit;
import io.redback.security.Session;
import io.redback.utils.RedbackFile;

public class DynamicForm extends ReportUnit {
	protected PDFont font;
	protected float fontSize;
	protected float width;
	protected String orderAttribute;
	protected String valueAttribute;
	protected String optionsAttribute;
	protected String typeAttribute;
	protected String catOrderAttribute;
	protected String catAttribute;
	protected String labelAttribute;
	protected String detailAttribute;

	public DynamicForm(ReportManager rm, ReportConfig rc, DataMap c) throws RedbackException {
		super(rm, rc, c);
		font = PDType1Font.HELVETICA;
		fontSize = 12f;
		width = c.containsKey("width") ? c.getNumber("width").floatValue() : -1;
		orderAttribute = config.getString("orderattribute");
		valueAttribute = config.getString("valueattribute");
		optionsAttribute = config.getString("optionsattribute");
		typeAttribute = config.getString("typeattribute");
		catOrderAttribute = config.getString("categoryorderattribute");
		catAttribute = config.getString("categoryattribute");
		labelAttribute = config.getString("labelattribute");
		detailAttribute = config.getString("detailattribute");
	}

	public ReportBox produce(Map<String, Object> context) throws IOException, RedbackException {
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

		ReportBox container = ReportBox.VContainer(true);
		String lastCat = "";
		for(RedbackObjectRemote ror: rors) {
			String cat = ror.getString(catAttribute);
			if(cat == null) cat = "";
			if(!cat.equals(lastCat)) {
				ReportBox catRb = ReportBox.VContainer(false);
				catRb.color = Color.decode("#3f51b5");
				if(width > -1)
					catRb.width = width;
				ReportBox catTextRb = ReportBox.Text(cat, font, fontSize);
				catTextRb.color = Color.WHITE;
				catRb.addChild(catTextRb);
				catRb.height += 4;
				catTextRb.x += 5;
				container.addChild(catRb);
				lastCat = cat;
			}
			ReportBox formItemRb = ReportBox.VContainer(false);
			String type = ror.getString(typeAttribute);
			String label = ror.getString(labelAttribute);
			ReportBox labelAnswerRowRb = ReportBox.HContainer(false);
			float labelWidth = 0;
			if(label != null) {
				ReportBox rb = ReportBox.Text(label, font, fontSize);
				rb.color = Color.decode("#666666");
				rb.fontSize = 11f;
				labelWidth = rb.width;
				labelAnswerRowRb.addChild(rb);
			}
			formItemRb.addChild(labelAnswerRowRb);
			String detail = ror.getString(detailAttribute);
			if(detail != null) {
				ReportBox detailRb = ReportBox.Text(detail, font, fontSize - 4);
				detailRb.color = Color.lightGray;
				formItemRb.addChild(detailRb);
			}
			
			if(type.equals("string") || type.equals("textarea")) {
				String value = ror.getString(valueAttribute);
				if(value != null) {
					ReportBox row = ReportBox.HContainer(false);
					ReportBox col = ReportBox.VContainer(false);
					col.addChild(ReportBox.Empty(10, 3));
					String[] lines = value.split("\\n");
					for(int i = 0; i < lines.length; i++) {
						String line = lines[i];
						List<String> sublines = cutToLines(line, font, fontSize, width > -1 ? width : 200);
						for(String subline : sublines) {
							col.addChild(ReportBox.Text(subline, font, fontSize));
						}		
						
					}
					col.addChild(ReportBox.Empty(10, 10));
					row.addChild(col);
					formItemRb.addChild(row);
				}	
				
			} else if(type.equals("number")) {
				Number value = ror.getNumber(valueAttribute);
				if(value != null) {
					String valueStr = value.toString();
					ReportBox rb = ReportBox.Text(valueStr, font, fontSize);
					if(width > -1) {
						labelAnswerRowRb.addChild(ReportBox.Empty(width - labelWidth - rb.width, fontSize));
					}
					labelAnswerRowRb.addChild(rb);
				}
				
			} else if(type.equals("date")) {
				Date value = ror.getDate(valueAttribute);
				if(value != null) {
					DateFormat formatter = DateFormat.getDateTimeInstance();
					String valueStr = formatter.format(value);
					ReportBox rb = ReportBox.Text(valueStr, font, fontSize);
					if(width > -1) {
						labelAnswerRowRb.addChild(ReportBox.Empty(width - labelWidth - rb.width, fontSize));
					}
					labelAnswerRowRb.addChild(rb);
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
					ReportBox rb = ReportBox.Text(display, font, fontSize);
					if(width > -1) {
						labelAnswerRowRb.addChild(ReportBox.Empty(width - labelWidth - rb.width, fontSize));
					}
					labelAnswerRowRb.addChild(rb);
				}
				
			} else if(type.equals("checkbox")) {
				boolean value = ror.getBool(valueAttribute);
				ReportBox rb = ReportBox.Checkbox(value, 12, 12);
				if(width > -1) {
					labelAnswerRowRb.addChild(ReportBox.Empty(width - labelWidth - 12, 12));
				}
				labelAnswerRowRb.addChild(rb);

			} else if(type.equals("files")) {
				FileClient fc = reportManager.getFileClient();
				List<RedbackFile> files = fc.listFilesFor(session, "formitem", ror.getUid());
				for(RedbackFile file: files) {
					ReportBox rb = reportBoxFromFile(file);
					if(rb != null) {
						formItemRb.addChild(ReportBox.Empty(5, 5));
						formItemRb.addChild(rb);
						formItemRb.canBreak = true;
					}
				}
				
			} else if(type.equals("signature")) {
				DataEntity de = ror.get(valueAttribute);
				if(de != null && de instanceof DataMap) {
					FileClient fc = reportManager.getFileClient();
					RedbackFile file = fc.getFile(session, ((DataMap)de).getString("fileuid"));
					ReportBox rb = reportBoxFromFile(file);
					if(rb != null) {
						formItemRb.addChild(ReportBox.Empty(5, 5));
						formItemRb.addChild(rb);
					}
				}
			}
			formItemRb.height += 12;
			container.addChild(formItemRb);
		}
		return container;	
	}
	
	protected ReportBox reportBoxFromFile(RedbackFile file) throws IOException {
		ReportBox ret = null;
		BufferedImage orig = ImageIO.read(new ByteArrayInputStream(file.bytes));
		if(orig.getHeight() > 0) {
			int newHeight = 300;
			int newWidth = (int)((float)orig.getWidth() / ((float)orig.getHeight() / (float)newHeight));
			BufferedImage img = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
			Graphics2D gc = img.createGraphics();
			gc.setColor(Color.WHITE);
			gc.fillRect(0, 0, newWidth, newHeight);
			gc.drawImage(orig.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH),0,0,null);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(img, "png", baos);
			ret = ReportBox.Image(baos.toByteArray(), 400, 300);
		}					
		return ret;	
	}

}
