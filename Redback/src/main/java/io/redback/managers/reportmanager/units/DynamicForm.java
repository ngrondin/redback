package io.redback.managers.reportmanager.units;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import io.firebus.utils.DataList;
import io.firebus.utils.DataMap;
import io.redback.RedbackException;
import io.redback.client.RedbackObjectRemote;
import io.redback.managers.reportmanager.ReportBox;
import io.redback.managers.reportmanager.ReportConfig;
import io.redback.managers.reportmanager.ReportManager;
import io.redback.managers.reportmanager.ReportUnit;

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
		font = PDType1Font.TIMES_ROMAN;
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
				ReportBox catRb = ReportBox.Text(cat, font, fontSize);
				float catWidth = (width == -1 ? catRb.width : width);
				container.addChild(ReportBox.Empty(catWidth, 5));
				container.addChild(catRb);
				container.addChild(ReportBox.HLine(catWidth, 10));
				lastCat = cat;
			}
			ReportBox item = ReportBox.VContainer(false);
			String type = ror.getString(typeAttribute);
			String label = ror.getString(labelAttribute);
			ReportBox labelAnswer = ReportBox.HContainer(false);
			float labelWidth = 0;
			if(label != null) {
				ReportBox rb = ReportBox.Text(label, font, fontSize);
				labelWidth = rb.width;
				labelAnswer.addChild(rb);
			}
			item.addChild(labelAnswer);
			String detail = ror.getString(detailAttribute);
			if(detail != null) {
				item.addChild(ReportBox.Text(detail, font, fontSize - 2));
			}
			
			if(type.equals("string") || type.equals("textarea")) {
				String value = ror.getString(valueAttribute);
				if(value != null) {
					ReportBox row = ReportBox.HContainer(false);
					row.addChild(ReportBox.Empty(20, 10));
					ReportBox col = ReportBox.VContainer(false);
					col.addChild(ReportBox.Empty(10, 10));
					List<String> lines = cutToLines(value, font, fontSize, width > -1 ? width : 200);
					for(String line : lines) {
						col.addChild(ReportBox.Text(line, font, fontSize));
					}		
					row.addChild(col);
					item.addChild(row);
				}	
				
			} else if(type.equals("number")) {
				String value = ror.getNumber(valueAttribute).toString();
				ReportBox rb = ReportBox.Text(value, font, fontSize);
				if(width > -1) {
					labelAnswer.addChild(ReportBox.Empty(width - labelWidth - rb.width, fontSize));
				}
				labelAnswer.addChild(rb);
				
			} else if(type.equals("choice")) {
				String value = ror.getString(valueAttribute);
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
					labelAnswer.addChild(ReportBox.Empty(width - labelWidth - rb.width, fontSize));
				}
				labelAnswer.addChild(rb);
				
			} else if(type.equals("checkbox")) {
				boolean value = ror.getBool(valueAttribute);
				ReportBox rb = ReportBox.Checkbox(value, 12, 12);
				if(width > -1) {
					labelAnswer.addChild(ReportBox.Empty(width - labelWidth - 12, 12));
				}
				labelAnswer.addChild(rb);

			} else if(type.equals("files")) {
				
			} else if(type.equals("signature")) {
				
			}
			item.height += 10;
			container.addChild(item);
		}
		return container;	
	}

}
