package com.zdonnell.eve.database;

import android.database.sqlite.SQLiteDatabase;

public class Database {

	public static final String DB_NAME = "api_data_db";
	public static final int DB_VERSION = 1;
	
	public static void onCreate(SQLiteDatabase db)
	{
		createSkillsTable(db);
		createSkillTreeTable(db);
		createSkillQueueTable(db);
		createCharacterInfoTable(db);
	}
	
	public static void onUpdate(SQLiteDatabase db, int oldVersion, int newVersion)
	{
		
	}
	
	private static void createCharacterInfoTable(SQLiteDatabase db)
	{
		String newTableQueryString = "create table " + CharacterInfoData.TABLE + " ("
				
				+ CharacterInfoData.COL_CHARACTER_ID + " integer primary key not null," 
				+ CharacterInfoData.COL_RACE + " text," 
				+ CharacterInfoData.COL_BLOODLINE + " text,"
				+ CharacterInfoData.COL_WALLET_BALLANCE + " real,"
				+ CharacterInfoData.COL_SKILLPOINTS + " integer,"
				+ CharacterInfoData.COL_SHIP_NAME + " text,"
				+ CharacterInfoData.COL_SHIP_TYPEID + " integer,"
				+ CharacterInfoData.COL_CORP_DATE + " text,"
				+ CharacterInfoData.COL_ALLIANCE_ID + " integer,"
				+ CharacterInfoData.COL_ALLIANCE + " text,"		
				+ CharacterInfoData.COL_ALLIANCE_DATE + " text,"				
				+ CharacterInfoData.COL_LAST_KNOWN_LOC + " text,"				
				+ CharacterInfoData.COL_SEC_STATUS + " real,"				

				+ "UNIQUE (" + CharacterInfoData.COL_CHARACTER_ID + ") ON CONFLICT REPLACE);";

		db.execSQL(newTableQueryString);
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
	
	private static void createSkillTreeTable(SQLiteDatabase db)
	{
		String newTableQueryString = "create table " + SkillTree.TABLE + " ("
				+ SkillTree.COL_SKILL_TYPE_ID + " integer primary key not null," 
				+ SkillTree.COL_SKILL_NAME + " text," 
				+ SkillTree.COL_SKILL_DESCRIPTION + " text,"
				+ SkillTree.COL_SKILL_RANK + " integer,"
				+ SkillTree.COL_SKILL_PUBLISHED + " integer,"
				+ SkillTree.COL_SKILL_PRIM_ATTR + " integer,"
				+ SkillTree.COL_SKILL_SEC_ATTR + " integer,"
				+ SkillTree.COL_SKILL_PREREQUESITES + " text,"
				+ SkillTree.COL_SKILL_GROUP_ID + " integer,"
				+ SkillTree.COL_SKILL_GROUP_NAME + " text,"				
				+ "UNIQUE (" + SkillTree.COL_SKILL_TYPE_ID + ") ON CONFLICT REPLACE);";

		db.execSQL(newTableQueryString);
	}
	
	private static void createSkillQueueTable(SQLiteDatabase db)
	{		
		String newTableQueryString = "create table " + SkillQueueData.TABLE + " ("
				+ SkillQueueData.COL_UNIQUE_ID + " integer primary key not null," 
				+ SkillQueueData.COL_CHAR_ID + " integer," 
				+ SkillQueueData.COL_TYPEID + " integer,"
				+ SkillQueueData.COL_START_SP + " integer,"
				+ SkillQueueData.COL_END_SP + " integer,"
				+ SkillQueueData.COL_LEVEL + " integer,"
				+ SkillQueueData.COL_START_TIME + " text,"
				+ SkillQueueData.COL_END_TIME + " text,"
				+ SkillQueueData.COL_POSITION + " integer,"				
				+ "UNIQUE (" + SkillQueueData.COL_CHAR_ID + ", " + SkillQueueData.COL_POSITION + ") ON CONFLICT REPLACE);";

		db.execSQL(newTableQueryString);
	}
}
