package com.zdonnell.eve.apilink.character;

import java.util.Set;

import android.content.Context;
import android.os.AsyncTask;

import com.beimin.eveapi.character.mail.messages.ApiMailMessage;
import com.beimin.eveapi.character.mail.messages.MailMessagesParser;
import com.beimin.eveapi.character.mail.messages.MailMessagesResponse;
import com.beimin.eveapi.core.ApiAuth;
import com.beimin.eveapi.core.ApiPage;
import com.beimin.eveapi.core.ApiPath;
import com.beimin.eveapi.exception.ApiException;
import com.zdonnell.eve.apilink.APIExceptionCallback;
import com.zdonnell.eve.apilink.CacheDatabase;
import com.zdonnell.eve.apilink.IApiTask;
import com.zdonnell.eve.database.MailHeadersData;

public class MailHeadersTask extends AsyncTask<Void, Void, MailMessagesResponse> implements IApiTask<MailMessagesResponse>
{		
	private CacheDatabase cacheDatabase;
	
	private APIExceptionCallback<MailMessagesResponse> callback;
	private ApiAuth<?> apiAuth;
	
	private boolean apiExceptionOccured = false;
	private ApiException exception;
			
	private MailMessagesResponse cachedData;
	private MailHeadersData mailHeadersData;
	
	private boolean cacheExists = false, cacheValid = false;
		
	/**
	 * Constructor
	 * 
	 * @param callback
	 * @param apiAuth
	 * @param context
	 */
	public MailHeadersTask(APIExceptionCallback<MailMessagesResponse> callback, ApiAuth<?> apiAuth, Context context)
	{
		this.callback = callback;
		this.apiAuth = apiAuth;
		
		cacheDatabase = new CacheDatabase(context);
		mailHeadersData = new MailHeadersData(context);
	}
	
	@Override
	protected MailMessagesResponse doInBackground(Void... params)
	{
		int requestHash = apiAuth.hashCode() + requestTypeHash();
		
		cacheValid = cacheDatabase.cacheValid(requestHash);
		cacheExists = cacheDatabase.cacheExists(requestHash);
				
		if (cacheValid)
		{
			return buildResponseFromDatabase();
		}
		else
		{
			// The cache is out of date (invalid) but load it anyway while we contact the API server
			if (cacheExists) 
			{
				cachedData = buildResponseFromDatabase();
				publishProgress();
			}
			else callback.updateState(APIExceptionCallback.STATE_CACHED_RESPONSE_NOT_FOUND);
 	
			MailMessagesParser parser = MailMessagesParser.getInstance();		
			MailMessagesResponse response = null;
						
	        try 
	        { 
	        	response = parser.getResponse(apiAuth);
	        		        	
	        	cacheDatabase.updateCache(requestHash, response.getCachedUntil());
	        	mailHeadersData.setMailHeaders(apiAuth.getCharacterID().intValue(), response.getAll());
	        	
	        	// combine cached mail headers and the new mail headers into the response
	        	if (cacheExists) for (ApiMailMessage cachedMessage : cachedData.getAll()) response.add(cachedMessage);
	        }
			catch (ApiException e) 
			{
				apiExceptionOccured = true;
				exception = e;
			}
	        	        
	        return response;
		}
	}
	
	@Override
	protected void onPostExecute(MailMessagesResponse response) 
	{	
		if (cacheValid)
		{
			callback.updateState(APIExceptionCallback.STATE_CACHED_RESPONSE_ACQUIRED_VALID);
			callback.onUpdate(response);
		}
		else
		{
			if (apiExceptionOccured)
			{
				callback.updateState(APIExceptionCallback.STATE_SERVER_RESPONSE_FAILED);
				callback.onError(response, exception);
			}
			else
			{
				callback.updateState(APIExceptionCallback.STATE_SERVER_RESPONSE_ACQUIRED);
				callback.onUpdate(response);
			}
		}
    }

	@Override
	protected void onProgressUpdate(Void... progress)
	{		
		callback.onUpdate(cachedData);
	}
	
	public int requestTypeHash() 
	{
		return ApiPath.CHARACTER.getPath().concat(ApiPage.ASSET_LIST.getPage()).hashCode();
	}

	public MailMessagesResponse buildResponseFromDatabase() 
	{		
		MailMessagesResponse response = new MailMessagesResponse();
		
		Set<ApiMailMessage> mailHeaders = mailHeadersData.getMailHeaders(apiAuth.getCharacterID().intValue());
		for (ApiMailMessage mailHeader : mailHeaders) response.add(mailHeader);
				
		return response;
	}
}

