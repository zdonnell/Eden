package com.zdonnell.eve.apilink.server;

import android.content.Context;
import android.os.AsyncTask;

import com.beimin.eveapi.exception.ApiException;
import com.beimin.eveapi.server.ServerStatusParser;
import com.beimin.eveapi.server.ServerStatusResponse;
import com.zdonnell.eve.apilink.APIExceptionCallback;

public class Server 
{
	private Context context;
	
	public Server(Context context)
	{
		this.context = context;
	}
	
	public void status(APIExceptionCallback<ServerStatusResponse> callback)
	{
		new ServerStatusTask(callback).execute(); 
	}
}
