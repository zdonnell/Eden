package com.zdonnell.eve.api;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.SparseArray;

/**
 * If a static type info database is unavailable, this class should be used to acquire type names
 * 
 * @author zachd
 *
 */
public class TypeNameDatabase {

	// a reference to the database used by this application/object
	private SQLiteDatabase db;
	private Context context;
	
	private final String DB_NAME = "type_name_db";
	private final int DB_VERSION = 1;

	public final static String TABLE_NAME = "type_names";

	public final static String TABLE_ID = "_id";
	public final static String TABLE_TYPE_NAME = "type_name";
	
	public TypeNameDatabase(Context context)
	{
		this.context = context;

		CustomSQLiteOpenHelper helper = new CustomSQLiteOpenHelper(context);
		this.db = helper.getWritableDatabase();
	}
	
	public SparseArray<String> getTypeNames(int... typeIDs)
	{		
		String rawQuery = "SELECT * FROM " + TABLE_NAME + " WHERE " + TABLE_ID + " = ";
		
		for (int x = 0; x < typeIDs.length; x++)
		{	
			rawQuery += typeIDs[x];
			if (x != typeIDs.length - 1) rawQuery += " OR ";
		}
		
		Cursor c = db.rawQuery(rawQuery, null);
		SparseArray<String> returnNames = new SparseArray<String>(c.getCount());
		
		while (c.moveToNext()) returnNames.put(c.getInt(0), c.getString(1));
		c.close();
		
		return returnNames;
	}
	
	public void setTypeNames(SparseArray<String> names)
	{
		for (int i = 0; i < names.size(); i++) 
		{
			ContentValues values = new ContentValues(2);
			values.put(TABLE_ID, names.keyAt(i));
			values.put(TABLE_TYPE_NAME, names.valueAt(i));
			
			db.insert(TABLE_NAME, null, values);
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
					+ TABLE_TYPE_NAME + " text)";

			db.execSQL(newTableQueryString);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) 
		{

		}
	}
}
