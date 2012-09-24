package com.zdonnell.eve.api.server;

import java.util.ArrayList;

import android.content.Context;

import com.zdonnell.eve.api.APIObject;
import com.zdonnell.eve.api.CachedTimeDB;

public class Server extends APIObject {
	
	/**
	 * Obtains the status of the TQ Server
	 * 
	 * @return An array storing the status values, where index 0 = Online/Offline
	 * and index 1 = Online Players
	 */
	public String[] status() 
	{
		StatusRequest request = new StatusRequest();

		return request.get();
	}
}
