package com.zdonnell.eve.api.priceservice;

import android.content.Context;
import android.os.AsyncTask;
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
		
		new PriceDatabaseTask(callback, context).execute(strippedTypeIDs);
	}
}