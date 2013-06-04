package com.zdonnell.eve.apilink.eve;

import android.os.AsyncTask;

import com.beimin.eveapi.core.ApiPage;
import com.beimin.eveapi.core.ApiPath;
import com.beimin.eveapi.eve.conquerablestationlist.ConquerableStationListParser;
import com.beimin.eveapi.eve.conquerablestationlist.StationListResponse;
import com.beimin.eveapi.exception.ApiException;
import com.zdonnell.eve.apilink.APIExceptionCallback;
import com.zdonnell.eve.apilink.IApiTask;

public class ConquerableStationsTask extends AsyncTask<Void, Void, StationListResponse> implements IApiTask<StationListResponse>
{
	private APIExceptionCallback<StationListResponse> callback;
	
	private boolean apiExceptionOccured = false;
	private ApiException exception;
				
	public ConquerableStationsTask(APIExceptionCallback<StationListResponse> callback)
	{
		this.callback = callback;
		callback.updateState(APIExceptionCallback.STATE_CACHED_RESPONSE_NOT_FOUND);
	}
	
	@Override
	protected StationListResponse doInBackground(Void... params)
	{					
		ConquerableStationListParser parser = ConquerableStationListParser.getInstance();		
		StationListResponse response = null;
		
        try 
        { 
        	response = parser.getResponse();
        }
		catch (ApiException e) 
		{
			apiExceptionOccured = true;
			exception = e;
		}
        
        return response;
	}
	
	@Override
	protected void onPostExecute(StationListResponse response) 
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
	
	@Override
	public int requestTypeHash() 
	{
		return ApiPath.EVE.getPath().concat(ApiPage.CONQUERABLE_STATION_LIST.getPage()).hashCode();
	}

	@Override
	public StationListResponse buildResponseFromDatabase() 
	{
		return null;
	}
}
