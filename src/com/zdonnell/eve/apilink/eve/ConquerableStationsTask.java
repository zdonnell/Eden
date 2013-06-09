package com.zdonnell.eve.apilink.eve;

import android.content.Context;

import com.beimin.eveapi.eve.conquerablestationlist.ConquerableStationListParser;
import com.beimin.eveapi.eve.conquerablestationlist.StationListResponse;
import com.beimin.eveapi.exception.ApiException;
import com.zdonnell.eve.apilink.APIExceptionCallback;
import com.zdonnell.eve.apilink.APITask;

public class ConquerableStationsTask extends APITask<Void, Void, StationListResponse>
{		
	public ConquerableStationsTask(APIExceptionCallback<StationListResponse> callback, final Context context)
	{
		super(callback, context, false, new EveApiInteraction<StationListResponse>()
		{
			@Override
			public StationListResponse perform() throws ApiException 
			{
				ConquerableStationListParser parser = ConquerableStationListParser.getInstance();		
		        return parser.getResponse();
			}
		});
	}
	
	public int requestTypeHash() { return 0; /* cache not used */ }

	public StationListResponse buildResponseFromDatabase() { return null; /* cache not used */ }
}
