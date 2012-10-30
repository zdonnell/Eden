package com.zdonnell.eve.api;

import org.w3c.dom.Document;

public class APIObject {

	protected APICredentials credentials;
	
	protected static String baseURL = "https://api.eveonline.com/";

	protected void setCredentials(APICredentials credentials) {
		this.credentials = credentials;
	}
	
	public APICredentials getCredentials()
	{
		return credentials;
	}
	
	/**
	 * Extend this class to generate a callback method for the 
	 * {@link ResourceManager} to use to parse the Document it acquires
	 * 
	 * @author zachd
	 *
	 * @param <T> The Generic Type the Document should be parsed into
	 */
	protected abstract class APIParser<T>
	{
		public abstract T parse(Document document);
	}
}
