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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.util.SparseArray;

public class ImageService {

	/* Singleton instance of the Resource Manager */
	private static ImageService instance;
	
	private final static int CHAR = 0;
	private final static int CORP = 1;
	private final static int ICON = 2;
	
	private final static String serverUrl = "http://image.eveonline.com/";
	
	private static String[] url = new String[3];
	private static String[] localImagePrefix = new String[3];
	private static int imageSize[] = new int[3];
	private static String imageExtension[] = new String[3];

	static
	{
		url[CHAR] = serverUrl + "Character/";
		url[CORP] = serverUrl + "Corporation/";
		url[ICON] = serverUrl + "Type/";
		
		localImagePrefix[CHAR] = "char_";
		localImagePrefix[CORP] = "corp_";
		localImagePrefix[ICON] = "type_";
		
		imageSize[CHAR] = 512;
		imageSize[CORP] = 256;
		imageSize[ICON] = 64;
		
		imageExtension[CHAR] = ".jpg";
		imageExtension[CORP] = ".png";
		imageExtension[ICON] = ".png";
	}
 	
	/**
	 * Array of cache sizes
	 */
	private int cacheSizes[] = new int[3];
	
	/**
	 * array of in memory caches
	 */
	@SuppressWarnings("unchecked")
	private LruCache<Integer, Bitmap> bitmapCaches[] = new LruCache[3];
	
	/**
	 * Context of the app for file access etc.
	 */
	private Context context; 
	
	/**
	 * Singleton access method
	 * 
	 * @param context
	 * @return
	 */
	public static ImageService getInstance(Context context)
	{
		if (instance == null)
		{
			instance = new ImageService(context);
		}
		return instance;
	}	
	
	/**
	 * Constructor that uses the default sized {@link LruCache}
	 * 
	 * @param context
	 * @see #bitmapCaches
	 * @see #cacheSizes
	 * 
	 */
	public ImageService(Context context)
	{
		this.context = context;
		initializeCaches(null);
   	}
	
	/**
	 * Constructor that uses the default sized {@link LruCache}
	 * 
	 * @param context
	 * @param cacheSizes array of values that specify the in-memory cache size. 
	 * Use {@link #CHAR}, {@link #CORP}, and {@link #ICON} for array indices.
	 * @see #bitmapCaches
	 * @see #cacheSizes
	 * 
	 */
	public ImageService(Context context, int[] cacheSizes)
	{
		this.context = context;
		initializeCaches(cacheSizes);
	}
	
	/**
	 * Gets bitmaps for the specified typeIDs.  Upon loading they will be passed
	 * to the provided callback.
	 * 
	 * To cache bitmaps for future use, this can be called with a null callback.
	 * Alternatively the callback can be used as a notification for when the images
	 * are cached.
	 * 
	 * @see {@link IconObtainedCallback}
	 * 
	 * @param callback {@link IconObtainedCallback} to be notified when the loading is complete
	 * @param typeIDs 
	 */
	public void getTypes(IconObtainedCallback callback, int... typeIDs)
	{
		getImages(callback, typeIDs, ICON);
	}
	
	/**
	 * Gets bitmaps for the specified character IDs.  Upon loading they will be passed
	 * to the provided callback.
	 * 
	 * To cache bitmaps for future use, this can be called with a null callback.
	 * Alternatively the callback can be used as a notification for when the images
	 * are cached.
	 * 
	 * @see {@link IconObtainedCallback}
	 * 
	 * @param callback {@link IconObtainedCallback} to be notified when the loading is complete
	 * @param charIDs 
	 */
	public void getPortraits(IconObtainedCallback callback, int... charIDs)
	{
		getImages(callback, charIDs, CHAR);
	}
	
	/**
	 * Gets bitmaps for the specified corporation IDs.  Upon loading they will be passed
	 * to the provided callback.
	 * 
	 * To cache bitmaps for future use, this can be called with a null callback.
	 * Alternatively the callback can be used as a notification for when the images
	 * are cached.
	 * 
	 * @see {@link IconObtainedCallback}
	 * 
	 * @param callback {@link IconObtainedCallback} to be notified when the loading is complete
	 * @param charIDs 
	 */
	public void getCorpLogos(IconObtainedCallback callback, int... corpIDs)
	{
		getImages(callback, corpIDs, CORP);
	}
	
	/**
	 * If a low memory warning is received, this can be used to empty a specific cache
	 * 
	 * @param cacheType the cache to clear
	 */
	public void clearCache(int cacheType) { bitmapCaches[cacheType].evictAll(); }
		
	/**
	 * 
	 * @param callback
	 * @param ids
	 * @param type
	 */
	private void getImages(final IconObtainedCallback callback, int[] ids, int type)
	{
		final SparseArray<Bitmap> cachedIDs = new SparseArray<Bitmap>(ids.length);
		final ArrayList<Integer> idsToLoad = new ArrayList<Integer>(ids.length);
		final ArrayList<Integer> idsCached = new ArrayList<Integer>(ids.length);
		
		/* check for already cached images */
		for (int id : ids)
		{
			Bitmap cachedIcon = bitmapCaches[type].get(id);
			
			if (cachedIcon != null) 
			{
				cachedIDs.put(id, cachedIcon); 
				idsCached.add(id);
			}
			else idsToLoad.add(id);
		}
			
		/** 
		 * If there are IDs requested that are not cached, we need to load them up
		 * We will request that they be loaded with an {@link IconLoader}, and upon completion will combine
		 * the previously obtained cached icons, and return the complete {@link SparseArray} to the provided
		 * {@link IconObtainedCallback}
		 */
		Integer[] staticIconsToLoad = new Integer[idsToLoad.size()];
		idsToLoad.toArray(staticIconsToLoad);
		
		if (!idsToLoad.isEmpty())
		{			
			new IconLoader(type, new IconObtainedCallback()
			{
				@Override
				public void iconsObtained(SparseArray<Bitmap> bitmaps) 
				{				
					/* Merge in the SparseArray of already cached images */
					for (int cachedID : idsCached) bitmaps.put(cachedID, cachedIDs.get(cachedID));				
					
					if (callback != null) callback.iconsObtained(bitmaps);
				}
			}).execute(staticIconsToLoad);
		}
		else callback.iconsObtained(cachedIDs);
	}
	
