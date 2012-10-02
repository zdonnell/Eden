package com.zdonnell.eve.api;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.apache.http.NameValuePair;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class CacheDatabase {

	// the Activity or Application that is creating an object from this class.
	Context context;

	// a reference to the database used by this application/object
	private SQLiteDatabase db;

	// These constants are specific to the database. They should be
	// changed to suit your needs.
	private final String DB_NAME = "cache_db";
	private final int DB_VERSION = 1;

	public final static String TABLE_NAME = "cache_status";

	public final static String TABLE_ID = "_id";
	public final static String TABLE_RESULT_URL = "result_url";
	public final static String TABLE_ACTOR_ID = "cache_unique";
	public final static String TABLE_EXPIRE = "cached_until";
	public final static String TABLE_RAW_RESPONSE = "cached_raw_response";


	public CacheDatabase(Context context) 
	{
		this.context = context;

		CustomSQLiteOpenHelper helper = new CustomSQLiteOpenHelper(context);
		this.db = helper.getWritableDatabase();
	}

	/**
	 * Checks whether a given data request is still cached
	 * 
	 * @param URL the request URL
	 * @param actorID the relevant ID, could be accountID, charID, corpID, etc.
	 * @return
	 */
	public boolean isCached(String URL, NameValuePair[] actorIDs, boolean refreshCache)
	{
		boolean isCached = false;
		String uniqueIDString = "";
		
		for (NameValuePair ID : actorIDs) uniqueIDString += ID.getValue();
		
		String[] insertValues = new String[] { URL, uniqueIDString };
		String query = "SELECT " + TABLE_EXPIRE + " FROM cache_status WHERE " + TABLE_RESULT_URL + "=? AND " + TABLE_ACTOR_ID + "=?";
		Cursor c = db.rawQuery(query, insertValues);
		
		/*
		 * Check to see if there is even an entry, if there is, parse and
		 * compare
		 */
		if (c.moveToFirst()) {
			String dateTime = c.getString(0);
			c.close();

			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
			Date cachedUntil = new Date();

			try 
			{
				cachedUntil = formatter.parse(dateTime);
			}
			catch (ParseException e) 
			{
				/*
				 * In error, it will be assumed that the request should query
				 * the server
				 */
				isCached = false;
				e.printStackTrace();
			}

			Date now = Calendar.getInstance().getTime();

			if (now.before(cachedUntil)) 
			{
				isCached = true;
			}
			else if (!refreshCache)
			{
				isCached = true;
			}
		}

		return isCached;
	}

	/**
	 * 
	 * @param URL
	 * @param actorIDs
	 * @return
	 */
	public String getCachedResource(String URL, NameValuePair[] actorIDs)
	{
		String uniqueIDString = "", rawResource = "";
		
		for (NameValuePair ID : actorIDs) uniqueIDString += ID.getValue();
		
		String query = "SELECT " + TABLE_RAW_RESPONSE + " FROM cache_status WHERE " + TABLE_RESULT_URL + "=? AND " + TABLE_ACTOR_ID + "=?";
		Cursor c = db.rawQuery(query, new String[] { URL, uniqueIDString });
		
		if (c.moveToFirst()) rawResource = c.getString(0);
		c.close();
		
		return rawResource;
	}
	
	/**
	 * Sets the Date and Time that the request will be cached for on the server.
	 * 
	 * If a cache expiration date already exists for the data requested, it will
	 * be replaced.
	 * 
	 * @param URL The URL of the request
	 * @param uniqueIDs the relevant ID, could be accountID, charID, corpID, etc.
	 * @param cachedUntil The time at which the cache expires (in <B>yyyy-MM-dd HH:mm:ss</B> format)
	 */
	public void updateCache(String URL, NameValuePair[] uniqueIDs, String rawResponse) 
	{
		String actorIDString = "";
		
		for (NameValuePair ID : uniqueIDs) actorIDString += ID.getValue();
		
		String cachedUntil = ResourceManager.buildDocument(rawResponse).getElementsByTagName("cachedUntil").item(0).getTextContent();
		
		ContentValues values = new ContentValues();
		values.put(TABLE_RESULT_URL, URL);
		values.put(TABLE_ACTOR_ID, actorIDString);
		values.put(TABLE_EXPIRE, cachedUntil);
		values.put(TABLE_RAW_RESPONSE, rawResponse);

		try
		{ 
			db.insert(TABLE_NAME, null, values); 
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	private class CustomSQLiteOpenHelper extends SQLiteOpenHelper 
	{
		public CustomSQLiteOpenHelper(Context context) 
		{
			super(context, DB_NAME, null, DB_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) 
		{
			String newTableQueryString = "create table " + TABLE_NAME + " ("
					+ TABLE_ID + " integer primary key autoincrement not null,"
					+ TABLE_RESULT_URL + " text,"
					+ TABLE_ACTOR_ID + " integer,"
					+ TABLE_RAW_RESPONSE + " text,"
					+ TABLE_EXPIRE + " text,"
					+ "UNIQUE (" + TABLE_ACTOR_ID + ", " + TABLE_RESULT_URL + ") ON CONFLICT REPLACE);";

			db.execSQL(newTableQueryString);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) 
		{

		}
	}
}
