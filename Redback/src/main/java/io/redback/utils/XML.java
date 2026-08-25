package io.redback.utils;

import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import io.redback.exceptions.RedbackException;

public class XML {
	protected Document document;
	protected Element element;
	
	public XML(InputStream is) throws RedbackException {
		try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            document = builder.parse(is);
            document.getDocumentElement().normalize();
            element = document.getDocumentElement();
		} catch(Exception e) {
			throw new RedbackException("Error parsing xml document", e);
		}
	}
	
	public XML(String t) throws RedbackException {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	        DocumentBuilder builder = factory.newDocumentBuilder();
	        document = builder.newDocument();
	        element = document.createElement(t);
	        document.appendChild(element);
		} catch(Exception e) {
			throw new RedbackException("Error creating xml document", e);
		}
	}
	
	private XML(Document doc, Element el) {
		document = doc;
		element = el;
	}
	
	public void setAttribute(String k, String v) {
		element.setAttribute(k, v);
	}
	
	public String getAttribute(String k) {
		return element.getAttribute(k);
	}
	
	public XML createChild(String t) {
        Element child = document.createElement(t);
        element.appendChild(child);
		return new XML(document, child);
	}
	
	public List<XML> getChildren() {
		NodeList list = element.getChildNodes();
		List<XML> children = new ArrayList<XML>();
		for(int i = 0; i < list.getLength(); i++) 
			if(list.item(i) instanceof Element)
				children.add(new XML(document, (Element)list.item(i)));
		return children;
	}
	
	public List<XML> getChildrenByTagName(String t) {
		NodeList list = element.getChildNodes();
		List<XML> children = new ArrayList<XML>();
		for(int i = 0; i < list.getLength(); i++) 
			if(list.item(i) instanceof Element && ((Element)list.item(i)).getTagName().equals(t))
				children.add(new XML(document, (Element)list.item(i)));
		return children;
	}
	
	public XML getFirstChildByTagName(String t) {
		NodeList list = element.getChildNodes();
		for(int i = 0; i < list.getLength(); i++) 
			if(list.item(i) instanceof Element && ((Element)list.item(i)).getTagName().equals(t))
				return new XML(document, (Element)list.item(i));
		return null;
	}
	
	public void setText(String t) {
		element.setTextContent(t);
	}
	
	public String getText() {
		return element.getTextContent();
	}
	
	public String toString()  {
		try {
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
	        Transformer transformer = transformerFactory.newTransformer();
	        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	        transformer.setOutputProperty("{http://apache.org}indent-amount", "4");
	        DOMSource source = new DOMSource(element);
	        StringWriter writer = new StringWriter();
	        StreamResult result = new StreamResult(writer);
	        transformer.transform(source, result);
	        return writer.toString();
		} catch(Exception e) {
			return "<xml>" + e.getMessage() + "</xml>";
		}
		
	}

}
