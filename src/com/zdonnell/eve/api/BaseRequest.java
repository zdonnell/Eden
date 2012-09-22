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
	 * @param url
	 *            the full URL of the resource requested
	 * @param postData
	 *            A List of {@link NameValuePair} to be sent as POST Data
	 * @return a String representation of the response
	 */
	protected String makeHTTPRequest(String resURL, List<NameValuePair> postData) {
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(baseURL + resURL);

		String rawResponse = null;

		try {
			httppost.setEntity(new UrlEncodedFormEntity(postData));
			HttpResponse response = httpclient.execute(httppost);
			HttpEntity returnEntity = response.getEntity();

			if (returnEntity != null)
				rawResponse = EntityUtils.toString(returnEntity);

		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
		} catch (IOException e) {
			// TODO Auto-generated catch block
		}

		return rawResponse;
	}

	/**
	 * @param xmlString
	 *            a string that contains valid xml document markup
	 * @return a {@link Document} assembled from the xmlString
	 */
	protected Document buildDocument(String xmlString) {
		factory = DocumentBuilderFactory.newInstance();

		try {
			domBuilder = factory.newDocumentBuilder();

			InputStream responseStream = new ByteArrayInputStream(
					xmlString.getBytes());
			xmlDoc = domBuilder.parse(responseStream);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return xmlDoc;
	}

	/**
	 * @return the time that the resource is cached until
	 */
	public String cachedTime() {
		return cachedTime;
	}
}