	private class IconLoader extends AsyncTask<Integer, Void, SparseArray<Bitmap>>
	{
		private IconObtainedCallback callback = null;
		private int type;
		
		public IconLoader(int type)
		{
			this.type = type;
		}
		
		public IconLoader(int type, IconObtainedCallback callback)
		{
			this(type);
			this.callback = callback;
		}
		
		@Override
		protected SparseArray<Bitmap> doInBackground(Integer... ids) 
		{			
			SparseArray<Bitmap> bitmapsLoaded = new SparseArray<Bitmap>(ids.length);
						
			for (int id : ids)
			{
				Bitmap bitmapLoaded = null;
				
				try { bitmapLoaded = loadBitmapFromStorage(id, type); }
				catch (Exception e) { e.printStackTrace(); } 
				
				/* There was nothing loaded locally, try to download the image */
				if (bitmapLoaded == null)
				{					
					try { bitmapLoaded = retrieveBitmapFromServer(id, type); }
					catch (Exception e) { e.printStackTrace(); }
				}
				
				if (bitmapLoaded != null) bitmapCaches[type].put(id, bitmapLoaded);
				bitmapsLoaded.put(id, bitmapLoaded);
			}
			return bitmapsLoaded;
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
	
	/**
	 * Attempts to load the specified image from storage.
	 * 
	 * @param id the id of the image to load
	 * @param type {@link ImageService#CHAR}, {@link ImageService#CORP}, or {@link ImageService#ICON}
	 * @return the {@link Bitmap} loaded from storage
	 * @throws IOException if the image does not exist
	 */
	private Bitmap loadBitmapFromStorage(int id, int type) throws IOException
	{
		FileInputStream fis = context.openFileInput(localImagePrefix[type] + id + ".png");
		BufferedInputStream buf = new BufferedInputStream(fis);
		
		byte[] ByteBuffer = new byte[buf.available()];
		buf.read(ByteBuffer);
		
		Bitmap bitmapLoaded = BitmapFactory.decodeByteArray(ByteBuffer, 0, ByteBuffer.length);	
		
		if (fis != null) fis.close();
		if (buf != null) buf.close();
		
		return bitmapLoaded;
	}
	
	/**
	 * Attempts to retrieve the specified image from the Eve Online Image Server.
	 * 
	 * @param id the id of the image to load
	 * @param type {@link ImageService#CHAR}, {@link ImageService#CORP}, or {@link ImageService#ICON}
	 * @return the {@link Bitmap} obtained from the server, or null if it could not be retrieved.
	 * @throws IOException if the image does not exist
	 */
	private Bitmap retrieveBitmapFromServer(int id, int type) throws MalformedURLException, IOException
	{
		Bitmap bitmapLoaded = null;
		
		String imageURL = url[type] + id + "_" + imageSize[type] + imageExtension[type];
	
		InputStream in = new java.net.URL(imageURL).openStream();
		bitmapLoaded = BitmapFactory.decodeStream(in);
			
		if (bitmapLoaded != null) saveBitmap(bitmapLoaded, id, type);
		
		return bitmapLoaded;
	}
	
	/**
	 * Saves the specified bitmap to local storage
	 * 
	 * @param image The Bitmap to save
	 * @param id the id of the Bitmap to save
	 * @param type {@link ImageService#CHAR}, {@link ImageService#CORP}, or {@link ImageService#ICON}
	 */
	private void saveBitmap(Bitmap image, int id, int type) 
	{		
		try 
		{
			FileOutputStream out = context.openFileOutput(localImagePrefix[type] + id + ".png", 0);
			image.compress(Bitmap.CompressFormat.PNG, 100, out);
			out.close();
		} 
		catch (Exception e) { e.printStackTrace(); }
	}
	
	/**
	 * Initializes the {@link #bitmapCaches} according to provided cacheSizes.  If
	 * no values are provided, defaults are used.
	 * 
	 * @param cacheSizes the sizes to be used for each cache.  if 0 is provided the cache
	 * will not be used.
	 * 
	 * @see #Images(Context)
	 * @see #Images(Context, int[])
	 */
	private void initializeCaches(int[] cacheSizes)
	{
		if (cacheSizes == null) 
		{
			this.cacheSizes[CHAR] = 4 * 1024 * 1024; // 4 MB
			this.cacheSizes[CORP] = 2 * 1024 * 1024; // 2 MB
			this.cacheSizes[ICON] = 4 * 1024 * 1024; // 4 MB
		} 
		else for (int i = 0; i < 3; i++) this.cacheSizes[i] = cacheSizes[i];
		
		for (int i = 0; i < 3; i++)
		{
			bitmapCaches[i] = new LruCache<Integer, Bitmap>(this.cacheSizes[i]) 
		    {
				@Override
				protected int sizeOf(Integer key, Bitmap value) 
				{
					return value.getByteCount();
				}
		    };
		}
	}
}
