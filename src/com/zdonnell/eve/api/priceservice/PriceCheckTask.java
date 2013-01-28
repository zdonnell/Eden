package com.zdonnell.eve.api.priceservice;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
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
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.os.AsyncTask;
import android.util.Log;
import android.util.SparseArray;

import com.zdonnell.eve.api.APICallback;

public class PriceCheckTask extends AsyncTask<Integer, Integer, SparseArray<Float>>
{
	private final static String apiURL = "http://api.eve-central.com/api/marketstat";
	
	private final int chunkSize = 25;
	
	private APICallback<SparseArray<Float>> callback;
	
	public PriceCheckTask(APICallback<SparseArray<Float>> callback)
	{
		this.callback = callback;
	}
	
	@Override
	protected SparseArray<Float> doInBackground(Integer... typeIDs)
	{
		SparseArray<Float> values = new SparseArray<Float>(typeIDs.length);
		
		ArrayList<int[]> chunks = chunkIDs(typeIDs);
		
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(apiURL);
		
		for (int[] chunk : chunks)
		{			
			List<NameValuePair> postData = new ArrayList<NameValuePair>(chunk.length);
			for (int id : chunk) postData.add(new BasicNameValuePair("typeid", String.valueOf(id)));
			
			try 
			{
				httppost.setEntity(new UrlEncodedFormEntity(postData));
				HttpResponse response = httpclient.execute(httppost);
				HttpEntity returnEntity = response.getEntity();

				if (returnEntity != null) parseReturnedString(EntityUtils.toString(returnEntity), values);
			} 
			catch (UnsupportedEncodingException e) { e.printStackTrace(); }
			catch (ClientProtocolException e) { e.printStackTrace(); } 
			catch (IOException e) { e.printStackTrace(); }
			
		}
				
		return values;
	}
	
	@Override
	protected void onPostExecute(SparseArray<Float> values)
	{
		callback.onUpdate(values);
	}
	
	private ArrayList<int[]> chunkIDs(Integer[] typeIDs)
	{

		int chunkCount = (int) Math.ceil((float) typeIDs.length / (float) chunkSize);
		Log.d("PRICE CHECK TASK", "CHUNK COUNT: " + chunkCount);

		
		ArrayList<int[]> chunkedIDs = new ArrayList<int[]>(chunkCount);
		
		for (int chunkNumber = 1; chunkNumber <= chunkCount; chunkNumber++)
		{
			int[] chunk;
			
			/* We don't want the last chunk to be of full size if that exceeds the amount of IDs left, properly size it */
			if (chunkNumber * chunkSize > typeIDs.length)
			{
				int truncatedChunkSize = chunkSize - ((chunkNumber * chunkSize) - typeIDs.length);
				chunk = new int[truncatedChunkSize];
				
				Log.d("PRICE CHECK TASK", "TRUNCATED CUNK SIZE: " + truncatedChunkSize);
			}
			else chunk = new int[chunkSize];
			
			for (int i = 0; i < chunk.length; i++)
			{
				chunk[i] = typeIDs[((chunkNumber - 1) * chunkSize) + i];
			}
			
			chunkedIDs.add(chunk);
		}
		
		return chunkedIDs;
	}
	
	private void parseReturnedString(String xmlDocumentString, SparseArray<Float> values)
	{		
		Document xmlDoc = buildDocument(xmlDocumentString);
		
		NodeList typeNodes = xmlDoc.getElementsByTagName("type");
		for (int i = 0; i < typeNodes.getLength(); i++)
		{
			Node typeNode = typeNodes.item(i);
			
			int typeID = Integer.parseInt(typeNode.getAttributes().getNamedItem("id").getNodeValue());
			
			NodeList allStats = typeNode.getLastChild().getChildNodes();
			float value = Float.parseFloat(allStats.item(1).getTextContent());
						
			values.put(typeID, value);
		}
	}
	
	/**
	 * @param xmlString a string that contains valid xml document markup
	 * @return a {@link Document} assembled from the xmlString
	 */
	private Document buildDocument(String xmlString) 
	{
		Document xmlDoc = null;
		DocumentBuilderFactory factory;
		DocumentBuilder domBuilder;
		
		/* remove whitespace in between nodes */
		xmlString = xmlString.replaceAll(">\\s*<", "><");
		
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