package com.zdonnell.eve.api;

import java.util.ArrayList;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class StaticTypes {
	
	Context context;

	private SQLiteDatabase db;
	
	private final String DB_NAME = "static_types";
	private final int DB_VERSION = 1;

	public StaticTypes(Context context) 
	{
		this.context = context;

		StaticTypeSQLHelper helper = new StaticTypeSQLHelper(context);
		this.db = helper.getWritableDatabase();
	}

	public StaticTypeInfo getTypeInformation()
	{
		return null;
	}
	
	public String getTypeName() {
		return null;
	}
	
	private class StaticTypeSQLHelper extends SQLiteOpenHelper 
	{
		public StaticTypeSQLHelper(Context context) 
		{
			super(context, DB_NAME, null, DB_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) 
		{
			
			db.execSQL("");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) 
		{

		}
	}
}
