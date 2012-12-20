package com.zdonnell.eve.api;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;

public class ResourceRequestMonitor {

	/** 
	 * Singleton instance of the Request Manager
	 */
	private static ResourceRequestMonitor instance;
	
	/**
	 * List of all currently in progress requests.
	 */
	private ArrayList<Request> activeRequests = new ArrayList<Request>();
	
	/**
	 * This is a pointer to the currently displayed activity.  For more information see {@link #bindActivity(Activity)}
	 */
	private IRequestMonitoringActivity currentlyBoundActivity;
	
	/**
	 * Singleton access method
	 * 
	 * @param context
	 * @return
	 */
	public static ResourceRequestMonitor getInstance(Context context)
	{
		if (instance == null)
		{
			instance = new ResourceRequestMonitor();
		}
		return instance;
	}
	
	public void registerRequest(Request newRequest)
	{
		activeRequests.add(newRequest);
		
		@SuppressWarnings("unchecked")
		ArrayList<Request> copyOfActiveRequests = (ArrayList<Request>) activeRequests.clone();
		if (currentlyBoundActivity != null) currentlyBoundActivity.requestsUpdated(copyOfActiveRequests);
	}
	
	public void giveProgressUpdate(Request requestToUpdate, int curProgress) throws RequestNotActiveException
	{
		if (activeRequests.contains(requestToUpdate))
		{
			requestToUpdate.updateProgress(curProgress);
			currentlyBoundActivity.requestsUpdated(activeRequests);
		}
		else throw new RequestNotActiveException();
	}
	
	public void requestCompleted(Request completedRequest) throws RequestNotActiveException
	{
		if (activeRequests.contains(completedRequest))
		{
			activeRequests.remove(completedRequest);
			currentlyBoundActivity.requestsUpdated(activeRequests);
		}
		else throw new RequestNotActiveException();
	}
	
	public boolean stopRequest(Request requestToStop) throws RequestNotActiveException
	{
		if (activeRequests.contains(requestToStop))
		{
			boolean successful = requestToStop.cancel();
			activeRequests.remove(requestToStop);
			
			return successful;
		}
		else throw new RequestNotActiveException();
	}
	
	/**
	 * This method should be used to bind the current activity to the RequestManager, this will
	 * allow the RequestManager to send updates to the activity when they become available.
	 * 
	 * @see {@link IRequestMonitoringActivity}
	 * @param activity
	 */
	public void bindActivity(IRequestMonitoringActivity activity)
	{
		currentlyBoundActivity = activity;
	}
	
	@SuppressWarnings("rawtypes")
	public static class Request
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
	
	public class RequestNotActiveException extends Exception
	{

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		
	}
}
