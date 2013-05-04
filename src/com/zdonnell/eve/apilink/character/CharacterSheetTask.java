package com.zdonnell.eve.apilink.character;

import java.util.Set;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.beimin.eveapi.character.sheet.ApiSkill;
import com.beimin.eveapi.character.sheet.CharacterSheetParser;
import com.beimin.eveapi.character.sheet.CharacterSheetResponse;
import com.beimin.eveapi.core.ApiAuth;
import com.beimin.eveapi.core.ApiPage;
import com.beimin.eveapi.core.ApiPath;
import com.beimin.eveapi.exception.ApiException;
import com.zdonnell.eve.apilink.APIExceptionCallback;
import com.zdonnell.eve.apilink.CacheDatabase;
import com.zdonnell.eve.apilink.IApiTask;
import com.zdonnell.eve.database.SkillsData;

/**
 * AsyncTask to retrieve character sheet information and provide it to the specified callback
 * 
 * @author Zach
 *
 */
public class CharacterSheetTask extends AsyncTask<Void, Void, CharacterSheetResponse> implements IApiTask<CharacterSheetResponse>
{	
	private CacheDatabase cacheDatabase;
	
	private APIExceptionCallback<CharacterSheetResponse> callback;
	private ApiAuth<?> apiAuth;
	private Context context;
	
	private boolean apiExceptionOccured = false;
	private ApiException exception;
	
	private boolean cacheExists = false, cacheValid = false;
	
	private CharacterSheetResponse cachedData;
		
	/**
	 * Constructor
	 * 
	 * @param callback
	 * @param apiAuth
	 * @param context
	 */
	public CharacterSheetTask(APIExceptionCallback<CharacterSheetResponse> callback, ApiAuth<?> apiAuth, Context context)
	{
		this.callback = callback;
		this.apiAuth = apiAuth;
		this.context = context;
		
		cacheDatabase = new CacheDatabase(context);
	}
	
	@Override
	protected CharacterSheetResponse doInBackground(Void... params)
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
			
			CharacterSheetParser parser = CharacterSheetParser.getInstance();		
			CharacterSheetResponse response = null;
			
	        try 
	        { 
	        	response = parser.getResponse(apiAuth);
	        	cacheDatabase.updateCache(requestHash, response.getCachedUntil());
	        	
	        	new SkillsData(context).storeSkills((int) response.getCharacterID(), response.getSkills());
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
	protected void onPostExecute(CharacterSheetResponse response) 
	{	
		// We can arrive here one of two ways, if the cache was still valid, or if it was invalid
		// and a server response was acquired, check which it is.
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
		callback.updateState(APIExceptionCallback.STATE_CACHED_RESPONSE_ACQUIRED_INVALID);
		callback.onUpdate(cachedData);
	}
	
	@Override
	public int requestTypeHash() 
	{
		return ApiPath.CHARACTER.getPath().concat(ApiPage.CHARACTER_SHEET.getPage()).hashCode();
	}

	@Override
	public CharacterSheetResponse buildResponseFromDatabase() 
	{
		CharacterSheetResponse response = new CharacterSheetResponse();
		
		// Get skills
		SkillsData skillsData = new SkillsData(context);
		Set<ApiSkill> skills = skillsData.getSkills(apiAuth.getCharacterID().intValue());
		for (ApiSkill s : skills) response.addSkill(s);
		
		return response;
	}
}

