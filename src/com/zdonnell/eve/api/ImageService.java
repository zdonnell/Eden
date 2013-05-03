package com.zdonnell.eve.api;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
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
	 * If a request is made for a image while it is already in a load queue, save the request callbacks in this
	 * SparseArray so they can be notified when the image is loaded.
	 */
	private SparseArray<SparseArray<ArrayList<IconObtainedCallback>>> pendingRequests = new SparseArray<SparseArray<ArrayList<IconObtainedCallback>>>();
	
	private SparseArray<ArrayList<Integer>> typesBeingLoaded = new SparseArray<ArrayList<Integer>>();
	
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
	private ImageService(Context context)
	{
		this.context = context;
		initializeCaches(null);
		
		/* setup the pending request holders, and the in loading list */
		for (int x = 0; x < 3; x++)
		{			
			typesBeingLoaded.put(x, new ArrayList<Integer>());
			pendingRequests.put(x, new SparseArray<ArrayList<IconObtainedCallback>>());
		}
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
	private ImageService(Context context, int[] cacheSizes)
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
	public void getTypes(IconObtainedCallback callback, boolean thumb, Integer... typeIDs)
	{
		if (typeIDs.length > 1) getImages(callback, typeIDs, ICON, thumb);
		else
		{
			if (idIsBeingLoaded(typeIDs[0], ICON)) queueRequest(typeIDs[0], ICON, callback);
			else getImages(callback, typeIDs, ICON, thumb);
		}
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
	public void getPortraits(IconObtainedCallback callback, boolean thumb, Integer... charIDs)
	{
		if (charIDs.length > 1) getImages(callback, charIDs, CHAR, thumb);
		else
		{
			if (idIsBeingLoaded(charIDs[0], CHAR)) queueRequest(charIDs[0], CHAR, callback);
			else getImages(callback, charIDs, CHAR, thumb);
		}
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
	public void getCorpLogos(IconObtainedCallback callback, boolean thumb, Integer... corpIDs)
	{
		if (corpIDs.length > 1) getImages(callback, corpIDs, CORP, thumb);
		else
		{
			if (idIsBeingLoaded(corpIDs[0], CORP)) queueRequest(corpIDs[0], CORP, callback);
			else getImages(callback, corpIDs, CORP, thumb);
		}
	}
	
	/**
	 * Checks to see if the current id for a specific type has already had a load request outstanding for it
	 * 
	 * @param id the character, corp, or type ID 
	 * @param type {@link #CHAR}, {@link #CORP}, or {@link #ICON}
	 * @return true if the type is already being loaded
	 */
	private boolean idIsBeingLoaded(int id, int type)
	{
		return typesBeingLoaded.get(type) != null && typesBeingLoaded.get(type).contains(id);
	}
	
	/** 
	 * This will create the list for the specified id/type combination if it does not already exist, and then add the provided
	 * callback to the list.
	 * 
	 * @param id the character, corp, or type ID 
	 * @param type {@link #CHAR}, {@link #CORP}, or {@link #ICON}
	 * @param callback the callback request to add to the list
	 */
	private void queueRequest(int id, int type, IconObtainedCallback callback)
	{
		if (pendingRequests.get(type).get(id) == null) pendingRequests.get(type).put(id, new ArrayList<IconObtainedCallback>());
		pendingRequests.get(type).get(id).add(callback);
	}
	
	/**
	 * If a low memory warning is received, this can be used to empty a specific cache
	 * 
	 * @param cacheType the cache to clear
	 */
	public void clearMemoryCache(int cacheType) { bitmapCaches[cacheType].evictAll(); }
		
	/**
	 * 
	 * @param callback
	 * @param ids
	 * @param type
	 */
	private void getImages(final IconObtainedCallback callback, Integer[] ids, final int type, boolean thumb)
	{		
		for (int i = 0; i < pendingRequests.get(CHAR).size(); ++i)
		{
			ArrayList<IconObtainedCallback> requestsForChar = pendingRequests.get(CHAR).valueAt(i);
		}
		
		/* add bitmap ids to the list of bitmaps being loaded */
		for (int id : ids) 
		{
			if (!typesBeingLoaded.get(type).contains(id)) typesBeingLoaded.get(type).add(id);
		}
		
		final SparseArray<Bitmap> cachedIDs = new SparseArray<Bitmap>(ids.length);
		final ArrayList<Integer> idsToLoad = new ArrayList<Integer>(ids.length);
		final ArrayList<Integer> idsCached = new ArrayList<Integer>(ids.length);
		
		/* check for already cached images */
		for (int id : ids)
		{
			Bitmap cachedIcon = bitmapCaches[type].get(id);
			boolean isValidIcon = false;
			
			if (cachedIcon != null)
			{
				if (cachedIcon.getWidth() > (thumb ? imageSize[type]/4 : imageSize[type])) isValidIcon = true;
			}
			
			if (isValidIcon) 
			{
				cachedIDs.put(id, cachedIcon); 
				idsCached.add(id);
			}
			else idsToLoad.add(id);;
		}
			
		/** 
		 * If there are IDs requested that are not cached, we need to load them up
		 * We will request that they be loaded with an {@link IconLoader}, and upon completion will combine
		 * the previously obtained cached icons, and return the complete {@link SparseArray} to the provided
		 * {@link IconObtainedCallback}
		 */
		Integer[] staticIconsToLoad = new Integer[idsToLoad.size()];
		idsToLoad.toArray(staticIconsToLoad);
				
		if (staticIconsToLoad.length > 0)
		{			
			new IconLoader(type, new IconObtainedCallback()
			{
				@Override
				public void iconsObtained(SparseArray<Bitmap> bitmaps) 
				{				
					/* Merge in the SparseArray of already cached images */
					for (int cachedID : idsCached) 
					{
						bitmaps.put(cachedID, cachedIDs.get(cachedID));				
					}
					
					if (callback != null) callback.iconsObtained(bitmaps);
					checkPendingRequests(bitmaps, type);
				}
			}, thumb).execute(staticIconsToLoad);
		}
		else
		{				
			if (callback != null) callback.iconsObtained(cachedIDs);
			checkPendingRequests(cachedIDs, type);
		}
	}
	
	/**
	 * Checks all pending requests to see if any of the bitmaps provided
	 * have callback requests waiting for them.  If there are the bitmap is provided
	 * to the callback(s), and the callback(s) are removed from the pending request list.
	 * 
	 * @param bitmaps
	 * @param type
	 */
	private void checkPendingRequests(SparseArray<Bitmap> bitmaps, int type)
	{
		SparseArray<Bitmap> tempSparseArray = new SparseArray<Bitmap>();
		
		/* Cycle through all Bitmaps provided */
		for (int i = 0; i < bitmaps.size(); i++)
		{			
			/* Get the key of the Bitmap (id) and try to acquire the pending request list for that type + id */
			int bitmapID = bitmaps.keyAt(i);
			ArrayList<IconObtainedCallback> callbacksForID = pendingRequests.get(type).get(bitmapID);
			
			Log.d("TESTING LOG STATEMENT", "HERE" + bitmapID);
			
			/* check if there are actually pending requests */
			if (callbacksForID != null && !callbacksForID.isEmpty())
			{
				/* Create a sparse array containing just the Bitmap, and pass it to each callback */
				tempSparseArray.put(bitmapID, bitmaps.get(bitmapID));
				
				for (IconObtainedCallback callback : callbacksForID)
				{
					/* It shouldn't be null, but we'll check anyway */
					if (callback != null) callback.iconsObtained(tempSparseArray);
				}
				
				tempSparseArray.clear();
			}
			
			/* remove the id from the pending requests, and from the "being loaded" list */
			pendingRequests.get(type).remove(bitmapID);
			typesBeingLoaded.get(type).remove(Integer.valueOf(bitmapID));
		}
	}
	
	private class IconLoader extends AsyncTask<Integer, Integer, SparseArray<Bitmap>>
	{
		private IconObtainedCallback callback = null;
		private int type;
		private boolean thumb;
				
		SparseArray<Bitmap> bitmapsLoaded;
		
		public IconLoader(int type, boolean thumb)
		{
			this.type = type;
			this.thumb = thumb;
		}
		
		public IconLoader(int type, IconObtainedCallback callback, boolean thumb)
		{
			this(type, thumb);
			this.callback = callback;
		}
		
		@Override
		protected SparseArray<Bitmap> doInBackground(Integer... ids) 
		{			
			bitmapsLoaded = new SparseArray<Bitmap>(ids.length);
			
			for (int id : ids)
			{
				Bitmap bitmapLoaded = null;
				
				try { bitmapLoaded = loadBitmapFromStorage(id, type, thumb); }
				catch (Exception e) {  } 
				
				/* There was nothing loaded locally, try to download the image */
				if (bitmapLoaded == null)
				{					
					try { bitmapLoaded = retrieveBitmapFromServer(id, type, thumb); }
					catch (Exception e) { e.printStackTrace(); }
				}
				
				if (bitmapLoaded != null) bitmapCaches[type].put(id, bitmapLoaded);
				bitmapsLoaded.put(id, bitmapLoaded);
				
				publishProgress(id);
			}
			return bitmapsLoaded;
		}
		
		@Override
		protected void onProgressUpdate(Integer... id)
		{
			if (pendingRequests.get(type).get(id[0]) != null && pendingRequests.get(type).get(id[0]).size() > 0)
			{
				SparseArray<Bitmap> bitmapJustLoaded = new SparseArray<Bitmap>();
				bitmapJustLoaded.put(id[0], bitmapsLoaded.get(id[0]));
				
				for (IconObtainedCallback request : pendingRequests.get(type).get(id[0])) request.iconsObtained(bitmapJustLoaded);
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
	
	/**
	 * Attempts to load the specified image from storage.
	 * 
	 * @param id the id of the image to load
	 * @param type {@link ImageService#CHAR}, {@link ImageService#CORP}, or {@link ImageService#ICON}
	 * @return the {@link Bitmap} loaded from storage
	 * @throws IOException if the image does not exist
	 */
	private Bitmap loadBitmapFromStorage(int id, int type, boolean thumb) throws IOException
	{
		int fileSize = thumb ? imageSize[type]/4 : imageSize[type];
		
		FileInputStream fis = context.openFileInput(localImagePrefix[type] + fileSize + "_" + id + ".png");
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
	private Bitmap retrieveBitmapFromServer(int id, int type, boolean thumb) throws MalformedURLException, IOException
	{
		Bitmap bitmapLoaded = null;
		
		String imageURL = url[type] + id + "_" + imageSize[type] + imageExtension[type];
	
		InputStream in = new java.net.URL(imageURL).openStream();
		bitmapLoaded = BitmapFactory.decodeStream(in);
			
		if (bitmapLoaded != null) {
			saveBitmap(bitmapLoaded, id, type);
			if (thumb) bitmapLoaded = Bitmap.createScaledBitmap(bitmapLoaded, imageSize[type]/4, imageSize[type]/4, true);
		}
		
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
			FileOutputStream out = context.openFileOutput(localImagePrefix[type] + imageSize[type] + "_" + id + ".png", 0);
			image.compress(Bitmap.CompressFormat.PNG, 100, out);
			out.close();
			
			out = context.openFileOutput(localImagePrefix[type] + imageSize[type]/4 + "_" + id + ".png", 0);
			Bitmap scaledImage = Bitmap.createScaledBitmap(image, imageSize[type]/4, imageSize[type]/4, true);
			scaledImage.compress(Bitmap.CompressFormat.PNG, 100, out);
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
					return value.getRowBytes() * value.getHeight();
				}
		    };
		}
	}
}
