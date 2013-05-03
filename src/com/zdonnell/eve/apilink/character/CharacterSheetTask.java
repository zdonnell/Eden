package com.zdonnell.eve.apilink.character;

import android.content.Context;
import android.os.AsyncTask;

import com.beimin.eveapi.character.sheet.CharacterSheetParser;
import com.beimin.eveapi.character.sheet.CharacterSheetResponse;
import com.beimin.eveapi.core.ApiAuth;
import com.beimin.eveapi.exception.ApiException;
import com.beimin.eveapi.server.ServerStatusParser;
import com.beimin.eveapi.server.ServerStatusResponse;
import com.zdonnell.eve.apilink.CacheDatabase;
import com.zdonnell.eve.apilink.APIExceptionCallback;

public class CharacterSheetTask extends AsyncTask<Void, Void, CharacterSheetResponse>
{
	private APIExceptionCallback<ServerStatusResponse> callback;
	private ApiAuth<?> apiAuth;
	
	private boolean apiExceptionOccured = false;
	private ApiException exception;
	
	public CharacterSheetTask(APIExceptionCallback<ServerStatusResponse> callback, ApiAuth<?> apiAuth)
	{
		this.callback = callback;
	}
	
	@Override
	protected CharacterSheetResponse doInBackground(Void... params)
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
	
	@Override
	protected void onPostExecute(CharacterSheetResponse response) 
	{
		if (apiExceptionOccured) 
		{
			
		}
		else 
		{
			
		}
    }
}

