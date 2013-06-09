package com.zdonnell.eve.apilink.server;

import android.content.Context;

import com.beimin.eveapi.exception.ApiException;
import com.beimin.eveapi.server.ServerStatusParser;
import com.beimin.eveapi.server.ServerStatusResponse;
import com.zdonnell.eve.apilink.APIExceptionCallback;
import com.zdonnell.eve.apilink.APITask;

public class ServerStatusTask extends APITask<Void, Void, ServerStatusResponse>
{
	public ServerStatusTask(APIExceptionCallback<ServerStatusResponse> callback, Context context)
	{
		super(callback, context, false, new EveApiInteraction<ServerStatusResponse>()
		{
			@Override
			public ServerStatusResponse perform() throws ApiException 
			{
				ServerStatusParser parser = ServerStatusParser.getInstance();		
				return parser.getServerStatus();
			}
		});
	}

	public int requestTypeHash() { return 0; /* cache not used */ }

	public ServerStatusResponse buildResponseFromDatabase() { return null; /* cache not used */ }
}
