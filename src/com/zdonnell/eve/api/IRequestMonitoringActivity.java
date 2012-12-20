package com.zdonnell.eve.api;

import java.util.ArrayList;

/**
 * If you intend to monitor active requests from an activity, it will need to implement this interface
 * 
 * @author zachd
 *
 */
public interface IRequestMonitoringActivity {

	/**
	 * This method is called by the {@link ResourceRequestMonitor} to notify the current activity that the active requests
	 * list has been updated (Removal or Addition).
	 * 
	 * @param activeRequests
	 */
	public void requestsUpdated(ArrayList<ResourceRequestMonitor.Request> activeRequests);
	
}
