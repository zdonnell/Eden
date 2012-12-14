package com.zdonnell.eve.api;

import java.util.ArrayList;

import android.content.Context;
import android.os.AsyncTask;

public class RequestManager {

	/** 
	 * Singleton instance of the Request Manager
	 */
	private static RequestManager instance;
	
	/**
	 * List of all currently in progress requests.
	 */
	private ArrayList<Request> activeRequests = new ArrayList<Request>();
	
	/**
	 * Singleton access method
	 * 
	 * @param context
	 * @return
	 */
	public static RequestManager getInstance(Context context)
	{
		if (instance == null)
		{
			instance = new RequestManager();
		}
		return instance;
	}
	
	public void registerRequest(Request newRequest)
	{
		activeRequests.add(newRequest);
	}
	
	public boolean stopRequest(Request requestToStop)
	{
		if (activeRequests.contains(requestToStop))
		{
			boolean successful = requestToStop.cancel();
			activeRequests.remove(requestToStop);
			
			return successful;
		}
		else return false;
	}
	
	@SuppressWarnings("rawtypes")
	public abstract static class Request
	{
		private int maxProgress;
		
		private int curProgress;
		
		private AsyncTask task;
		
		private long startTime;
		
		public Request(int maxProgress, AsyncTask task)
		{
			this.maxProgress = maxProgress;
			this.task = task;
		}
		
		public void setStartTime(long startTime) { this.startTime = startTime; }
		public void updateProgress(int curProgress) { this.curProgress = curProgress; }
		
		public boolean cancel() { return task.cancel(true); }
		
		public int getProgress() { return curProgress; }
		public int getMaxProgress() { return maxProgress; }
	}
}
