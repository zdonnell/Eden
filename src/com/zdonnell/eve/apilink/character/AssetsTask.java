package com.zdonnell.eve.apilink.character;

import java.util.Collection;

import android.content.Context;

import com.beimin.eveapi.character.assetlist.AssetListParser;
import com.beimin.eveapi.core.ApiAuth;
import com.beimin.eveapi.core.ApiPage;
import com.beimin.eveapi.core.ApiPath;
import com.beimin.eveapi.exception.ApiException;
import com.beimin.eveapi.shared.assetlist.AssetListResponse;
import com.beimin.eveapi.shared.assetlist.EveAsset;
import com.zdonnell.eve.apilink.APIExceptionCallback;
import com.zdonnell.eve.apilink.APITask;
import com.zdonnell.eve.database.AssetsData;

/**
 * AsyncTask to retrieve character asset information and provide it to the specified callback
 * 
 * @author Zach
 *
 */
public class AssetsTask extends APITask<Void, Void, AssetListResponse>
{		
	final ApiAuth<?> apiAuth;
	
	public AssetsTask(APIExceptionCallback<AssetListResponse> callback, final ApiAuth<?> apiAuth, final Context context)
	{
		super(callback, context, true, new EveApiInteraction<AssetListResponse>()
		{
			@Override
			public AssetListResponse perform() throws ApiException 
			{
				AssetListParser parser = AssetListParser.getInstance();		
				AssetListResponse response = parser.getResponse(apiAuth);
 		        	
		        new AssetsData(context).setAssets(apiAuth.getCharacterID().intValue(), response.getAll());
		        
		        return response;
			}
		});
		
		this.apiAuth = apiAuth;
	}
	
	@Override
	public int requestTypeHash() 
	{
		return ApiPath.CHARACTER.getPath().concat(ApiPage.ASSET_LIST.getPage()).hashCode();
	}

	@Override
	public AssetListResponse buildResponseFromDatabase() 
	{		
		AssetListResponse response = new AssetListResponse();
		
		Collection<EveAsset<EveAsset<?>>> assets = new AssetsData(context).getAssets(apiAuth.getCharacterID().intValue());
		for (EveAsset<EveAsset<?>> asset : assets) response.add(asset);
				
		return response;
	}
}

