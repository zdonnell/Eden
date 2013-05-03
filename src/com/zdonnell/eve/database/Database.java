package com.zdonnell.eve.database;

import android.database.sqlite.SQLiteDatabase;

public class Database {

	public static final String DB_NAME = "api_data_db";
	public static final int DB_VERSION = 1;
	
	public static void onCreate(SQLiteDatabase db)
	{
		createSkillsTable(db);
	}
	
	public static void onUpdate(SQLiteDatabase db, int oldVersion, int newVersion)
	{
		
	}
	
	private static void createSkillsTable(SQLiteDatabase db)
	{
		String newTableQueryString = "create table " + Skills.TABLE + " ("
				+ Skills.COL_UNIQUE_ID + " integer autoincrement primary key not null," 
				+ Skills.COL_CHAR_ID + " integer," 
				+ Skills.COL_TYPEID + " integer,"
				+ Skills.COL_SKILLPOINTS + " integer,"
				+ Skills.COL_LEVEL + " integer,"
				+ Skills.COL_UNPUBLISHED + " integer,"
				+ "UNIQUE (" + Skills.COL_CHAR_ID + ", " + Skills.COL_TYPEID + ") ON CONFLICT REPLACE);";

		db.execSQL(newTableQueryString);
	}
}
