package com.zdonnell.eve.api.server;

import org.w3c.dom.Document;

import android.content.Context;

import com.zdonnell.eve.api.APICredentials;
import com.zdonnell.eve.api.APIObject;
import com.zdonnell.eve.api.BaseRequest;
import com.zdonnell.eve.api.ResourceManager;

public class Server extends APIObject {
	
	private ResourceManager resourceManager;
	
	public Server(Context context)
	{
		super.setCredentials(credentials);
		resourceManager = new ResourceManager(context);
	}
	
	/**
	 * Obtains the status of the TQ Server
	 * 
	 * @return An array storing the status values, where index 0 = Online/Offline
	 * and index 1 = Online Players
	 */
	public String[] status() 
	{
		final String resourceSpecificURL = "server/ServerStatus.xml.aspx";
		String fullURL = BaseRequest.baseURL + resourceSpecificURL;
		
		Document responseDoc = resourceManager.getResource(fullURL, true);

		String[] status = new String[2];
		
		status[0] = responseDoc.getElementsByTagName("serverOpen").item(0).getTextContent();
		status[1] = responseDoc.getElementsByTagName("onlinePlayers").item(0).getTextContent();

		return status;
	}
}
