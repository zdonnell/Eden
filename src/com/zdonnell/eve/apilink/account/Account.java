package com.zdonnell.eve.apilink.account;

import android.os.AsyncTask;

import com.beimin.eveapi.account.characters.CharactersResponse;
import com.beimin.eveapi.core.ApiAuth;
import com.zdonnell.eve.apilink.APIExceptionCallback;

public class Account 
{
	private ApiAuth<?> apiAuth;
	
	public Account(ApiAuth<?> apiAuth)
	{
		this.apiAuth = apiAuth;
	}
	
	public void getCharacters(APIExceptionCallback<CharactersResponse> callback)
	{
		new CharactersTask(callback, apiAuth).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR); 
	}
}
