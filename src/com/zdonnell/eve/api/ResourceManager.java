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
import android.os.AsyncTask;
import android.util.Log;

import com.zdonnell.eve.api.ResourceRequestMonitor.RequestNotActiveException;
import com.zdonnell.eve.apilink.APICallback;

/**
 * Class to handle the storage and correct access of API Resources
 * 
 * @author Zach
 *
 */
public class ResourceManager {

	/* Singleton instance of the Resource Manager */
	private static ResourceManager instance;
	
	/* SQLite database to stored cached API resources */
	private CacheDatabase cacheDatabase;
	
	private static Context context;
	
	/**
	 * Singleton access method
	 * 
	 * @param newContext
	 * @return
	 */
	public static ResourceManager getInstance(Context newContext)
	{
		if (instance == null)
		{
			instance = new ResourceManager();
			instance.cacheDatabase = new CacheDatabase(newContext);
			
			context = newContext;
		}
		return instance;
	}
	
	/**
	 * Call to request an API resource.  Using the {@link APICallback} provided in the
	 * {@link APIRequestWrapper} this will first check the database for an existing cached
	 * copy of the resource.<br><br>
	 * 
	 * If a cache of the resource exists, the UI {@link APICallback} will be provided the cached
	 * copy.<br><br>
	 * 
	 * If there is no cache of the resource, or the cache is expired, the API server will then be
	 * queried, and if the resource is successfully obtained, the UI {@link APICallback} will
	 * once again be supplied with the now current resource.
	 * 
	 * @param rw a {@link APIRequestWrapper} to bundle the request arguments
	 */
	@SuppressWarnings("unchecked")	
	public void get(APIRequestWrapper rw)
	{		
		if (cacheDatabase.cacheExists(rw.resourceURL, rw.uniqueIDs)) 
		{
			new CacheDatabaseQuery(rw).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		}
		else
		{
			rw.apiCallback.updateState(APICallback.STATE_CACHED_RESPONSE_NOT_FOUND);
			new APIServerQuery(rw).execute();
		}
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

		} 
		catch (ClientProtocolException e) { e.printStackTrace(); }
		catch (IOException e) { e.printStackTrace(); }
				
		if (rawResponse != null) rawResponse = rawResponse.replaceAll(">\\s*<", "><");

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
	
	private class CacheDatabaseQuery extends AsyncTask<String, Integer, Document>
	{
		private APIRequestWrapper rw;
		
		public CacheDatabaseQuery(APIRequestWrapper rw)
		{
			this.rw = rw;
		}
		
		@Override
		protected Document doInBackground(String... params) 
		{
			String cachedResource = cacheDatabase.getCachedResource(rw.resourceURL, rw.uniqueIDs);	
			return buildDocument(cachedResource);
		}
		
		@SuppressWarnings("unchecked")
		@Override
		protected void onPostExecute(Document queriedResource)
		{
			rw.apiCallback.onUpdate(rw.parser.parse(queriedResource));
			if (cacheDatabase.cacheExpired(rw.resourceURL, rw.uniqueIDs)) 
			{
				rw.apiCallback.updateState(APICallback.STATE_CACHED_RESPONSE_ACQUIRED_INVALID);
				new APIServerQuery(rw).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			}
			else rw.apiCallback.updateState(APICallback.STATE_CACHED_RESPONSE_ACQUIRED_VALID);
		}
		
	}
	
	/**
	 * Class to Asynchronously query the API Server for resource requests
	 * 
	 * @author Zach
	 *
	 */
	private class APIServerQuery extends AsyncTask<String, Integer, Document> {

		APIRequestWrapper rw;
		
		ResourceRequestMonitor.Request request;
		
		public APIServerQuery(APIRequestWrapper rw) 
		{ 
			request = new ResourceRequestMonitor.Request(0, this);
			ResourceRequestMonitor.getInstance(context).registerRequest(request);
						
			this.rw = rw;
		}
		
		@Override
		protected Document doInBackground(String... params) 
		{
			ArrayList<NameValuePair> assembledPOSTData = new ArrayList<NameValuePair>();
			for (NameValuePair nvp : rw.uniqueIDs) assembledPOSTData.add(nvp);
			
			if (rw.credentials != null)
			{
				assembledPOSTData.add(new BasicNameValuePair("keyID", String.valueOf(rw.credentials.keyID)));
				assembledPOSTData.add(new BasicNameValuePair("vCode", rw.credentials.verificationCode));
			}
			
			String queriedResource = queryResource(rw.resourceURL, assembledPOSTData);
			if (queriedResource != null)
			{
				NameValuePair[] newUniqueIDs = rw.uniqueIDs;
				if (rw.uniqueIDs.length == 0 && rw.credentials != null) 
				{
					newUniqueIDs = new BasicNameValuePair[] { new BasicNameValuePair("keyID", String.valueOf(rw.credentials.keyID))};
				}
				cacheDatabase.updateCache(rw.resourceURL, newUniqueIDs, queriedResource);
					
				return buildDocument(queriedResource);
			}
			else return null;
		}
		
		@SuppressWarnings("unchecked")
		@Override
		protected void onPostExecute(Document queriedResource)
		{
			try { ResourceRequestMonitor.getInstance(context).requestCompleted(request); } 
			catch (RequestNotActiveException e) { e.printStackTrace(); }
			
			if (queriedResource != null) 
			{
				rw.apiCallback.onUpdate(rw.parser.parse(queriedResource));
				rw.apiCallback.updateState(APICallback.STATE_SERVER_RESPONSE_ACQUIRED);
			}
			else rw.apiCallback.updateState(APICallback.STATE_SERVER_RESPONSE_FAILED);
		}
	}
	
	/**
	 * A Static Helper class to bundle arguments needed for resource requests.
	 * 
	 * @author Zach
	 *
	 */
	public static class APIRequestWrapper
	{
		@SuppressWarnings("rawtypes")
		final APICallback apiCallback; 
		@SuppressWarnings("rawtypes")
		final APIObject.APIParser parser;
		final APICredentials credentials;
		final String resourceURL;
		final boolean refresh;
		final NameValuePair[] uniqueIDs;
		
		public APIRequestWrapper(@SuppressWarnings("rawtypes") APICallback apiCallback, @SuppressWarnings("rawtypes") APIObject.APIParser parser, APICredentials credentials, String resourceURL, boolean refresh, NameValuePair ...uniqueIDs)
		{
			this.apiCallback = apiCallback;
			this.parser = parser;
			this.credentials = credentials;
			this.resourceURL = resourceURL;
			this.refresh = refresh;
			this.uniqueIDs = uniqueIDs;
		}
	}
}
