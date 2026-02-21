package io.redback.tools;

import java.io.File;
import java.io.FileInputStream;

import io.firebus.data.DataMap;

public class AccessReview {
	public static void main(String[] args) {
		if(args.length >= 1) {
			String folderPath = args[0];
			File folder = new File(folderPath);
			reviewFolder(folder);			
		}
	}
	
	public static void reviewFolder(File folder) {
		if(folder.isDirectory()) {
			File[] files = folder.listFiles();
			for(File file: files) {
				if(!file.isDirectory()) {
					reviewFile(file);
				} else {
					reviewFolder(file);
				}
			}
		}
	}
	
	public static void reviewFile(File file) {
		String path = file.getPath();
		String[] parts = path.split("/");
		String service = parts[parts.length - 3];
		String cat = parts[parts.length - 2];
		String filename = parts[parts.length - 1];
		if(filename.endsWith(".json")) {
			try {
				DataMap data = new DataMap(new FileInputStream(file));
				generalReview(service, cat, filename, data);
			} catch(Exception e) {
				System.out.println("Error in " + service + "/" + cat + "/" + filename);
				e.printStackTrace();
			}			
		}
	}
	
	public static void generalReview(String service, String cat, String filename, DataMap data) {
		String accesscat = data.getString("accesscat");
		System.out.println(service + "\t" + cat + "\t" + filename + "\t" + accesscat);

	}
}
