package io.redback.tools;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

import io.firebus.Firebus;
import io.firebus.Payload;

public class LoadRunner 
{
	protected Firebus firebus;
	protected ArrayList<String> calls;
	protected Random rnd;
	protected int cycles;
	protected HashMap<String, ArrayList<Long>> results;
	protected int activeThreads;
	
	protected class LoadRunnerThread extends Thread
	{
		protected Firebus firebus;
		protected LoadRunner master;
		protected int cyclesLeft;
		
		public LoadRunnerThread(Firebus fb, LoadRunner m, int c)
		{
			firebus = fb;
			master = m;
			cyclesLeft = c;
			setName("rbLoadRunnerWorker" + getId());
		}
		
		public void run()
		{
			while(cyclesLeft > 0)
			{
				String call = master.getNextCall();
				int sep = call.indexOf(" ");
				String service = call.substring(0, sep);
				String msg = call.substring(sep);
				long dur = 0;
				long ct = System.currentTimeMillis();
				try
				{
					firebus.requestService(service, new Payload(msg));
					dur = System.currentTimeMillis() - ct;
				}
				catch(Exception e)
				{
					dur = -1;
				}
				master.finishedCall(call, dur);
				cyclesLeft--;
			}
			master.finishedRun();
		}
	}
	
	public LoadRunner(String netName, String pass, String filePath, int at, int c)
	{
		firebus = new Firebus(netName, pass);
		/*
		firebus.registerServiceProvider("stub", new ServiceProvider() {
			public Payload service(Payload payload)	throws FunctionErrorException
			{
				return new Payload("allo " + payload.getString());
			}

			public ServiceInformation getServiceInformation()
			{
				return null;
			}}, 10);*/
		calls = new ArrayList<String>();
		rnd = new Random();
		activeThreads = at;
		cycles = c;
		results = new HashMap<String, ArrayList<Long>>();
		try
		{
			BufferedReader br = new BufferedReader(new FileReader(filePath));
			String line = null;
			while((line = br.readLine()) != null)
			{
				if(!line.startsWith("//"))
				{
					calls.add(line);
					results.put(line, new ArrayList<Long>());
				}
			}
			br.close();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public String getNextCall()
	{
		int r = rnd.nextInt(calls.size());
		String call = calls.get(r);
		return call;
	}
	
	public synchronized void finishedCall(String call, long dur)
	{
		results.get(call).add(dur);
	}
	
	public synchronized void finishedRun()
	{
		activeThreads--;
	}
	
	public void run()
	{
		for(int i = 0; i < activeThreads; i++)
		{
			LoadRunnerThread lrt = new LoadRunnerThread(firebus, this, cycles);
			lrt.start();
		}

		boolean wait = true;
		while(wait) 
		{
			synchronized(this)
			{
				if(activeThreads == 0)
					wait = false;
			}
			try { Thread.sleep(100); } catch(Exception e) {}
		}
		
		Iterator<String> it = results.keySet().iterator();
		while(it.hasNext())
		{
			long sum = 0;
			long resultCount = 0;
			long timeoutCount = 0;
			String call = it.next();
			ArrayList<Long> list = results.get(call);
			for(int j = 0; j < list.size(); j++)
			{
				if(list.get(j) > 0)
				{
					resultCount++;
					sum += list.get(j);
				}
				else
				{
					timeoutCount++;
				}
			}
			long avg = -1;
			if(list.size() > 0)
				avg = sum / list.size();
			String callDisp = String.format("%-40s", call);
			if(callDisp.length() > 40)
				callDisp = callDisp.substring(0, 40);
			System.out.println(callDisp + "\t\t\tavg: " + avg + "\t\t\tresults: " + resultCount + "\t\t\ttimeouts: " + timeoutCount);
		}
		firebus.close();
	}
	
	public static void main(String[] args)
	{
		try
		{
			Thread.currentThread().setName("rbLoadRunner");
			LoadRunner lr = new LoadRunner(args[0], args[1], args[2], Integer.parseInt(args[3]), Integer.parseInt(args[4]));
			Thread.sleep(2000);
			lr.run();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}
