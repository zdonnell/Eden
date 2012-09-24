package com.zdonnell.eve;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

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
		
		subURLs[CHAR] = "Characters/";
		subURLs[CORP] = "Corporations/";
	}
	
	/**
	 * Hashmap to store loaded images in memory
	 */
	private HashMap<Integer, Bitmap> memCache = new HashMap<Integer, Bitmap>();;
	
	private Context context;

	public ImageService(Context context)
	{
		this.context = context;
	}
	
	/**
	 * Takes an ImageView and Character or Corporation ID and sets the appropriate
	 * image
	 * 
	 * @param view The ImageView to update with the acquired bitmap
	 * @param actorID The Corporation or Character ID
	 * @param type {@link CHAR} or {@link CORP}
	 */
	public void setPortrait(ImageView view, int actorID, int type) 
	{
		Bitmap cachedBM = null;

		/* Check if the image is already loaded into memory */
		if (memCache.containsKey(actorID)) view.setImageBitmap(memCache.get(actorID));
		else 
		{		
			/* Try to load it from storage */
			try 
			{
				FileInputStream fis = context.openFileInput(actorID + ".png");
				BufferedInputStream buf = new BufferedInputStream(fis);
	
				byte[] bitmapBytes = new byte[buf.available()];
				buf.read(bitmapBytes);
	
				cachedBM = BitmapFactory.decodeByteArray(bitmapBytes, 0, bitmapBytes.length);
				memCache.put(actorID, cachedBM);
	
				if (fis != null) fis.close();
				if (buf != null) buf.close();
	
			} catch (FileNotFoundException e) {
				// e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
	
			if (cachedBM != null) view.setImageBitmap(cachedBM);
			
			/* The image isn't stored anywhere, download it */
			else 
			{
				/* Assemble the proper image URL */
				String imageURL = baseImageURL + subURLs[type] + actorID + "_" + dimensions[type] + imageExtensions[type];
	
				DownloadImageTask getImage = new DownloadImageTask(view, actorID);
				getImage.execute(imageURL);
			}
		}
	}

	/**
	 * Saves the specified bitmap to local storage
	 * 
	 * @param result The Bitmap to save
	 * @param actorID The Char or Corp ID
	 */
	private void cacheBitmap(Bitmap result, int actorID) 
	{		
		try 
		{
			FileOutputStream out = context.openFileOutput(actorID + ".png", 0);
			result.compress(Bitmap.CompressFormat.PNG, 75, out);
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Class to manage the downloading of images from the Eve Online
	 * Image Server Asynchronously.
	 * 
	 * @author Zach
	 *
	 */
	private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
		ImageView bmImage;
		int actorID;

		public DownloadImageTask(ImageView bmImage, int actorID) 
		{
			this.bmImage = bmImage;
			this.actorID = actorID;
		}

		protected Bitmap doInBackground(String... urls) 
		{
			String urldisplay = urls[0];
			Bitmap mIcon11 = null;
			try {
				InputStream in = new java.net.URL(urldisplay).openStream();
				mIcon11 = BitmapFactory.decodeStream(in);
			} catch (Exception e) {
				Log.e("Error", e.getMessage());
				e.printStackTrace();
			}
			return mIcon11;
		}

		protected void onPostExecute(Bitmap result) 
		{
			if (bmImage != null) bmImage.setImageBitmap(result);
			memCache.put(actorID, result);
			cacheBitmap(result, actorID);
		}
	}
}
