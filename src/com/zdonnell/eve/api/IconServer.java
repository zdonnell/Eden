package com.zdonnell.eve.api;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v4.util.LruCache;
import android.util.Log;
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
	public void getIcons(int[] iconIDs, final IconObtainedCallback callback)
	{
		SparseArray<Bitmap> cachedIcons = new SparseArray<Bitmap>(iconIDs.length);
		ArrayList<Integer> iconsToLoad = new ArrayList<Integer>(iconIDs.length);
		
		/* check for already cached icons */
		for (int iconID : iconIDs)
		{
			Bitmap cachedIcon = bitmapCache.get(iconID);
			
			if (cachedIcon != null) cachedIcons.put(iconID, cachedIcon);
			else iconsToLoad.add(iconID);
		}
		
		/* Finalize objects for direct reference in the callback */
		final int[] cachedIconsIDList = iconIDs;
		final SparseArray<Bitmap> cachedIconsFinal = cachedIcons;
		
		/**
		 * If there are IDs requested that are not cached, we need to load them up
		 * We will request that they be loaded with an {@link IconLoader}, and upon completion will combine
		 * the previously obtained cached icons, and return the complete {@link SparseArray} to the provided
		 * {@link IconObtainedCallback}
		 */
		Integer[] staticIconsToLoad = new Integer[iconsToLoad.size()];
		iconsToLoad.toArray(staticIconsToLoad);
		
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
						bitmaps.put(cachedIconID, bitmaps.get(cachedIconID));				
					}
					
					callback.iconsObtained(bitmaps);
				}
			}).execute(staticIconsToLoad);
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
			SparseArray<Bitmap> bitmapsLoaded = new SparseArray<Bitmap>(iconIDs.length);
			
			FileInputStream fis = null;
			BufferedInputStream buf = null;
			byte[] ByteBuffer;
			
			for (int iconID : iconIDs)
			{
				Bitmap bitmapLoaded = null;
				Log.d("ICON ID", "is " + iconID);
				
				try 
				{
					fis = context.openFileInput("type_" + iconID + ".png");
					buf = new BufferedInputStream(fis);
					
					ByteBuffer = new byte[buf.available()];
					buf.read(ByteBuffer);
					
					bitmapLoaded = BitmapFactory.decodeByteArray(ByteBuffer, 0, ByteBuffer.length);	
					
					if (fis != null) fis.close();
					if (buf != null) buf.close();
				} 
				catch (FileNotFoundException e) { } 
				catch (IOException e) { }
				
				if (bitmapLoaded == null)
				{
					Log.d("Image", "getting from server");
					
					String imageURL = "http://image.eveonline.com/Type/" + iconID + "_64.png";

					InputStream in;
					try 
					{
						in = new java.net.URL(imageURL).openStream();
						bitmapLoaded = BitmapFactory.decodeStream(in);
					} 
					catch (MalformedURLException e) { e.printStackTrace(); } 
					catch (IOException e) { e.printStackTrace(); }
					
					
					if (bitmapLoaded != null) 
					{
						Log.d("Image", "not null");
						saveBitmap(bitmapLoaded, iconID);
					}
				}
				
				if (bitmapLoaded != null) bitmapCache.put(iconID, bitmapLoaded);
				bitmapsLoaded.put(iconID, bitmapLoaded);
			}
			
			return bitmapsLoaded;
		}
		
		/**
		 * Saves the specified bitmap to local storage
		 * 
		 * @param image The Bitmap to save
		 * @param actorID The Char or Corp ID
		 */
		private void saveBitmap(Bitmap image, int actorID) 
		{		
			try 
			{
				FileOutputStream out = context.openFileOutput("type_" + actorID + ".png", 0);
				image.compress(Bitmap.CompressFormat.PNG, 100, out);
				out.close();
			} 
			catch (Exception e) 
			{
				e.printStackTrace();
			}
		}
		
		@Override
		protected void onPostExecute(SparseArray<Bitmap> bitmaps)
		{
			if (callback != null) callback.iconsObtained(bitmaps);
		}
	}
	
	public static abstract class IconObtainedCallback
	{
		abstract public void iconsObtained(SparseArray<Bitmap> bitmaps);
	}
}
