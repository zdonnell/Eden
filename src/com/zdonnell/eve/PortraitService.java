package com.zdonnell.eve;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

public class PortraitService {

	final static String baseImageURL = "https://image.eveonline.com/Character/";

	final static int dimension = 512;
	
	private Context context;

	public PortraitService(Context context)
	{
		this.context = context;
	}
	
	public void setPortrait(ImageView view, int characterID) 
	{
		Bitmap cachedBM = null;

		try 
		{
			FileInputStream fis = context.openFileInput(characterID + ".jpg");
			BufferedInputStream buf = new BufferedInputStream(fis);

			byte[] bitmapBytes = new byte[buf.available()];
			buf.read(bitmapBytes);

			cachedBM = BitmapFactory.decodeByteArray(bitmapBytes, 0, bitmapBytes.length);

			if (fis != null) fis.close();
			if (buf != null) buf.close();

		} catch (FileNotFoundException e) {
			// e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (cachedBM != null) view.setImageBitmap(cachedBM);
		else 
		{
			String imageURL = baseImageURL + characterID + "_" + dimension + ".jpg";

			DownloadImageTask getImage = new DownloadImageTask(view, characterID);
			getImage.execute(imageURL);
		}
	}

	private void cacheBitmap(Bitmap result, int characterID) 
	{
		try 
		{
			FileOutputStream out = context.openFileOutput(characterID + ".jpg", 0);
			result.compress(Bitmap.CompressFormat.JPEG, 100, out);
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
		ImageView bmImage;
		int characterID;

		public DownloadImageTask(ImageView bmImage, int characterID) 
		{
			this.bmImage = bmImage;
			this.characterID = characterID;
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
			cacheBitmap(result, characterID);
		}
	}
}
