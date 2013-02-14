package com.zdonnell.eve.api.priceservice;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.util.SparseArray;

public class PriceDatabase {

	private Context context;

	private SQLiteDatabase db;

	private final String DB_NAME = "price_db";
	private final int DB_VERSION = 1;

	public final static String TABLE_NAME = "prices";

	public final static String TABLE_ID = "_id";
	public final static String TABLE_PRICE = "price";
	public final static String TABLE_DATE_SET = "date";

	public PriceDatabase(Context context) 
	{
		this.context = context;

		CustomSQLiteOpenHelper helper = new CustomSQLiteOpenHelper(context);
		this.db = helper.getWritableDatabase();
	}

	/**
	 * Gets the prices for a set of typeIDs
	 * 
	 * @param typeIDs int array of typeIDs
	 * @return {@link SparseArray} containing the prices
	 */
	public SparseArray<Float> getPrices(Integer[] typeIDs, long validCacheAge)
	{
		SparseArray<Float> returnSparseArray = new SparseArray<Float>(typeIDs.length);
		 		
 		Cursor c;
 		
 		String whereClause = TABLE_ID + " IN (?";
		String[] typeIDStrings = new String[typeIDs.length];
		
		for (int i = 0; i < typeIDs.length; i++)	
		{
			if (i != typeIDs.length - 1) whereClause += ",?";
			typeIDStrings[i] = String.valueOf(typeIDs[i]);
		}
		
		c = db.query(TABLE_NAME, null, whereClause + ")", typeIDStrings, null, null, null);
 		
 		while (c.moveToNext())
 		{
 			int typeID = c.getInt(0);
			float price = c.getFloat(1);
			long timeSet = c.getLong(2);
			
			/* 
			 * If the entry for the typeID is older than required by validCacheAge, don't enter it into the sparse array.
			 * A non entry will force the PriceService to query the price server for it
			 */
			if (System.currentTimeMillis() - validCacheAge < timeSet)
			{
				returnSparseArray.put(typeID, price);
			}
 		}
 		
 		c.close();
		
		return returnSparseArray;
	}
	
	/**
	 * Obtains the price for one typeID
	 * 
	 * @see #getPrices(int[])
	 * 
	 * @param typeID
	 * @return
	 */
	public float getPrice(int typeID, long validCacheAge)
	{
		Integer[] priceArray = new Integer[1];
		priceArray[0] = typeID;
		
		SparseArray<Float> returnedPriceArray = getPrices(priceArray, validCacheAge);
		
		return returnedPriceArray.get(typeID); 
	}
	
	
	/**
	 * Inserts a set of Prices associated with typeIDs into the database
	 * 
	 * TODO optimize insertion
	 * 
	 * @param prices
	 */
	public void setPrices(SparseArray<Float> prices)
	{
		ContentValues tempPriceTypeID = new ContentValues();
		
		for (int i = 0; i < prices.size(); i++)
		{
			tempPriceTypeID.put(TABLE_ID, prices.keyAt(i));
			tempPriceTypeID.put(TABLE_PRICE, prices.valueAt(i));
			tempPriceTypeID.put(TABLE_DATE_SET, System.currentTimeMillis());

			try
			{
				db.insertOrThrow(TABLE_NAME, null, tempPriceTypeID);
			}
			catch (SQLiteConstraintException e)
			{
				db.replace(TABLE_NAME, null, tempPriceTypeID);
			}
			
			tempPriceTypeID.clear();
		}
	}
	
	public void setPrice(int typeID, float price)
	{
		SparseArray<Float> builtSparseArray = new SparseArray<Float>(1);
		builtSparseArray.put(typeID, price);
		
		setPrices(builtSparseArray);
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
					+ TABLE_ID + " integer primary key not null,"
					+ TABLE_PRICE + " real,"
					+ TABLE_DATE_SET + " integer,"
					+ "UNIQUE (" + TABLE_ID + ") ON CONFLICT REPLACE);";
	
			db.execSQL(newTableQueryString);
		}
	
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) 
		{
	
		}
	}
}
