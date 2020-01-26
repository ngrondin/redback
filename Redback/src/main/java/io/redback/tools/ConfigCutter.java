package io.redback.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;

import io.firebus.utils.DataList;
import io.firebus.utils.DataMap;

public class ConfigCutter {

	
	public static void main(String[] args)
	{
		String targetPath = "C:\\Users\\ngron\\git\\redbackwms\\src\\main\\resources\\com\\nic\\redback\\wms\\config";
		String sourcePath = "C:\\tmp\\rbwms";
		File dir = new File(sourcePath);
		File[] files = dir.listFiles();
		for(int i = 0; i < files.length; i++)
		{
			File file = files[i];
			String fileName = file.getName(); 
			if(fileName.startsWith("rb"))
			{
				String base = fileName.substring(0, fileName.length() - 5);
				String[] parts = base.split("_");
				try
				{
					FileReader fr = new FileReader(file);
					BufferedReader br = new BufferedReader(fr);
					String line = null;
					StringBuilder sb = new StringBuilder();
					while((line = br.readLine()) != null)
						sb.append(line + "\n");
					DataMap data = new DataMap("{\"configs\":[" + sb.toString() + "]}");
					DataList list = data.getList("configs");
					for(int j = 0; j < list.size(); j++)
					{
						DataMap cfg = list.getObject(j);
						File dir1 = new File(targetPath + "\\" + parts[0]);
						dir1.mkdir();
						File dir2 = new File(targetPath + "\\" + parts[0] + "\\" + parts[1]);
						dir2.mkdir();
						File newFile = new File(targetPath + "\\" + parts[0] + "\\" + parts[1] + "\\" + cfg.getString("name") + ".json");
						newFile.createNewFile();
						String body = cfg.toString();
						FileOutputStream fos = new FileOutputStream(newFile);
						fos.write(body.getBytes());
						fos.close();
					}
					fr.close();
					br.close();
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
		}
	}
}
