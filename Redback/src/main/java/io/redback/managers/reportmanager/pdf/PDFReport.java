package io.redback.managers.reportmanager.pdf;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import io.firebus.data.DataList;
import io.firebus.data.DataMap;
import io.redback.client.js.ObjectClientJSWrapper;
import io.redback.exceptions.RedbackException;
import io.redback.managers.reportmanager.Report;
import io.redback.managers.reportmanager.ReportConfig;
import io.redback.managers.reportmanager.ReportManager;
import io.redback.security.Session;
import io.redback.utils.ReportFilter;

public class PDFReport extends Report {
	protected PDDocument document;
	protected List<Unit> rootUnits;
	protected List<Unit> headerUnits;	
	protected List<Unit> footerUnits;
	protected float pageLongSide = 792;
	protected float pageShortSide = 612;
	protected float pageHeight = 792;
	protected float pageWidth = 612;
	protected float marginTop = 50;
	protected float marginBottom = 50;
	protected float marginLeft = 50;
	protected float marginRight = 50;
		
	public PDFReport(Session s, ReportManager rm, ReportConfig rc) throws RedbackException {
		super(s, rm, rc);
		try {
			document = new PDDocument();
			InputStream is = getClass().getResourceAsStream("/io/redback/fonts/calibri.ttf");
			if(is != null)
				PDType0Font.load(document, is);
			DataList content = rc.getData().getList("content");
			rootUnits = new ArrayList<Unit>();
			for(int i = 0; i < content.size(); i++) {
				rootUnits.add(Unit.fromConfig(reportManager, rc, content.getObject(i)));
			}
			DataList header = rc.getData().getList("header");
			if(header != null) {
				headerUnits = new ArrayList<Unit>();
				for(int i = 0; i < header.size(); i++) {
					headerUnits.add(Unit.fromConfig(reportManager, rc, header.getObject(i)));
				}		
			}			
			DataList footer = rc.getData().getList("footer");
			if(footer != null) {
				footerUnits = new ArrayList<Unit>();
				for(int i = 0; i < footer.size(); i++) {
					footerUnits.add(Unit.fromConfig(reportManager, rc, footer.getObject(i)));
				}		
			}
			DataMap layout = rc.getData().getObject("layout");
			if(layout != null) {
				if(layout.containsKey("margin")) {
					marginTop = marginBottom = marginLeft = marginRight = layout.getNumber("margin").floatValue();
				} 
				if(layout.containsKey("size")) {
					String size = layout.getString("size");
					if(size.equals("A4")) {
						pageLongSide = 841;
						pageShortSide = 595;
					} else if(size.equals("A3")) {
						pageLongSide = 1190;
						pageShortSide = 841;
					} else if(size.equals("letter")) {
						pageLongSide = 792;
						pageShortSide = 612;
					} 
				}
				if(layout.containsKey("page")) {
					String page = layout.getString("page");
					if(page.equals("portrait")) {
						pageHeight = pageLongSide;
						pageWidth = pageShortSide;
					} else if(page.equals("landscape")) {
						pageHeight = pageShortSide;
						pageWidth = pageLongSide;					
					}
				} else {
					pageHeight = pageLongSide;
					pageWidth = pageShortSide;
				}
			}
		} catch(Exception e) {
			throw new RedbackException("Error initiating pdf report", e);
		}
	}
	
	public void produce(List<ReportFilter> filters) throws RedbackException {
		try {			
			Map<String, Object> context = new HashMap<String, Object>();
			context.put("session", session);
			context.put("oc", new ObjectClientJSWrapper(reportManager.getObjectClient(), session));	
			if(filters.size() >= 1) {
				context.put("filterobjectname", filters.get(0).object);
				context.put("filter", filters.get(0).filter);
				context.put("search", filters.get(0).search);
				context.put("uid", filters.get(0).uid);
			}
			context.put("sets", ReportFilter.convertToDataList(filters));
			context.put("document", document);
			Box header = Box.VContainer(true);
			if(headerUnits != null) {
				for(int j = 0; j < headerUnits.size(); j++) 
					header.addChild(headerUnits.get(j).produce(context));
			}				
			Box footer = Box.VContainer(true);
			if(footerUnits != null) {
				for(int j = 0; j < footerUnits.size(); j++) 
					footer.addChild(footerUnits.get(j).produce(context));		
			}
			Box root = Box.VContainer(true);
			for(int i = 0; i < rootUnits.size(); i++) 
				root.addChild(rootUnits.get(i).produce(context));
			float maxHeight = pageHeight - marginTop - marginBottom - header.height - footer.height;
			List<Box> pages = paginate(root, maxHeight);
			for(int i = 0; i < pages.size(); i++) {
				context.put("page", (i + 1));
				renderPage(pages.get(i), header, footer, i);
			}
		} catch(Exception e) {
			e.printStackTrace();
			throw new RedbackException("Error producing report", e);
		}
	}
	
