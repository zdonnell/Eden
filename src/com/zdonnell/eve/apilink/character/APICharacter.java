package com.zdonnell.eve.apilink.character;

import android.content.Context;
import android.os.AsyncTask;

import com.beimin.eveapi.character.sheet.CharacterSheetResponse;
import com.beimin.eveapi.core.ApiAuth;
import com.beimin.eveapi.exception.ApiException;
import com.beimin.eveapi.server.ServerStatusParser;
import com.beimin.eveapi.server.ServerStatusResponse;
import com.zdonnell.eve.apilink.APIExceptionCallback;

public class APICharacter 
{
	private Context context;
	private ApiAuth<?> apiAuth;
	
	public APICharacter(Context context, ApiAuth<?> apiAuth)
	{
		this.context = context;
		this.apiAuth = apiAuth;
	}
	
	public void getCharacterSheet(APIExceptionCallback<CharacterSheetResponse> callback)
	{
		new CharacterSheetTask(callback, apiAuth, context).execute(); 
	}
}
