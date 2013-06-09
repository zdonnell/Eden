package com.zdonnell.eve.apilink.account;

import android.content.Context;

import com.beimin.eveapi.account.characters.CharactersParser;
import com.beimin.eveapi.account.characters.CharactersResponse;
import com.beimin.eveapi.core.ApiAuth;
import com.beimin.eveapi.exception.ApiException;
import com.zdonnell.eve.apilink.APIExceptionCallback;
import com.zdonnell.eve.apilink.APITask;

public class CharactersTask extends APITask<Void, Void, CharactersResponse>
{
	public CharactersTask(APIExceptionCallback<CharactersResponse> callback, final Context context, final ApiAuth<?> apiAuth)
	{
		super(callback, context, false, new EveApiInteraction<CharactersResponse>()
		{
			@Override
			public CharactersResponse perform() throws ApiException 
			{
				CharactersParser parser = CharactersParser.getInstance();		
				return parser.getResponse(apiAuth);
			}
			
		});
	}

	@Override
	protected int requestTypeHash() { return 0; /* no hash needed, not using cache database */ }

	@Override
	protected CharactersResponse buildResponseFromDatabase() { return null; /* not using cache database */ }
}
