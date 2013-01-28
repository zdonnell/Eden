package com.zdonnell.eve.api.priceservice;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import com.zdonnell.eve.api.APICallback;

import android.content.Context;
import android.os.AsyncTask;
import android.util.SparseArray;

public class PriceService {
	
	private static PriceService instance;
	
	private Context context;
		
	/* Time in millis to store the price before forcing a new request */
	private final long priceCacheTime = 168 * 60 * 60 * 1000; // 168 hours or 7 days
	
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
		this.context = context;
	}
	
	public void getValues(Integer[] typeIDs, APICallback<SparseArray<Float>> callback)
	{		
		new PriceCheckTask(callback).execute(typeIDs);
	}
}
