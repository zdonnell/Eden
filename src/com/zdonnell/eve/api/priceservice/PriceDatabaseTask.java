package com.zdonnell.eve.api.priceservice;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.SparseArray;

import com.zdonnell.eve.api.APICallback;

public class PriceDatabaseTask extends AsyncTask<Integer, Integer, SparseArray<Float>>
{		
	private APICallback<SparseArray<Float>> callback;
	
	private static final int HOURLY = 0;
	private static final int DAILY = 1;
	private static final int WEEKLY = 2;
	
	private static long[] cachePriceTimes = new long[3];
	static
	{
		cachePriceTimes[HOURLY] =   1 * 60 * 60 * 1000;
		cachePriceTimes[DAILY]  =  24 * 60 * 60 * 1000;
		cachePriceTimes[WEEKLY] = 168 * 60 * 60 * 1000;
	}
	
	private long priceCacheTime; // 168 hours or 7 days
		
	private PriceDatabase priceDatabase;
	
	private Context context;
	
	private Integer[] strippedTypeIDs;
	
	public PriceDatabaseTask(APICallback<SparseArray<Float>> callback, Context context)
	{
		this.callback = callback;
		this.context = context;
		this.priceDatabase = new PriceDatabase(context);
		
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		priceCacheTime = cachePriceTimes[Integer.valueOf(preferences.getString("price_frequency", "1"))];
	}
	
	@Override
	protected SparseArray<Float> doInBackground(Integer... typeIDs)
	{	
		strippedTypeIDs = typeIDs;
		return priceDatabase.getPrices(typeIDs, priceCacheTime);
	}
	
	@Override
	protected void onPostExecute(SparseArray<Float> cachedPrices)
	{								
		/* If the returned SparseArray is of the same size as the Integer array then it contains all prices requested */
		if (cachedPrices.size() == strippedTypeIDs.length) callback.onUpdate(cachedPrices);
		else
		{						
			Integer[] nonCachedTypeIDs = new Integer[strippedTypeIDs.length - cachedPrices.size()];
			
			Log.d("Price Service", "Requesting prices for " + nonCachedTypeIDs.length + " types");
			
			int index = 0;
			for (int typeID : strippedTypeIDs)
			{
				/* Check to if each typeID has a price in the cachedPrices SparseArray, 
				 * if not add it to the Array of prices to be queried 
				 */
				if (cachedPrices.get(typeID) == null) 
				{
					nonCachedTypeIDs[index] = typeID;
					++index;
				}
			}
			
			new PriceCheckTask(callback, cachedPrices, context).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, nonCachedTypeIDs);
		}	
	}
}