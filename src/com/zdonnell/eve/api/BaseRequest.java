package com.zdonnell.eve.api;

import java.io.IOException;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;


public abstract class BaseRequest {
	
	protected APICredentials credentials;
	
	protected int actorID;
	
	/**
	 * the time that the resource is cached until, based on the last successful
	 * request
	 */
	private String cachedTime;
	
	protected String makeHTTPRequest(String url, List<NameValuePair> postData)
	{
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(url);
		
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
	
	public String cachedTime() 
	{
		return cachedTime;
	}
}
