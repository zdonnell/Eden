package com.zdonnell.eve.api;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;

public abstract class BaseRequest {

	public final static String baseURL = "https://api.eveonline.com/";

	protected APICredentials credentials;

	protected int actorID;

	/**
	 * the time that the resource is cached until, based on the last successful
	 * request
	 */
	protected String cachedTime;

	private Document xmlDoc = null;

	private DocumentBuilderFactory factory;

	private DocumentBuilder domBuilder;

	/**
	 * @param xmlString
	 *            a string that contains valid xml document markup
	 * @return a {@link Document} assembled from the xmlString
	 */
	protected Document buildDocument(String xmlString) 
	{
		factory = DocumentBuilderFactory.newInstance();

		try 
		{
			domBuilder = factory.newDocumentBuilder();

			InputStream responseStream = new ByteArrayInputStream(xmlString.getBytes());
			xmlDoc = domBuilder.parse(responseStream);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return xmlDoc;
	}

	/**
	 * @return the time that the resource is cached until
	 */
	public String cachedTime() 
	{
		return cachedTime;
	}
}
