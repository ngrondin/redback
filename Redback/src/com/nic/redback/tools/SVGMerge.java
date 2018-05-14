package com.nic.redback.tools;

import java.io.File;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class SVGMerge 
{
	public static void main(String args[])
	{
		try
		{
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			dbFactory.setValidating(false);
			dbFactory.setNamespaceAware(false);
			dbFactory.setFeature("http://xml.org/sax/features/namespaces", false);
			dbFactory.setFeature("http://xml.org/sax/features/validation", false);
			dbFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
			dbFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			
			Document masterDoc = dBuilder.newDocument();
			Element masterRootElement = masterDoc.createElement("svg");
			masterRootElement.setAttribute("version", "1.1");
			masterRootElement.setAttribute("xmlns", "http://www.w3.org/2000/svg");
			masterRootElement.setAttribute("xmlns:xlink", "http://www.w3.org/1999/xlink");
			masterRootElement.setAttribute("x", "0px");
			masterRootElement.setAttribute("y", "0px");
			masterRootElement.setAttribute("viewBox", "0 0 24 24");
			masterRootElement.setAttribute("style", "enable-background:new 0 0 24 24;");
			masterRootElement.setAttribute("xml:space", "preserve");
			masterDoc.appendChild(masterRootElement);
			
			File dir = new File(args[0]);
			File[] files = dir.listFiles();
			for(int i = 0; i < files.length; i++)
			{
				double scale = 1.0;
				String name = files[i].getName();
				name = name.substring(4,  name.length() - 4);
				Element masterGElement = masterDoc.createElement("g");
				masterGElement.setAttribute("id", name);
				masterRootElement.appendChild(masterGElement);
				
				Document doc = dBuilder.parse(files[i]);
				doc.getDocumentElement().normalize();
				Element localRootElement = (Element)doc.getElementsByTagName("svg").item(0);
				String viewboxStr = localRootElement.getAttribute("viewBox");
				if(viewboxStr != null)
					scale = 24.0 / (Double.parseDouble(viewboxStr.split(" ")[3]));
				masterGElement.setAttribute("transform", "scale(" + scale + ")");
				
				NodeList nList = localRootElement.getChildNodes();
				for(int j = 0; j < nList.getLength(); j++)
				{
					Node node = nList.item(j);
					if(node instanceof Element)
					{
						Element localElement = (Element)node;
						if(hasChildElement(localElement))
						{
							Element newElement = processElement(localElement, scale);
							masterDoc.adoptNode(newElement);
							masterGElement.appendChild(newElement);
						}
					}
				}
			}
			
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
			DOMSource source = new DOMSource(masterDoc);
			StreamResult result = new StreamResult(new File("file.svg"));
			transformer.transform(source, result);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static boolean hasChildElement(Element el)
	{
		boolean hasChildElement = false;
		NodeList nList = el.getChildNodes();
		for(int i = 0; i < nList.getLength(); i++)
			if(nList.item(i) instanceof Element)
				hasChildElement = true;
		return hasChildElement;
	}
	
	public static Element processElement(Element inElement, double scale)
	{
		Element outElement = (Element)inElement.cloneNode(false);
		/*
		NamedNodeMap attributes = inElement.getAttributes();
		for(int i = 0; i < attributes.getLength(); i++)
		{
			Node attribute = attributes.item(i);
			//System.out.println(attribute.getNodeName());
			//if(attribute.getNodeName() == "d")
			//	outElement.setAttribute(attribute.getNodeName(), processD(inElement.getAttribute(attribute.getNodeName()), scale));
		}
		*/
		NodeList list = inElement.getChildNodes();
		for(int i = 0; i < list.getLength(); i++)
		{	
			Node node = list.item(i);
			if(node instanceof Element)
			{
				Element outChildElement = processElement((Element)list.item(i), scale);
				outElement.appendChild(outChildElement);
			}
		}
		return outElement;
	}

	/*
	public static String processD(String in, double scale)
	{
		NumberFormat formatter = new DecimalFormat("#0.000");     
		String out = "";
		int pos = 0;
		int state = 0;
		char command = 0;
		String numStr = "";
		int numCount = 0;
		while(pos < in.length())
		{
			char c = in.charAt(pos);
			if(state == 0)
			{
				command = c;
				System.out.println(command);
				out += command;
				state = 1;
			}
			else if(state == 1)
			{
				if(c == '-'  ||  Character.isDigit(c))
				{
					pos--;
					state = 2;
				}
			}
			else if(state == 2)
			{
				if(c == ','  ||  c == ' ' ||  Character.isAlphabetic(c)   ||  (c == '-'  &&  numStr.length() > 0))
				{
					Double val = null;
					try
					{
						val = Double.parseDouble(numStr);
					}
					catch(Exception e)
					{
						System.out.println(numStr);
					}
					val *= scale;
					if(numCount > 0)
						out += ",";
					out += formatter.format(val);
					numStr = "";
					numCount++;

					if(c == ' ' || c == ',')
					{
						state = 1;
					}
					else if(c == '-')
					{
						pos--;
						state = 2;
					}
					else if(Character.isAlphabetic(c))
					{
						numCount = 0;
						pos--;
						state = 0;
					}
				}
				else
				{
					numStr += c;
				}
			}
			pos++;
		}
		return out;
	}
	
	*/
}
