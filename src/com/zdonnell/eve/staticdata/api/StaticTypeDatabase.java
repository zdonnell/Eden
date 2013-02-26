package com.zdonnell.eve.staticdata.api;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.util.SparseArray;

public class StaticTypeDatabase 
{
	Context context;

	private SQLiteDatabase db;

	private final String DB_NAME = "static_db";
	private final int DB_VERSION = 1;

	/* TABLE SPECIFICATION STRINGS */
	public final static String TABLE_NAME = "type_info";
	public final static String TABLE_ROW_ID = "_id";
	public final static String TABLE_ROW_GROUPID = "group_id";
	public final static String TABLE_ROW_MARKETGROUPID = "market_group_id";
	public final static String TABLE_ROW_TYPENAME = "type_name";
	public final static String TABLE_ROW_DESCRIPTION = "table_row_description";
	public final static String TABLE_ROW_M3 = "m3";	
	
	public StaticTypeDatabase(Context context)
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
	public SparseArray<TypeInfo> getTypeInfo(Integer... typeIDs)
	{
		SparseArray<TypeInfo> typeInfoSet = new SparseArray<TypeInfo>(typeIDs.length);
		
		Cursor c;
		
		String whereClause = TABLE_ROW_ID + " IN (?";
		String[] typeIDStrings = new String[typeIDs.length];
		
		for (int i = 0; i < typeIDs.length; i++)	
		{
			if (i != typeIDs.length - 1) whereClause += ",?";
			typeIDStrings[i] = String.valueOf(typeIDs[i]);
		}
				
		c = db.query(TABLE_NAME, null, whereClause + ")", typeIDStrings, null, null, null);

		/* TODO add check to see if the local data is "up to date" */
		while (c.moveToNext())
		{
			TypeInfo typeInfo = new TypeInfo();
			typeInfo.typeID = c.getInt(c.getColumnIndex(TABLE_ROW_ID));
			typeInfo.groupID = c.getInt(c.getColumnIndex(TABLE_ROW_GROUPID));
			typeInfo.marketGroupID = c.getInt(c.getColumnIndex(TABLE_ROW_MARKETGROUPID));
			typeInfo.typeName = c.getString(c.getColumnIndex(TABLE_ROW_TYPENAME));
			typeInfo.description = c.getString(c.getColumnIndex(TABLE_ROW_DESCRIPTION));
			typeInfo.m3 = c.getFloat(c.getColumnIndex(TABLE_ROW_M3));

			typeInfoSet.put(typeInfo.typeID, typeInfo);
		}
		
		c.close();
		
		return typeInfoSet;
	}
	
	public void insertTypeInfo(SparseArray<TypeInfo> typeInfoSet)
	{
		TypeInfo[] typeInfoArray = new TypeInfo[typeInfoSet.size()];
		
		for (int i = 0; i < typeInfoSet.size(); ++i)
		{
			typeInfoArray[i] = typeInfoSet.valueAt(i);
		}
		
		insertTypeInfo(typeInfoArray);
	}
	
	/**
	 * Used to update the database with the newest info for a type
	 * 
	 * @param typeInfoSet an Array of {@link TypeInfo} objects for insertion
	 */
	public void insertTypeInfo(TypeInfo... typeInfoSet)
	{
		for (TypeInfo typeInfo : typeInfoSet)
		{
			ContentValues insertValues = new ContentValues();
			insertValues.put(TABLE_ROW_ID, typeInfo.typeID);
			insertValues.put(TABLE_ROW_GROUPID, typeInfo.groupID);
			insertValues.put(TABLE_ROW_MARKETGROUPID, typeInfo.marketGroupID);
			insertValues.put(TABLE_ROW_TYPENAME, typeInfo.typeName);
			insertValues.put(TABLE_ROW_DESCRIPTION, typeInfo.description);
			insertValues.put(TABLE_ROW_M3, typeInfo.m3);
		
			db.insert(TABLE_NAME, null, insertValues);
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
					+ TABLE_ROW_GROUPID + " integer,"
					+ TABLE_ROW_MARKETGROUPID + " integer,"
					+ TABLE_ROW_TYPENAME + " text,"
					+ TABLE_ROW_DESCRIPTION + " text,"
					+ TABLE_ROW_M3 + " real,"					
					+ "UNIQUE (" + TABLE_ROW_ID + ") ON CONFLICT REPLACE);";

			db.execSQL(newTableQueryString);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) 
		{

		}
	}
}
