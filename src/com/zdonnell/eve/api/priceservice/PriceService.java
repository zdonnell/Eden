package com.zdonnell.eve.api.priceservice;

import android.content.Context;
import android.util.Log;
import android.util.SparseArray;

import com.zdonnell.eve.api.APICallback;
import com.zdonnell.eve.helpers.Tools;

public class PriceService {
	
	private static PriceService instance;
	
	private Context context;
		
	/* Time in millis to store the price before forcing a new request */
	private final long priceCacheTime = 168 * 60 * 60 * 1000; // 168 hours or 7 days
	
	private PriceDatabase priceDatabase;
	
	/**
	 * Singleton access method
	 * 
	 * @param context
	 * @return
	 */
	public static PriceService getInstance(Context context)
	{
		if (instance == null)
		{
			instance = new PriceService(context);
		}
		return instance;
	}
	
	private PriceService(Context context)
	{
		this.priceDatabase = new PriceDatabase(context);
		this.context = context;
	}
	
	public void getValues(Integer[] typeIDs, APICallback<SparseArray<Float>> callback)
	{	
		Integer[] strippedTypeIDs = Tools.stripDuplicateIDs(typeIDs);
		
		SparseArray<Float> cachedPrices = priceDatabase.getPrices(strippedTypeIDs, priceCacheTime);
				
		/* If the returned SparseArray is of the same size as the Integer array then it contains all prices requested */
		if (cachedPrices.size() == strippedTypeIDs.length) callback.onUpdate(cachedPrices);
		else
		{						
			Integer[] nonCachedTypeIDs = new Integer[strippedTypeIDs.length - cachedPrices.size()];
			
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
			
			new PriceCheckTask(callback, cachedPrices, context).execute(nonCachedTypeIDs);
		}
	}
}