	protected List<Box> paginate(Box root, float maxHeight) {
		List<Box> pages = new ArrayList<Box>();
		List<Float> breakPoints = new ArrayList<Float>();
		root.resolveBreakPoints(breakPoints, 0);
		if(breakPoints.size() > 0) {
			for(int i = breakPoints.size() - 1; i >= 0; i--) {
				float bp = breakPoints.get(i);
				pages.add(0, root.breakAt(bp));
			}
			if(root.children.size() > 0)
				pages.add(0, root);
		} else {
			pages.add(root);
		}
		for(int i = 0; i < pages.size(); i++) {
			Box remainder = pages.get(i);
			while(remainder != null && remainder.height > maxHeight) {
				Box top = remainder;
				remainder = top.breakAt(maxHeight);
				if(remainder != null) {
					remainder.x = 0;
					remainder.y = 0;
					if(i + 1 < pages.size())
						pages.add(i + 1, remainder);
					else
						pages.add(remainder);
					i++;
				}
				if(top.height == 0) break; //It couldn't cut what's left
			}			
		}
		return pages;
	}
	
	protected void renderPage(Box content, Box header, Box footer, int pageNumber) throws IOException {
		PDPage pdPage = new PDPage(new PDRectangle(pageWidth, pageHeight));
		//PDPage pdPage = new PDPage();
		document.addPage(pdPage);
		PDPageContentStream contentStream = new PDPageContentStream(document, pdPage);
		
		renderReportBox(pdPage, contentStream, header, marginLeft, marginTop);
		renderReportBox(pdPage, contentStream, content, marginLeft, marginTop + header.height);
		renderReportBox(pdPage, contentStream, footer, marginLeft, pageHeight - marginBottom - footer.height);
		contentStream.close();			
	}
	
	protected void renderReportBox(PDPage page, PDPageContentStream stream, Box reportBox, float offsetx, float offsety) throws IOException {

		float pageTop = pageHeight;//782;
		if(reportBox.type.equals("container")) {
			if(reportBox.color != null) {
				stream.setNonStrokingColor(reportBox.color);
				stream.addRect(offsetx, pageTop - offsety - reportBox.height, reportBox.width, reportBox.height);
				stream.fill();
			}
			if(reportBox.borderColor != null) {
				stream.setStrokingColor(reportBox.borderColor);
				stream.setLineWidth(0.3f);
				stream.addRect(offsetx, pageTop - offsety - reportBox.height, reportBox.width, reportBox.height);
				stream.stroke();
			}
			for(Box rb : reportBox.children) {
				renderReportBox(page, stream, rb, offsetx + rb.x, offsety + rb.y);
			}
		} else if(reportBox.type.equals("hline")) {
			stream.setLineWidth(0.3f);
			float y = pageTop - offsety - (reportBox.height / 2);// - 4;
			stream.moveTo(offsetx, y);//(reportBox.height / 2) + 2);
			stream.setStrokingColor(reportBox.color);
			stream.lineTo(offsetx + reportBox.width, y);//(reportBox.height / 2) + 2);
			stream.stroke();

		} else if(reportBox.type.equals("text")) {
			if(reportBox.text != null) {
				String txt = reportBox.text.replaceAll("\t", "").replaceAll("\u00A0", "").replaceAll("\uFEFF", "");
				while(txt.length() > 0 && Utils.textWidth(reportBox.font, reportBox.fontSize, txt) > reportBox.width)
					txt = txt.substring(0, txt.length() - 1);
				stream.beginText(); 
				stream.setNonStrokingColor(reportBox.color);
				stream.setFont(reportBox.font, reportBox.fontSize);
				stream.newLineAtOffset(offsetx + (0.15f * reportBox.height), pageTop - offsety - (0.75f * reportBox.height) );
				stream.showText(txt);      
				stream.endText();
			}
						
		} else if(reportBox.type.equals("checkbox")) {
			String base64Str = null;
			if(reportBox.checked) {
				base64Str = "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"; 
			} else {
				base64Str = "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";
			}
			byte[] bytes = Base64.getDecoder().decode(base64Str);
			PDImageXObject pdImage = PDImageXObject.createFromByteArray(document, bytes, "image");
            stream.drawImage(pdImage, offsetx, pageTop - offsety - reportBox.height - 3, reportBox.width, reportBox.height);
		} else if(reportBox.type.equals("image")) {
			PDImageXObject pdImage = PDImageXObject.createFromByteArray(document, reportBox.bytes, "image");
            stream.drawImage(pdImage, offsetx, pageTop - offsety - reportBox.height, reportBox.width, reportBox.height);

		} else if(reportBox.type.equals("empty")) {
			
		}
	}
	
	public byte[] getBytes() throws RedbackException {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			document.save(baos);
			document.close();
			return baos.toByteArray();
		} catch(Exception e) {
			throw new RedbackException("Error get the bytes of the pdf document", e);
		}
	}

	public String getMime() {
		return "application/pdf";
	}
	
	public String getFilename() {
		return reportConfig.getName() + ".pdf";
	}
}
