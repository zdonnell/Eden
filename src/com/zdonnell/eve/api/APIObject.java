package com.zdonnell.eve.api;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;

public class APIObject {

	protected APICredentials credentials;

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
	
	/**
	 * Extend this class to provide instructions for an {@link APIObject} request
	 * to call when finished
	 * 
	 * @author zachd
	 *
	 * @param <T> The Generic type the onUpdate method should take and provide to the callback
	 * code
	 */
	public abstract class APICallback<T> 
	{
		public abstract void onUpdate(T updatedData);
	}
}
