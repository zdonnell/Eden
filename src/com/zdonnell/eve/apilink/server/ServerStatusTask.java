package com.zdonnell.eve.apilink.server;

import android.os.AsyncTask;

import com.beimin.eveapi.exception.ApiException;
import com.beimin.eveapi.server.ServerStatusParser;
import com.beimin.eveapi.server.ServerStatusResponse;
import com.zdonnell.eve.apilink.APIExceptionCallback;

public class ServerStatusTask extends AsyncTask<Void, Void, ServerStatusResponse>
{
	private APIExceptionCallback<ServerStatusResponse> callback;
	
	private boolean apiExceptionOccured = false;
	private ApiException exception;
	
	public ServerStatusTask(APIExceptionCallback<ServerStatusResponse> callback)
	{
		this.callback = callback;
		
		// We don't cache server status, so just notify the callback that we didn't find any
		callback.updateState(APIExceptionCallback.STATE_CACHED_RESPONSE_NOT_FOUND);
	}
	
	@Override
	protected ServerStatusResponse doInBackground(Void... params)
	{
		ServerStatusParser parser = ServerStatusParser.getInstance();		
		ServerStatusResponse response = null;
		
        try { response = parser.getServerStatus(); }
		catch (ApiException e) 
		{
			apiExceptionOccured = true;
			exception = e;
		}
        
        return response;
	}
	
	@Override
	protected void onPostExecute(ServerStatusResponse response) 
	{
		if (apiExceptionOccured) 
		{
			callback.onError(response, exception);
			callback.updateState(APIExceptionCallback.STATE_SERVER_RESPONSE_FAILED);
		}
		else 
		{
			callback.onUpdate(response);
			callback.updateState(APIExceptionCallback.STATE_SERVER_RESPONSE_ACQUIRED);
		}
    }
}
