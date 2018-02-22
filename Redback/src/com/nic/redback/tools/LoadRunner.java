package com.nic.redback.tools;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import com.nic.firebus.Firebus;
import com.nic.firebus.Payload;
import com.nic.firebus.exceptions.FunctionTimeoutException;

public class LoadRunner 
{
	protected Firebus firebus;
	protected ArrayList<String> calls;
	protected Random rnd;
	protected int cycles;
	protected HashMap<Integer, ArrayList<Long>> results;
	
	public LoadRunner(String netName, String pass, String filePath, int c)
	{
		firebus = new Firebus(netName, pass);
		calls = new ArrayList<String>();
		rnd = new Random();
		cycles = c;
		results = new HashMap<Integer, ArrayList<Long>>();
		int i = 0;
		try
		{
			BufferedReader br = new BufferedReader(new FileReader(filePath));
			String line = null;
			while((line = br.readLine()) != null)
			{
				calls.add(line);
				results.put(i, new ArrayList<Long>());
			}
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public void run()
	{
		for(int i = 0; i < cycles; i++)
		{
			int r = rnd.nextInt(calls.size()-1);
			String call = calls.get(r);
			int sep = call.indexOf(" ");
			String service = call.substring(0, sep);
			String msg = call.substring(sep);
			long ct = System.currentTimeMillis();
			try
			{
				Payload resp = firebus.requestService(service, new Payload(msg));
			}
			catch(Exception e)
			{
				
			}
			long dur = System.currentTimeMillis() - ct;
			results.get(r).add(dur);
		}
		
		for(int i = 0; i < calls.size(); i++)
		{
			long sum = 0;
			ArrayList<Long> list = results.get(i);
			for(int j = 0; j < list.size(); j++)
				sum += list.get(j);
			long avg = sum / list.size();
			System.out.println(calls.get(i) + " : " + avg);
		}
	}
	
	public static void main(String[] args)
	{
		LoadRunner lr = new LoadRunner(args[0], args[1], args[2], Integer.parseInt(args[3]));
		lr.run();
	}
}
