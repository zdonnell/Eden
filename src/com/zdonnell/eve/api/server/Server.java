package com.zdonnell.eve.api.server;

import org.w3c.dom.Document;

import android.content.Context;

import com.zdonnell.eve.api.APICallback;
import com.zdonnell.eve.api.APIObject;
import com.zdonnell.eve.api.ResourceManager;
import com.zdonnell.eve.api.ResourceManager.APIRequestWrapper;

public class Server extends APIObject {
	
	public static final int STATUS = 0;

	public static final String[] xmlURLs = new String[1];
	static
	{
		xmlURLs[STATUS] = baseURL + "server/ServerStatus.xml.aspx";
	}
	
	private ResourceManager resourceManager;
	
	public Server(Context context)
	{
		super.setCredentials(credentials);
		resourceManager = ResourceManager.getInstance(context);
	}
	
	/**
	 * Obtains the status of the TQ Server
	 * 
	 * @return An array storing the status values, where index 0 = Online/Offline
	 * and index 1 = Online Players
	 */
	public void status(APICallback<String[]> apiCallback) 
	{		
		resourceManager.get(new APIRequestWrapper(apiCallback, new StatusParser(), null, xmlURLs[STATUS], true));
	}
	
	private class StatusParser extends APIParser<String[]>
	{
		@Override
		public String[] parse(Document document) {
			String[] status = new String[2];
			
			status[0] = document.getElementsByTagName("serverOpen").item(0).getTextContent();
			status[1] = document.getElementsByTagName("onlinePlayers").item(0).getTextContent();

			return status;
		}
	}
}
