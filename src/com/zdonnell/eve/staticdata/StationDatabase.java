package com.zdonnell.eve.staticdata;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.SparseArray;

public class StationDatabase 
{
	Context context;

	private SQLiteDatabase db;

	private final String DB_NAME = "station_db";
	private final int DB_VERSION = 1;

	/* TABLE SPECIFICATION STRINGS */
	public final static String TABLE_NAME = "station_info";
	public final static String TABLE_ROW_ID = "_id";
	public final static String TABLE_ROW_STATIONTYPEID = "station_type_id";
	public final static String TABLE_ROW_STATIONNAME = "station_name";
	
	
	public StationDatabase(Context context)
	{
		this.context = context;

		CustomSQLiteOpenHelper helper = new CustomSQLiteOpenHelper(context);
		this.db = helper.getWritableDatabase();
	}
	
	/**
	 * Used to obtain only up to date information for a given set of typeIDs
	 * 
	 * @param typeIDs an Array of typeIDs to request information for
	 * @return a {@link SparseArray} of up to date {@link TypeInfo} objects, this will not
	 * contain values for non stored typeIDs or typeIDs whose entry is "out of date."  This returned
	 * SparseArray should be compared against the original ID list to determine which types had no
	 * information stored.
	 */
	public SparseArray<StationInfo> getStationInfo(Integer... stationIDs)
	{
		SparseArray<StationInfo> typeInfoSet = new SparseArray<StationInfo>(stationIDs.length);
		
		Cursor c;
		
		String whereClause = TABLE_ROW_ID + " IN (?";
		String[] typeIDStrings = new String[stationIDs.length];
		
		for (int i = 0; i < stationIDs.length; i++)	
		{
			if (i != stationIDs.length - 1) whereClause += ",?";
			typeIDStrings[i] = String.valueOf(stationIDs[i]);
		}
				
		c = db.query(TABLE_NAME, null, whereClause + ")", typeIDStrings, null, null, null);

		/* TODO add check to see if the local data is "up to date" */
		while (c.moveToNext())
		{
			StationInfo typeInfo = new StationInfo();
			typeInfo.stationID = c.getInt(c.getColumnIndex(TABLE_ROW_ID));
			typeInfo.stationTypeID = c.getInt(c.getColumnIndex(TABLE_ROW_STATIONTYPEID));
			typeInfo.stationName = c.getString(c.getColumnIndex(TABLE_ROW_STATIONNAME));

			typeInfoSet.put(typeInfo.stationID, typeInfo);
		}
		
		c.close();
				
		return typeInfoSet;
	}
	
	public void insertStationInfo(SparseArray<StationInfo> stationInfoSet)
	{
		StationInfo[] typeInfoArray = new StationInfo[stationInfoSet.size()];
		
		for (int i = 0; i < stationInfoSet.size(); ++i)
		{
			typeInfoArray[i] = stationInfoSet.valueAt(i);
		}
		
		insertStationInfo(typeInfoArray);
	}
	
	/**
	 * Used to update the database with the newest info for a type
	 * 
	 * @param typeInfoSet an Array of {@link TypeInfo} objects for insertion
	 */
	public void insertStationInfo(StationInfo... typeInfoSet)
	{
		try
		{
			db.beginTransaction();
			
			for (StationInfo typeInfo : typeInfoSet)
			{
				ContentValues insertValues = new ContentValues();
				insertValues.put(TABLE_ROW_ID, typeInfo.stationID);
				insertValues.put(TABLE_ROW_STATIONTYPEID, typeInfo.stationTypeID);
				insertValues.put(TABLE_ROW_STATIONNAME, typeInfo.stationName);
						
				db.insertWithOnConflict(TABLE_NAME, null, insertValues, SQLiteDatabase.CONFLICT_REPLACE);
			}
			
			db.setTransactionSuccessful();
		}
		catch (SQLException e) { }
		finally
		{
			db.endTransaction();
		}
	}
	
	
	/**
	 * Basic helper class to create/update the raw sqlite database
	 * 
	 * @author Zach
	 *
	 */
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
					+ TABLE_ROW_ID + " integer primary key not null,"
					+ TABLE_ROW_STATIONTYPEID + " integer,"
					+ TABLE_ROW_STATIONNAME + " text,"
					+ "UNIQUE (" + TABLE_ROW_ID + ") ON CONFLICT REPLACE);";

			db.execSQL(newTableQueryString);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) 
		{

		}
	}
}
