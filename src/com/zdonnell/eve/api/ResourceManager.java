package com.zdonnell.eve.api;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
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
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;

import android.content.Context;
import android.util.Log;

/**
 * Class to handle the storage and correct access of API Resources
 * 
 * @author Zach
 *
 */
public class ResourceManager {

	private static ResourceManager instance;
	
	private Context context;
	
	private CacheDatabase cacheDatabase;
	
	private APICredentials credentials;

	private boolean needsCredentials = true;
	
	private ResourceManager(Context context)	
	{
		this.context = context;
		cacheDatabase = new CacheDatabase(context);
	}
	
	public static ResourceManager getInstance(Context context)
	{
		if (instance == null)
		{
			instance = new ResourceManager(context);
		}
		return instance;
	}
	
	/**
	 * Obtains the raw API resource specified by resourceURL and actorID
	 * 
	 * @param resourceURL
	 * @param uniqueIDs 
	 * @return
	 */
	public Document getResource(APICredentials credentials, String resourceURL, boolean refresh, NameValuePair ...uniqueIDs)
	{
		String cachedResource = "";
		
		if (cacheDatabase.isCached(resourceURL, uniqueIDs, refresh))
		{
			cachedResource = cacheDatabase.getCachedResource(resourceURL, uniqueIDs);
		}
		else
		{
			ArrayList<NameValuePair> assembledPOSTData = new ArrayList<NameValuePair>();
			for (NameValuePair nvp : uniqueIDs) assembledPOSTData.add(nvp);
			
			if (credentials != null)
			{
				assembledPOSTData.add(new BasicNameValuePair("keyID", String.valueOf(credentials.keyID)));
				assembledPOSTData.add(new BasicNameValuePair("vCode", credentials.verificationCode));
			}
			
			cachedResource = queryResource(resourceURL, assembledPOSTData);
			
			if (cachedResource != null)
			{
				if (uniqueIDs.length == 0 && credentials != null) 
				{
					uniqueIDs = new BasicNameValuePair[] { new BasicNameValuePair("keyID", String.valueOf(credentials.keyID))};
				}
				cacheDatabase.updateCache(resourceURL, uniqueIDs, cachedResource);
			}
		}
		
		Log.d("RESOURCE", cachedResource);
		return buildDocument(cachedResource);
	}
	
	/**
	 * @param url the full URL of the resource requested
	 * @param postData A List of {@link NameValuePair} to be sent as POST Data
	 * @return a String representation of the served resource
	 */
	protected String queryResource(String resourceURL, List<NameValuePair> postData) 
	{	
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(resourceURL);

		String rawResponse = null;

		try 
		{
			httppost.setEntity(new UrlEncodedFormEntity(postData));
			HttpResponse response = httpclient.execute(httppost);
			HttpEntity returnEntity = response.getEntity();

			if (returnEntity != null) rawResponse = EntityUtils.toString(returnEntity);

		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
		} catch (IOException e) {
			// TODO Auto-generated catch block
		}

		return rawResponse;
	}
	
	/**
	 * @param xmlString a string that contains valid xml document markup
	 * @return a {@link Document} assembled from the xmlString
	 */
	public static Document buildDocument(String xmlString) 
	{
		Document xmlDoc = null;
		DocumentBuilderFactory factory;
		DocumentBuilder domBuilder;
		
		factory = DocumentBuilderFactory.newInstance();

		try 
		{
			domBuilder = factory.newDocumentBuilder();

			InputStream responseStream = new ByteArrayInputStream(xmlString.getBytes());
			xmlDoc = domBuilder.parse(responseStream);	
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}

		return xmlDoc;
	}
}
