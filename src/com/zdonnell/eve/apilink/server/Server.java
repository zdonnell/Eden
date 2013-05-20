package com.zdonnell.eve.apilink.server;

import com.beimin.eveapi.server.ServerStatusResponse;
import com.zdonnell.eve.apilink.APIExceptionCallback;

public class Server 
{	
	public void status(APIExceptionCallback<ServerStatusResponse> callback)
	{
		new ServerStatusTask(callback).execute(); 
	}
}
