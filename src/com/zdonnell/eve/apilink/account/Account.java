package com.zdonnell.eve.apilink.account;

import android.content.Context;
import android.os.AsyncTask;

import com.beimin.eveapi.account.characters.CharactersResponse;
import com.beimin.eveapi.core.ApiAuth;
import com.zdonnell.eve.apilink.APIExceptionCallback;

public class Account 
{
	final private ApiAuth<?> apiAuth;
	final private Context context;
	
	public Account(ApiAuth<?> apiAuth, Context context)
	{
		this.apiAuth = apiAuth;
		this.context = context;
	}
	
	public void getCharacters(APIExceptionCallback<CharactersResponse> callback)
	{
		new CharactersTask(callback, context, apiAuth).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR); 
	}
}
