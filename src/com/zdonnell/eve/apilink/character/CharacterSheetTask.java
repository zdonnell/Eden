package com.zdonnell.eve.apilink.character;

import java.util.Set;

import android.content.Context;
import android.os.AsyncTask;

import com.beimin.eveapi.character.sheet.ApiSkill;
import com.beimin.eveapi.character.sheet.CharacterSheetParser;
import com.beimin.eveapi.character.sheet.CharacterSheetResponse;
import com.beimin.eveapi.core.ApiAuth;
import com.beimin.eveapi.core.ApiPage;
import com.beimin.eveapi.core.ApiPath;
import com.beimin.eveapi.exception.ApiException;
import com.beimin.eveapi.server.ServerStatusParser;
import com.beimin.eveapi.server.ServerStatusResponse;
import com.zdonnell.eve.apilink.CacheDatabase;
import com.zdonnell.eve.apilink.APIExceptionCallback;
import com.zdonnell.eve.apilink.IApiTask;
import com.zdonnell.eve.database.SkillsData;

public class CharacterSheetTask extends AsyncTask<Void, Void, CharacterSheetResponse> implements IApiTask<CharacterSheetResponse>
{	
	private CacheDatabase cacheDatabase;
	
	private APIExceptionCallback<CharacterSheetResponse> callback;
	private ApiAuth<?> apiAuth;
	private Context context;
	
	private boolean apiExceptionOccured = false;
	private ApiException exception;
		
	public CharacterSheetTask(APIExceptionCallback<CharacterSheetResponse> callback, ApiAuth<?> apiAuth, Context context)
	{
		this.callback = callback;
		this.apiAuth = apiAuth;
		this.context = context;
	}
	
	@Override
	protected CharacterSheetResponse doInBackground(Void... params)
	{
		boolean cacheValid = cacheDatabase.cacheValid(apiAuth.hashCode() + requestTypeHash());
		
		if (cacheValid)
		{
			return buildResponseFromDatabase();
		}
		else
		{
			CharacterSheetParser parser = CharacterSheetParser.getInstance();		
			CharacterSheetResponse response = null;
			
	        try { response = parser.getResponse(apiAuth); }
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
		if (apiExceptionOccured) 
		{
			callback.onError(response, exception);
			//callback.updateState(APIExceptionCallback.STATE_SERVER_RESPONSE_ACQUIRED);
		}
		else 
		{
			callback.onUpdate(response);
			//callback.updateState(APIExceptionCallback.STATE_SERVER_RESPONSE_FAILED);
		}
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
		
		SkillsData skillsData = new SkillsData(context);
		Set<ApiSkill> skills = skillsData.getSkills(apiAuth.getCharacterID().intValue());
		for (ApiSkill s : skills) response.addSkill(s);
		
		return response;
	}
}

