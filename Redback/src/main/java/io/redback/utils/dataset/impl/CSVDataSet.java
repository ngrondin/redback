package io.redback.utils.dataset.impl;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Pattern;

import io.redback.utils.StringUtils;

public class CSVDataSet extends MemoryDataSet {
	
	public CSVDataSet(String str) {
    	String strm = str.replaceAll("\r", "").replaceAll(String.valueOf((char)65279), "");
    	if(StringUtils.isUTF7(strm))
    		strm = StringUtils.decodeUTF7(strm);
    	boolean firstLineFound = false;
    	boolean linesEnclosedWithQuotes = false;
    	int startFirstLine = 0;
    	int endFirstLine = 0;
    	char sep = ',';
    	char[] seps = {',', '|', '\t', ';'};
    	while(!firstLineFound) {
    		linesEnclosedWithQuotes = false;
        	endFirstLine = strm.indexOf("\n", startFirstLine);
        	String headerLine = strm.substring(startFirstLine, endFirstLine).trim();
        	if(headerLine.startsWith("\"") && headerLine.endsWith("\"") && headerLine.substring(0, headerLine.length() - 1).indexOf("\"") == -1) {
        		linesEnclosedWithQuotes = true;
        		headerLine = headerLine.substring(1, headerLine.length() - 1);
        	}
        	for(char s : seps) 
        		if(headerLine.indexOf(s) > -1)
        			sep = s;
    		headers = headerLine.split(Pattern.quote(String.valueOf(sep)));
    		if(headers.length > 0) {
        		firstLineFound = true;
        		for(int i = 0; i < headers.length; i++)
        			if(headers[i].length() == 0) 
        				firstLineFound = false;
        			else if(headers[i].startsWith("\"") && headers[i].endsWith("\""))
        				headers[i] = headers[i].substring(1, headers[i].length() - 1);
    		}
    		if(!firstLineFound)
    			startFirstLine = endFirstLine + 1;
    	}

		String body = strm.substring(endFirstLine).trim();
		if(linesEnclosedWithQuotes) body = body.substring(1).replaceAll("\"\n\"", "\n");
		StringBuilder buffer = new StringBuilder();
		Object[] row = new Object[headers.length];
		int col = 0;
		boolean inQuote = false;
		boolean escapeNext = false;
		for(int ptr = 0; ptr <= body.length(); ptr++) {
			char c = ptr < body.length() ? body.charAt(ptr) : 0;
			if(inQuote) {
				if(c == '\\' && escapeNext == false) {
					escapeNext = true;
				} else if(escapeNext) {
					if(c == 'n') buffer.append("\n");
					if(c == '"') buffer.append("\"");
					escapeNext = false;
				} else if(c == '"') {
					inQuote = !inQuote;
				} else {
					buffer.append(c);
				}				
			} else {
				boolean lastCharOfBody = ptr == body.length();
				boolean lastCharOfLine = lastCharOfBody || c == '\n';
				boolean lastCharOfField = lastCharOfLine || c == sep;
				if(lastCharOfField) {
					String val = buffer.length() > 0 ? buffer.toString() : null;
					if(col < headers.length) row[col] = val;    						
					buffer = new StringBuilder();
					col++;
					if(lastCharOfLine) {
						data.add(row);
						row = new Object[headers.length];
						col = 0;						
					}
				} else if(c == '"') {
					inQuote = true;
				} else {
					buffer.append(c);
				}
			}
		}
		cur = 0;
	}
	
	public static CSVDataSet fromFile(String path) throws IOException {
		String content = new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
		return new CSVDataSet(content);
	}
}
