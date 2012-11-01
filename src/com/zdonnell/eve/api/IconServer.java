package com.zdonnell.eve.api;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v4.util.LruCache;
import android.util.SparseArray;

/**
 * This class handles acquisition of icons from the typeIcon dump provided by CCP.
 * 
 * If this class is being used to provide only a single icon, instantiate it with {@link #IconServer(Context, int)}
 * and a cacheSize of 0.  Otherwise, if the class will be used to provide multiple (potentially duplicate) icons,
 * either use the default {@link #IconServer(Context)} constructor, or use {@link #IconServer(Context, int)} and provide
 * a size for the cache.
 * 
 * @see #getIcons(ArrayList, IconObtainedCallback)
 * @see #cacheIcons(ArrayList)
 * 
 * @author zachd
 *
 */
public class IconServer 
{
	private static final String baseIconPath = "icon_types/";
	
	boolean usingCache = true;
	
	/**
	 * size specification for the size of the {@link #bitmapCache}
	 * @see #IconServer(Context, int)
	 */
	int cacheSize = 4 * 1024 * 1024; // 4MiB
   
	/**
	 * The in memory cache of recently served icons
	 */
	LruCache<Integer, Bitmap> bitmapCache;

	private Context context; 
	
	/**
	 * Constructor that uses the default sized {@link LruCache}
	 * 
	 * @param context
	 * @see #bitmapCache
	 * @see #cacheSize
	 * 
	 */
	public IconServer(Context context)
	{
		this.context = context;
		initializeCache();
   	}
   
	/**
	 * Constructor that specifies the size of the in in memory cache
	 * 
	 * @param context The Context of the application
	 * @param cacheSize The size of the in memory {@link LruCache}.  A value of 0 will assume no in memory Cache.
	 * @see #bitmapCache
	 */
	public IconServer(Context context, int cacheSize)
   	{
		this.context = context;
		
		if (cacheSize != 0)
	   	{
		   	this.cacheSize = cacheSize;
		   	initializeCache();
		   
	   	} else usingCache = false;
   	}
	
	/**
	 * Will load the icons from the list of IDs provided, and pass them to the {@link IconObtainedCallback} provided in the form
	 * of a {@link SparseArray} of {@link Bitmap}
	 * 
	 * @param iconIDs An {@link ArrayList} of Integers representing the list of icons to
	 * be aquired
	 * @param callback An {@link IconObtainedCallback} that will passed the fully assembled {@link SparseArray}
	 * of {@link Bitmap} icons.
	 * @see {@link IconObtainedCallback}
	 */
	public void getIcons(ArrayList<Integer> iconIDs, final IconObtainedCallback callback)
	{
		SparseArray<Bitmap> cachedIcons = new SparseArray<Bitmap>(iconIDs.size());
		ArrayList<Integer> iconsToLoad = new ArrayList<Integer>(iconIDs.size());
		
		/* check for already cached icons */
		for (int iconID : iconIDs)
		{
			Bitmap cachedIcon = bitmapCache.get(iconID);
			
			if (cachedIcon != null) cachedIcons.put(iconID, cachedIcon);
			else iconsToLoad.add(iconID);
		}
		
		/* Finalize objects for direct reference in the callback */
		final ArrayList<Integer> cachedIconsIDList = iconIDs;
		final SparseArray<Bitmap> cachedIconsFinal = cachedIcons;
		
		/**
		 * If there are IDs requested that are not cached, we need to load them up
		 * We will request that they be loaded with an {@link IconLoader}, and upon completion will combine
		 * the previously obtained cached icons, and return the complete {@link SparseArray} to the provided
		 * {@link IconObtainedCallback}
		 */
		if (!iconsToLoad.isEmpty())
		{
			new IconLoader(context, bitmapCache, new IconObtainedCallback()
			{
				@Override
				public void iconsObtained(SparseArray<Bitmap> bitmaps) 
				{
					/* Merge in the SparseArray of already cached icons */
					for (int cachedIconID : cachedIconsIDList)
					{
						bitmaps.put(cachedIconID, cachedIconsFinal.get(cachedIconID));				
					}
					
					callback.iconsObtained(bitmaps);
				}
			}).execute((Integer[]) iconsToLoad.toArray());
		}
	}
	
	/**
	 * Takes a list of IDs and checks if they are already in the {@link #bitmapCache}.
	 * If an ID is, it will be moved to the head of the cache.  The remaining uncached 
	 * IDs will be loaded via an {@link IconLoader}
	 * 
	 * @param iconIDs an {@link ArrayList} of integer iconIDs to cache
	 */
	public void cacheIcons(ArrayList<Integer> iconIDs)
	{
		ArrayList<Integer> iconsToLoad = new ArrayList<Integer>(iconIDs.size());
		Bitmap tempBitmapCheck;
		
		for (Integer iconID : iconIDs)
		{
			tempBitmapCheck = bitmapCache.get(iconID);
			if (tempBitmapCheck == null) iconsToLoad.add(iconID);
		}
		
		new IconLoader(context, bitmapCache).execute((Integer[]) iconsToLoad.toArray());
	}
   
	/**
	 * Initializes the {@link #bitmapCache} according to {@link #cacheSize}
	 */
	private void initializeCache()
	{
		bitmapCache = new LruCache<Integer, Bitmap>(cacheSize) 
	    {
			@Override
			protected int sizeOf(Integer key, Bitmap value) 
			{
				return value.getByteCount();
			}
	    }; 
	}
	
	private class IconLoader extends AsyncTask<Integer, Void, SparseArray<Bitmap>>
	{
		private Context context;
		private LruCache<Integer, Bitmap> bitmapCache;
		private IconObtainedCallback callback = null;
		
		public IconLoader(Context context, LruCache<Integer, Bitmap> bitmapCache)
		{
			this.context = context;
			this.bitmapCache = bitmapCache;
		}
		
		public IconLoader(Context context, LruCache<Integer, Bitmap> bitmapCache, IconObtainedCallback callback)
		{
			this(context, bitmapCache);
			this.callback = callback;
		}
		
		@Override
		protected SparseArray<Bitmap> doInBackground(Integer... iconIDs) 
		{
			AssetManager assets = context.getAssets();
			
			SparseArray<Bitmap> bitmapsLoaded = new SparseArray<Bitmap>(iconIDs.length);
			
			for (int iconID : iconIDs)
			{
				InputStream bitmapInputStream;
				Bitmap bitmapLoaded = null;
				
				try 
				{
					bitmapInputStream = assets.open(baseIconPath + iconID + "_64.png");
					bitmapLoaded = BitmapFactory.decodeStream(bitmapInputStream);
				} 
				catch (IOException e) { }
				
				if (bitmapLoaded != null) bitmapCache.put(iconID, bitmapLoaded);
				bitmapsLoaded.put(iconID, bitmapLoaded);
			}
			
			return bitmapsLoaded;
		}
		
		@Override
		protected void onPostExecute(SparseArray<Bitmap> bitmaps)
		{
			if (callback != null) callback.iconsObtained(bitmaps);
		}
	}
	
	public abstract class IconObtainedCallback
	{
		abstract public void iconsObtained(SparseArray<Bitmap> bitmaps);
	}
}
