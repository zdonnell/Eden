package com.zdonnell.eve.apilink.eve;

import android.content.Context;
import android.os.AsyncTask;
import android.util.SparseArray;

import com.beimin.eveapi.core.ApiPage;
import com.beimin.eveapi.core.ApiPath;
import com.beimin.eveapi.eve.skilltree.ApiSkill;
import com.beimin.eveapi.eve.skilltree.ApiSkillGroup;
import com.beimin.eveapi.eve.skilltree.SkillTreeParser;
import com.beimin.eveapi.eve.skilltree.SkillTreeResponse;
import com.beimin.eveapi.exception.ApiException;
import com.zdonnell.eve.apilink.APIExceptionCallback;
import com.zdonnell.eve.apilink.CacheDatabase;
import com.zdonnell.eve.apilink.IApiTask;
import com.zdonnell.eve.database.SkillTree;

public class SkillTreeTask extends AsyncTask<Void, Void, SkillTreeResponse> implements IApiTask<SkillTreeResponse>
{
	private APIExceptionCallback<SkillTreeResponse> callback;
	
	private boolean apiExceptionOccured = false;
	private ApiException exception;
	
	private CacheDatabase cacheDatabase;
	
	private Context context;
	
	private boolean cacheExists = false, cacheValid = false;
	
	private SkillTreeResponse cachedData;
	
	public SkillTreeTask(APIExceptionCallback<SkillTreeResponse> callback, Context context)
	{
		this.callback = callback;
		this.context = context;
		
		cacheDatabase = new CacheDatabase(context);
	}
	
	@Override
	protected SkillTreeResponse doInBackground(Void... params)
	{		
		int requestHash = requestTypeHash();
		
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
			
			SkillTreeParser parser = SkillTreeParser.getInstance();		
			SkillTreeResponse response = null;
			
	        try 
	        { 
	        	response = parser.getResponse();
	        	fixSkillGroups(response);
	        	
	        	cacheDatabase.updateCache(requestHash, response.getCachedUntil());
	        	new SkillTree(context).setSkillTree(response.getAll());
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
	protected void onPostExecute(SkillTreeResponse response) 
	{
		// We can arrive here one of two ways, if the cache was still valid, or if it was invalid
		// and a server response was acquired, check which it is.
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
	
	/**
	 * Removes duplicate groups from a {@link SkillTreeResponse}
	 * 
	 * @param response
	 */
	private void fixSkillGroups(SkillTreeResponse response)
	{
		SparseArray<ApiSkillGroup> correctedSkillGroups = new SparseArray<ApiSkillGroup>();
		
		for (ApiSkillGroup group : response.getAll())
		{
			// This is the first time we have seen a group of this ID, add it to the corrected list
			if (correctedSkillGroups.get(group.getGroupID()) == null)
			{
				correctedSkillGroups.put(group.getGroupID(), group);
			}
			// The group exists in the corrected list already, add all it's skills to the existing group in the corrected list
			else
			{
				for (ApiSkill skill : group.getSkills()) correctedSkillGroups.get(group.getGroupID()).add(skill);
			}
		}
		
		// Add the corrected groups to a new SkillTreeResponse
		response.getAll().clear();
		for (int i = 0; i < correctedSkillGroups.size(); i++) response.add(correctedSkillGroups.valueAt(i));
	}

	@Override
	public int requestTypeHash() 
	{
		return ApiPath.EVE.getPath().concat(ApiPage.SKILL_TREE.getPage()).hashCode();
	}

	@Override
	public SkillTreeResponse buildResponseFromDatabase() 
	{
		SkillTreeResponse response = new SkillTreeResponse();
		
		SkillTree skillTree = new SkillTree(context);
		for (ApiSkillGroup group : skillTree.getSkillTree()) response.add(group);
		
		return response;
	}
}
