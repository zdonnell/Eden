package com.zdonnell.eve.apilink.character;

import android.content.Context;

import com.beimin.eveapi.core.ApiAuth;
import com.beimin.eveapi.core.ApiPage;
import com.beimin.eveapi.core.ApiPath;
import com.beimin.eveapi.eve.character.CharacterInfoParser;
import com.beimin.eveapi.eve.character.CharacterInfoResponse;
import com.beimin.eveapi.exception.ApiException;
import com.zdonnell.eve.apilink.APIExceptionCallback;
import com.zdonnell.eve.apilink.APITask;
import com.zdonnell.eve.database.CharacterInfoData;

/**
 * AsyncTask to retrieve character information and provide it to the specified callback
 * 
 * @author Zach
 *
 */
public class CharacterInfoTask extends APITask<Void, Void, CharacterInfoResponse>
{	
	final private ApiAuth<?> apiAuth;
	
	public CharacterInfoTask(APIExceptionCallback<CharacterInfoResponse> callback, final ApiAuth<?> apiAuth, final Context context)
	{
		super(callback, context, true, new EveApiInteraction<CharacterInfoResponse>()
		{
			@Override
			public CharacterInfoResponse perform() throws ApiException 
			{
				CharacterInfoParser parser = CharacterInfoParser.getInstance();		
				CharacterInfoResponse response = parser.getResponse(apiAuth);

		        new CharacterInfoData(context).setCharacterInfo(response);;
		        
		        return response;
			}
		});
		
		this.apiAuth = apiAuth;
	}
	
	@Override
	public int requestTypeHash() 
	{
		return ApiPath.CHARACTER.getPath().concat(ApiPage.CHARACTER_INFO.getPage()).hashCode();
	}

	@Override
	public CharacterInfoResponse buildResponseFromDatabase() 
	{	
		return new CharacterInfoData(context).getCharacterInfo(apiAuth.getCharacterID().intValue());
	}
}