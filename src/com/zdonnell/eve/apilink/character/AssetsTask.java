package com.zdonnell.eve.apilink.character;

import java.util.Collection;

import android.content.Context;
import android.os.AsyncTask;

import com.beimin.eveapi.character.assetlist.AssetListParser;
import com.beimin.eveapi.core.ApiAuth;
import com.beimin.eveapi.core.ApiPage;
import com.beimin.eveapi.core.ApiPath;
import com.beimin.eveapi.exception.ApiException;
import com.beimin.eveapi.shared.assetlist.AssetListResponse;
import com.beimin.eveapi.shared.assetlist.EveAsset;
import com.zdonnell.eve.apilink.APIExceptionCallback;
import com.zdonnell.eve.apilink.CacheDatabase;
import com.zdonnell.eve.apilink.IApiTask;
import com.zdonnell.eve.database.AssetsData;

/**
 * AsyncTask to retrieve character asset information and provide it to the specified callback
 * 
 * @author Zach
 *
 */
public class AssetsTask extends AsyncTask<Void, Void, AssetListResponse> implements IApiTask<AssetListResponse>
{		
	private CacheDatabase cacheDatabase;
	
	private APIExceptionCallback<AssetListResponse> callback;
	private ApiAuth<?> apiAuth;
	
	private boolean apiExceptionOccured = false;
	private ApiException exception;
			
	private AssetListResponse cachedData;
	private AssetsData assetsDatabase;
	
	private boolean cacheExists = false, cacheValid = false;
		
	/**
	 * Constructor
	 * 
	 * @param callback
	 * @param apiAuth
	 * @param context
	 */
	public AssetsTask(APIExceptionCallback<AssetListResponse> callback, ApiAuth<?> apiAuth, Context context)
	{
		this.callback = callback;
		this.apiAuth = apiAuth;
		
		cacheDatabase = new CacheDatabase(context);
		assetsDatabase = new AssetsData(context);
	}
	
	@Override
	protected AssetListResponse doInBackground(Void... params)
	{
		int requestHash = apiAuth.hashCode() + requestTypeHash();
		
		cacheValid = cacheDatabase.cacheValid(requestHash);
		cacheExists = cacheDatabase.cacheExists(requestHash);
				
		if (cacheValid)
		{
			return buildResponseFromDatabase();
		}
		else
		{
			// The cache is out of date (invalid) but load it anyway while we contact the API server
			if (cacheExists) 
			{
				cachedData = buildResponseFromDatabase();
				publishProgress();
			}
			else callback.updateState(APIExceptionCallback.STATE_CACHED_RESPONSE_NOT_FOUND);
 	
			AssetListParser parser = AssetListParser.getInstance();		
			AssetListResponse response = null;
						
	        try 
	        { 
	        	response = parser.getResponse(apiAuth);
	        		        	
	        	cacheDatabase.updateCache(requestHash, response.getCachedUntil());
	        	assetsDatabase.setAssets(apiAuth.getCharacterID().intValue(), response.getAll());
	        }
			catch (ApiException e) 
			{
				apiExceptionOccured = true;
				exception = e;
			}
	        	        
	        return response;
		}
	}
	
	@Override
	protected void onPostExecute(AssetListResponse response) 
	{	
		if (cacheValid)
		{
			callback.updateState(APIExceptionCallback.STATE_CACHED_RESPONSE_ACQUIRED_VALID);
			callback.onUpdate(response);
		}
		else
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
    }

	@Override
	protected void onProgressUpdate(Void... progress)
	{		
		callback.onUpdate(cachedData);
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
		
		Collection<EveAsset<EveAsset<?>>> assets = assetsDatabase.getAssets(apiAuth.getCharacterID().intValue());
		for (EveAsset<EveAsset<?>> asset : assets) response.add(asset);
				
		return response;
	}
}

