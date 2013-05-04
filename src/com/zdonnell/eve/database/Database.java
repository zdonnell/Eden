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
		String newTableQueryString = "create table " + SkillsData.TABLE + " ("
				+ SkillsData.COL_UNIQUE_ID + " integer primary key not null," 
				+ SkillsData.COL_CHAR_ID + " integer," 
				+ SkillsData.COL_TYPEID + " integer,"
				+ SkillsData.COL_SKILLPOINTS + " integer,"
				+ SkillsData.COL_LEVEL + " integer,"
				+ SkillsData.COL_UNPUBLISHED + " integer,"
				+ "UNIQUE (" + SkillsData.COL_CHAR_ID + ", " + SkillsData.COL_TYPEID + ") ON CONFLICT REPLACE);";

		db.execSQL(newTableQueryString);
	}
}
