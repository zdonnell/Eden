package com.zdonnell.eve.staticdata.api;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.util.SparseArray;

import com.zdonnell.eve.api.APICallback;
import com.zdonnell.eve.helpers.Tools;

public class StaticData {

	private StaticTypeDatabase typeDatabase;
	
	public StaticData(Context context)
	{
		typeDatabase = new StaticTypeDatabase(context);
	}
	
	public void getTypeInfo(APICallback<SparseArray<TypeInfo>> callback, Integer... typeIDs)
	{
		new StaticTypeDatabaseRequest(callback).execute(Tools.stripDuplicateIDs(typeIDs));	
	}
	
	
	private class StaticTypeDatabaseRequest extends AsyncTask<Integer, Integer, SparseArray<TypeInfo>>
	{
		private APICallback<SparseArray<TypeInfo>> onCompleteRequestCallback;
		
		private Integer[] requestedTypeIDs;
		
		public StaticTypeDatabaseRequest(APICallback<SparseArray<TypeInfo>> callback)
		{
			onCompleteRequestCallback = callback;
		}
		
		@Override
		protected SparseArray<TypeInfo> doInBackground(Integer... typeIDs) 
		{
			requestedTypeIDs = typeIDs;
			return typeDatabase.getTypeInfo(requestedTypeIDs);
		}
		
		@Override
		protected void onPostExecute(SparseArray<TypeInfo> storedTypes)
		{			
			int amountOfTypesNotFound = requestedTypeIDs.length - storedTypes.size();
			
			/* compare with passed typeIDs array */
			if (amountOfTypesNotFound > 0)
			{
				Integer[] unobtainedTypeIDs = new Integer[amountOfTypesNotFound];
				int unobtainedTypeIDsIndex = 0;
				
				for (int id : requestedTypeIDs)
				{
					if (storedTypes.get(id) == null)
					{
						unobtainedTypeIDs[unobtainedTypeIDsIndex] = id;
						++unobtainedTypeIDsIndex;
					}
				}
								
				/* Request the rest from the server, and let that AsyncTask finish the overall request */
				new StaticTypeServerRequest(storedTypes, onCompleteRequestCallback).execute(unobtainedTypeIDs);
			}
			/* All requested information was obtained from the database, tell the requesting
			 * callback
			 */
			else if (amountOfTypesNotFound == 0)
			{
				onCompleteRequestCallback.onUpdate(storedTypes);
			}
			
		}
	}
	
	
	private class StaticTypeServerRequest extends AsyncTask<Integer, Integer, SparseArray<TypeInfo>>
	{
		private final String serverURL = "http://zdonnell.com/eve/api/invTypes.php";
		
		private SparseArray<TypeInfo> typeInfoSetFromDatabase;
		
		private APICallback<SparseArray<TypeInfo>> onCompleteRequestCallback;
		
		public StaticTypeServerRequest(SparseArray<TypeInfo> typeInfoSetFromDatabase, APICallback<SparseArray<TypeInfo>> onCompleteRequestCallback)
		{
			this.typeInfoSetFromDatabase = typeInfoSetFromDatabase;
			this.onCompleteRequestCallback = onCompleteRequestCallback;
		}
		
		@Override
		protected SparseArray<TypeInfo> doInBackground(Integer... typeIDs) 
		{
			SparseArray<TypeInfo> typeInfoSet = new SparseArray<TypeInfo>(typeIDs.length);
			
			String serverRawResponse = getRawResponse(typeIDs);
			try 
			{
				JSONArray jsonResponse = new JSONArray(serverRawResponse);

				for (int i = 0; i < jsonResponse.length(); ++i)
				{
					TypeInfo typeInfo = new TypeInfo();
					JSONObject typeInfoObject = jsonResponse.getJSONObject(i);
					
					try { typeInfo.typeID = typeInfoObject.getInt("typeID"); }
					catch (JSONException e) { }
					try { typeInfo.groupID = typeInfoObject.getInt("groupID"); }
					catch (JSONException e) { }
					try { typeInfo.marketGroupID = typeInfoObject.getInt("marketGroupID"); }
					catch (JSONException e) { }
					try { typeInfo.typeName = typeInfoObject.getString("typeName"); }
					catch (JSONException e) { }
					try { typeInfo.description = typeInfoObject.getString("description"); }
					catch (JSONException e) { }
					
					typeInfoSet.put(typeInfo.typeID, typeInfo);
				}
			} 
			catch (JSONException e) { }
			
			if (typeInfoSet.size() > 0) typeDatabase.insertTypeInfo(typeInfoSet);
			
			return typeInfoSet;
		}
		
		@Override
		protected void onPostExecute(SparseArray<TypeInfo> queriedTypes)
		{
			/* merge the results from the server, into the original SparseArray filled by the database */
			for (int i = 0; i < queriedTypes.size(); ++i)
			{
				typeInfoSetFromDatabase.put(queriedTypes.keyAt(i), queriedTypes.valueAt(i));
			}
			
			onCompleteRequestCallback.onUpdate(typeInfoSetFromDatabase);
		}
		
		/**
		 * Obtains the raw JSON response from the server
		 * 
		 * @param typeIDs an Array of TypeIDs to check
		 * @return the raw response from the server as a String
		 */
		private String getRawResponse(Integer... typeIDs)
		{
			String queryString = "?typeID=";
			for (int i = 0; i < typeIDs.length; ++i) 
			{
				queryString += typeIDs[i];
				if (i < typeIDs.length - 1) queryString += ",";
			}
			
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost(serverURL + queryString);

			String rawResponse = null;

			try 
			{
				HttpResponse response = httpclient.execute(httppost);
				HttpEntity returnEntity = response.getEntity();

				if (returnEntity != null) rawResponse = EntityUtils.toString(returnEntity);

			} 
			catch (ClientProtocolException e) { e.printStackTrace(); }
			catch (IOException e) { e.printStackTrace(); }
			
			return rawResponse;
		}
	}
}
