package com.zdonnell.eve.apilink.eve;

import android.content.Context;
import android.os.AsyncTask;

import com.beimin.eveapi.eve.conquerablestationlist.StationListResponse;
import com.beimin.eveapi.eve.skilltree.SkillTreeResponse;
import com.zdonnell.eve.apilink.APIExceptionCallback;

public class Eve 
{
	private Context context;
	
	public Eve(Context context)
	{
		this.context = context;
	}
	
	public void skillTree(APIExceptionCallback<SkillTreeResponse> callback)
	{
		new SkillTreeTask(callback, context).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR); 
	}
	
	public void conqStationsList(APIExceptionCallback<StationListResponse> callback)
	{
		new ConquerableStationsTask(callback).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR); 
	}
}
