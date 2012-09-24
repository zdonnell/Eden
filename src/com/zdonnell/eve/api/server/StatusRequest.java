package com.zdonnell.eve.api.server;

import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.w3c.dom.Document;

import com.zdonnell.eve.api.BaseRequest;

/**
 * 
 * @author Zach
 * 
 */
public class StatusRequest extends BaseRequest 
{
	public final static String URL = "/server/ServerStatus.xml.aspx";

	/**
	 * Queries the API server, and retrieves the data.
	 * 
	 * @return an ArrayList of characters found on the account
	 */
	public String[] get() 
	{
		/* Query the server */
		String rawResponse = super.makeHTTPRequest(URL, new ArrayList<NameValuePair>());
		/* Build response into Document Model */
		Document responseDoc = super.buildDocument(rawResponse);

		String[] status = new String[2];
		
		status[0] = responseDoc.getElementsByTagName("serverOpen").item(0).getTextContent();
		status[1] = responseDoc.getElementsByTagName("onlinePlayers").item(0).getTextContent();

		cachedTime = responseDoc.getElementsByTagName("cachedUntil").item(0).getTextContent();

		return status;
	}
}
