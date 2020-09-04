package io.redback.managers.reportmanager;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import io.firebus.utils.DataMap;
import io.redback.RedbackException;
import io.redback.security.Session;

public class Report {
	protected Session session;
	protected ReportManager reportManager;
	protected ReportConfig reportConfig;
	protected PDDocument document;
		
	public Report(Session s, ReportManager rm, ReportConfig rc) {
		session = s;
		reportManager = rm;
		reportConfig = rc;
		document = new PDDocument();
	}
	
	public void produce(DataMap filter) throws RedbackException {
		try {
			Map<String, Object> context = new HashMap<String, Object>();
			context.put("session", session);
			context.put("filter", filter);
			context.put("document", document);
			ReportBox root = reportConfig.produce(context);
			List<ReportBox> pages = paginate(root);
			for(ReportBox page: pages) {
				PDPage pdPage = new PDPage();
				document.addPage(pdPage);
				PDPageContentStream contentStream = new PDPageContentStream(document, pdPage);
				render(pdPage, contentStream, page, 50, 50);
				contentStream.close();	
			}			
		} catch(Exception e) {
			e.printStackTrace();
			throw new RedbackException("Error producing report", e);
		}
	}
	
	protected List<ReportBox> paginate(ReportBox root) {
		List<ReportBox> pages = new ArrayList<ReportBox>();
		List<Float> breakPoints = new ArrayList<Float>();
		root.resolveBreakPoints(breakPoints, 0);
		for(int i = breakPoints.size() - 1; i >= 0; i--) {
			float bp = breakPoints.get(i);
			if(bp > 0)
				pages.add(0, root.breakAt(bp));
			else 
				pages.add(root);
		}
		for(int i = 0; i < pages.size(); i++) {
			ReportBox remainder = pages.get(i);
			while(remainder != null && remainder.height > 700) {
				ReportBox top = remainder;
				remainder = top.breakAt(700);
				remainder.x = 0;
				remainder.y = 0;
				pages.add(i + 1, remainder);
				i++;
			}			
		}
		return pages;
	}
	
	protected void render(PDPage page, PDPageContentStream stream, ReportBox reportBox, float offsetx, float offsety) throws IOException {

		float pageTop = 782;
		if(reportBox.type.equals("container")) {
			for(ReportBox rb : reportBox.children) {
				render(page, stream, rb, offsetx + rb.x, offsety + rb.y);
			}
		} else if(reportBox.type.equals("hline")) {
			stream.setLineWidth(0.3f);
			stream.setStrokingColor(Color.BLACK);
			stream.moveTo(offsetx, pageTop - offsety + (reportBox.height / 2));
			stream.lineTo(offsetx + reportBox.width, 782 - offsety + (reportBox.height / 2));
			stream.stroke();

		} else if(reportBox.type.equals("text")) {
			if(reportBox.text != null) {
				stream.beginText(); 
				stream.setFont(reportBox.font, reportBox.fontSize);
				stream.newLineAtOffset(offsetx, pageTop - offsety);
				stream.showText(reportBox.text);      
				stream.endText();
			}
						
		} else if(reportBox.type.equals("checkbox")) {
			stream.setLineWidth(0.3f);
			stream.setStrokingColor(Color.BLACK);
			stream.addRect(offsetx, pageTop - offsety, 12, 12);
			if(reportBox.checked) {
				stream.moveTo(offsetx, pageTop - offsety);
				stream.lineTo(offsetx + 12, pageTop - offsety + 12);
				stream.moveTo(offsetx, pageTop - offsety + 12);
				stream.lineTo(offsetx + 12, pageTop - offsety);
			}
			stream.stroke();

		} else if(reportBox.type.equals("image")) {
			PDImageXObject pdImage = PDImageXObject.createFromByteArray(document, reportBox.bytes, "image");
            stream.drawImage(pdImage, offsetx, pageTop - offsety - reportBox.height);

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
}
