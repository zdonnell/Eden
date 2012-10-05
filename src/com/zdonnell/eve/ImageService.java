package com.zdonnell.eve;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

/**
 * Class to manage downloading and storage / caching of Eve Online
 * Character and Corporation Images
 * 
 * @author Zach
 *
 */
public class ImageService {

	final static String baseImageURL = "https://image.eveonline.com/";
	
	public final static int CHAR = 0;
	public final static int CORP = 1;
	
	/**
	 * if preCache has been called, this queue will 
	 */
	private Queue<Integer> preCacheQueue = new LinkedList<Integer>();
	
	private HashMap<Integer, ArrayList<ImageView>> viewsToUpdate = new HashMap<Integer, ArrayList<ImageView>>();
	
	/**
	 * Dimensions to save the the portraits / corp logos at
	 */
	public static int[] dimensions = new int[2];
	
	/**
	 * Extension for the images provided by the Eve Online Image server
	 */
	public static String[] imageExtensions = new String[2];
	
	/**
	 * subURL of the image path
	 */
	public static String[] subURLs = new String[2];
	
	static {
		/* Static initialization of type specific value arrays */
		dimensions[CHAR] = 512;
		dimensions[CORP] = 128;
		
		imageExtensions[CHAR] = ".jpg";
		imageExtensions[CORP] = ".png";
		
		subURLs[CHAR] = "Character/";
		subURLs[CORP] = "Corporation/";
	}
	
	/**
	 * Hashmap to store loaded images in memory
	 */
	private HashMap<Integer, Bitmap> memoryImageCache = new HashMap<Integer, Bitmap>();;
	
	private Context context;

	public ImageService(Context context)
	{
		this.context = context;
	}
	
	/** 
	 * @param actorsToPreCache actorsToPreCache[x][0] = actorID, actorsToPreCache[x][1] = actorType.
	 */
	public void preCache(int[][] actorsToPreCache)
	{
		for (int[] actor : actorsToPreCache)
		{
			preCacheQueue.add(actor[0]);
			viewsToUpdate.put(actor[0], new ArrayList<ImageView>());

			new LoadImageTask(actor[0], actor[1], null).execute();
		}
	}
	
	/**
	 * Takes an ImageView and Character or Corporation ID and sets the appropriate
	 * image
	 * 
	 * @param view The ImageView to update with the acquired bitmap
	 * @param actorID The Corporation or Character ID
	 * @param actorType {@link CHAR} or {@link CORP}
	 */
	public void setPortrait(ImageView view, int actorID, int actorType) 
	{
		if (memoryImageCache.containsKey(actorID)) view.setImageBitmap(memoryImageCache.get(actorID));
		else if (preCacheQueue.contains(actorID)) viewsToUpdate.get(actorID).add(view);
		else 
		{	
			new LoadImageTask(actorID, actorType, view).execute();
		}
	}

	/**
	 * Saves the specified bitmap to local storage
	 * 
	 * @param result The Bitmap to save
	 * @param actorID The Char or Corp ID
	 */
	private void saveBitmap(Bitmap result, int actorID) 
	{		
		try 
		{
			FileOutputStream out = context.openFileOutput(actorID + ".png", 0);
			result.compress(Bitmap.CompressFormat.PNG, 75, out);
			out.close();
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 * @param actorID
	 * @param actorType
	 * @param view
	 */
	private void setImageFromHTTP(int actorID, int actorType, ImageView view) 
	{
		String assembledImageURL = baseImageURL;
		assembledImageURL += subURLs[actorType];
		assembledImageURL += actorID;
		assembledImageURL += "_" + dimensions[actorType];
		assembledImageURL += imageExtensions[actorType];
		
		/* Execute the AsyncTask */
		DownloadImageTask getImage = new DownloadImageTask(actorID, view);
		getImage.execute(assembledImageURL);
	}
	
	/**
	 * Class to manage the loading of images from cache Asynchronously.
	 * 
	 * @author Zach
	 *
	 */
	private class LoadImageTask extends AsyncTask<String, Void, Bitmap> 
	{
		private int actorID, actorType;
		private ImageView view;

		/**
		 * @param actorID
		 * @param view the ImageView to update once the image is acquired
		 */
		public LoadImageTask(int actorID, int actorType, ImageView view) 
		{
			this.actorID = actorID;
			this.actorType = actorType;
			this.view = view;
		}

		protected Bitmap doInBackground(String... urls) 
		{
			FileInputStream fis = null;
			BufferedInputStream buf = null;
			Bitmap bitmapFromSD = null;
			
			try 
			{
				fis = context.openFileInput(actorID + ".png");
				buf = new BufferedInputStream(fis);
				
				byte[] bitmapBytes = new byte[buf.available()];
				buf.read(bitmapBytes);
				
				bitmapFromSD = BitmapFactory.decodeByteArray(bitmapBytes, 0, bitmapBytes.length);
				
				if (fis != null) fis.close();
				if (buf != null) buf.close();
			} 
			catch (FileNotFoundException e) 
			{
				e.printStackTrace();
				return null;
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
				return null;
			}
			
			return bitmapFromSD;
		}

		protected void onPostExecute(Bitmap imageServed) 
		{
			if (imageServed != null)
			{
				if (view != null) 
				{
					view.setImageBitmap(imageServed);
					view = null;
				}
				imageDownloaded(actorID, imageServed, false);
			}
			else
			{
				setImageFromHTTP(actorID, actorType, view);
			}
		}
	}

	/**
	 * Class to manage the downloading of images from the Eve Online
	 * Image Server Asynchronously.
	 * 
	 * @author Zach
	 *
	 */
	private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> 
	{
		private int actorID;
		private ImageView view;

		/**
		 * @param actorID
		 * @param view the ImageView to update once the image is acquired
		 */
		public DownloadImageTask(int actorID, ImageView view) 
		{
			this.actorID = actorID;
			this.view = view;
		}

		protected Bitmap doInBackground(String... urls) 
		{
			String imageURL = urls[0];
			Bitmap imageServed = null;
						
			try 
			{
				InputStream in = new java.net.URL(imageURL).openStream();
				imageServed = BitmapFactory.decodeStream(in);
			} 
			catch (Exception e) 
			{
				e.printStackTrace();
			}
			
			return imageServed;
		}

		protected void onPostExecute(Bitmap imageServed) 
		{
			if (imageServed != null)
			{
				if (view != null) 
				{ 
					view.setImageBitmap(imageServed);
					view = null;
				}
				imageDownloaded(actorID, imageServed, true);
			}		
		}
	}
	
	/**
	 * Handles cleanup after an image has been downloaded
	 * 
	 * @param actorID
	 * @param imageServed
	 */
	private void imageDownloaded(int actorID, Bitmap imageServed, boolean save)
	{
		memoryImageCache.put(actorID, imageServed);
		if (save) saveBitmap(imageServed, actorID);
		
		preCacheQueue.remove(actorID);
		
		if (viewsToUpdate != null)
		{
			ArrayList<ImageView> viewsForID = viewsToUpdate.get(actorID);
			
			if (viewsForID != null)
			{
				for (ImageView view : viewsForID) 
				{
					if (view != null) 
					{
						view.setImageBitmap(imageServed);
						view = null;
					}
				}
			}
			viewsToUpdate.remove(actorID);
		}		
	}
}
