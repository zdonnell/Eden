package com.zdonnell.eve;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.beimin.eveapi.account.characters.EveCharacter;
import com.beimin.eveapi.core.ApiAuth;
import com.beimin.eveapi.core.ApiAuthorization;
import com.zdonnell.eve.apilink.account.EdenEveCharacter;

public class CharacterDB {
	// the Activity or Application that is creating an object from this class.
	Context context;

	// a reference to the database used by this application/object
	private SQLiteDatabase db;

	// These constants are specific to the database. They should be
	// changed to suit your needs.
	private final String DB_NAME = "char_db";
	private final int DB_VERSION = 2;

	// These constants are specific to the database table. They should be
	// changed to suit your needs.
	public final static String CHAR_TABLE = "char_table";

	public final static String CHAR_TABLE_NAME = "char_name";
	public final static String CHAR_TABLE_EVEID = "_id";
	public final static String CHAR_TABLE_CORPID = "char_corpid";
	public final static String CHAR_TABLE_CORPNAME = "char_corpname";
	public final static String CHAR_TABLE_KEYID = "char_keyid";
	public final static String CHAR_TABLE_VCODE = "char_vcode";
	public final static String CHAR_TABLE_ENABLED = "char_enabled";
	public final static String CHAR_TABLE_QUEUETIME = "char_queuetime";

	public CharacterDB(Context context) {
		this.context = context;

		// create or open the database
		CustomSQLiteOpenHelper helper = new CustomSQLiteOpenHelper(context);
		this.db = helper.getWritableDatabase();
	}
	
	public void addCharacter(EveCharacter character, ApiAuth<?> apiAuth, boolean enabled) {
		// this is a key value pair holder used by android's SQLite functions
		ContentValues values = new ContentValues();
		values.put(CHAR_TABLE_NAME, character.getName());
		values.put(CHAR_TABLE_EVEID, character.getCharacterID());
		values.put(CHAR_TABLE_CORPNAME, character.getCorporationName());
		values.put(CHAR_TABLE_CORPID, character.getCorporationID());
		values.put(CHAR_TABLE_KEYID, apiAuth.getKeyID());
		values.put(CHAR_TABLE_VCODE, apiAuth.getVCode());
		values.put(CHAR_TABLE_ENABLED, enabled ? "1" : "0");
		values.put(CHAR_TABLE_QUEUETIME, 0);

		// ask the database object to insert the new data
		try {

			db.insert(CHAR_TABLE, null, values);
		} catch (Exception e) {

			Log.e("DB ERROR", e.toString());
			e.printStackTrace();
		}
	}

	public Cursor allCharacters() 
	{
		// this is a database call that creates a "cursor" object.
		// the cursor object store the information collected from the
		// database and is used to iterate through the data.
		Cursor cursor = null;

		try {
			// ask the database object to create the cursor.
			cursor = db.query(CHAR_TABLE, new String[] { CHAR_TABLE_NAME, CHAR_TABLE_EVEID, CHAR_TABLE_CORPNAME, CHAR_TABLE_CORPID, CHAR_TABLE_KEYID, CHAR_TABLE_VCODE, CHAR_TABLE_QUEUETIME },
					null, null, null, null, null);

		} catch (SQLException e) {
			Log.e("DB Error", e.toString());
			e.printStackTrace();
		}
		
		// return the ArrayList that holds the data collected from
		// the database.
		return cursor;
	}
	
	public Cursor getEnabledCharacters() 
	{
		// this is a database call that creates a "cursor" object.
		// the cursor object store the information collected from the
		// database and is used to iterate through the data.
		Cursor cursor = null;

		try {
			// ask the database object to create the cursor.
			cursor = db.query(CHAR_TABLE, new String[] { CHAR_TABLE_EVEID, CHAR_TABLE_NAME, CHAR_TABLE_CORPNAME, CHAR_TABLE_CORPID, CHAR_TABLE_KEYID, CHAR_TABLE_VCODE, CHAR_TABLE_QUEUETIME },
					CHAR_TABLE_ENABLED + " = 1", null, null, null, null);

		} catch (SQLException e) {
			Log.e("DB Error", e.toString());
			e.printStackTrace();
		}
		
		// return the ArrayList that holds the data collected from
		// the database.
		return cursor;
	}
	
	public EdenEveCharacter[] getEnabledCharactersAsArray() 
	{
		// this is a database call that creates a "cursor" object.
		// the cursor object store the information collected from the
		// database and is used to iterate through the data.
		Cursor cursor = null;

		try {
			// ask the database object to create the cursor.
			cursor = db.query(CHAR_TABLE, new String[] { CHAR_TABLE_EVEID, CHAR_TABLE_NAME, CHAR_TABLE_CORPNAME, CHAR_TABLE_CORPID, CHAR_TABLE_KEYID, CHAR_TABLE_VCODE, CHAR_TABLE_QUEUETIME },
					CHAR_TABLE_ENABLED + " = 1", null, null, null, null);

		} catch (SQLException e) {
			Log.e("DB Error", e.toString());
			e.printStackTrace();
		}
		
		ArrayList<EdenEveCharacter> charArrayList = new ArrayList<EdenEveCharacter>(cursor.getCount());
		while (cursor.moveToNext())
		{
			EdenEveCharacter newChar = new EdenEveCharacter();
			
			newChar.setCharacterID(cursor.getInt(0));
			newChar.setName(cursor.getString(1));
			newChar.setCorporationID(cursor.getInt(3));
			newChar.setCorporationName(cursor.getString(2));
			
			ApiAuthorization apiAuth = new ApiAuthorization(cursor.getInt(4), cursor.getInt(0), cursor.getString(5));
			newChar.setApiAuth(apiAuth);
			newChar.setQueueTimeRemaining(cursor.getLong(6));
			
			charArrayList.add(newChar);
		}
		
		EdenEveCharacter[] charArray = new EdenEveCharacter[charArrayList.size()];
		charArrayList.toArray(charArray);
		
		cursor.close();
		// return the ArrayList that holds the data collected from
		// the database.
		return charArray;
	}
	
	public String getCharacterName(int characterID)
	{
		String query = "SELECT " + CHAR_TABLE_NAME + " FROM " + CHAR_TABLE + " WHERE " + CHAR_TABLE_EVEID + "=?";
				
		Cursor c = db.rawQuery(query, new String[]{ String.valueOf(characterID) });
		
		String name = "";
		if (c.moveToFirst()) name = c.getString(0);
		c.close();
		
		return name;
	}
	
	public String getCorpName(int characterID)
	{
		String query = "SELECT " + CHAR_TABLE_CORPNAME + " FROM " + CHAR_TABLE + " WHERE " + CHAR_TABLE_EVEID + "=?";
				
		Cursor c = db.rawQuery(query, new String[]{ String.valueOf(characterID) });
		
		String name = "";
		if (c.moveToFirst()) name = c.getString(0);
		c.close();
		
		return name;
	}
	
	public void setCharEnabled(int characterID, boolean enabled)
	{
		ContentValues values = new ContentValues();
		values.put(CHAR_TABLE_ENABLED, enabled ? "1" : "0");
		
		db.update(CHAR_TABLE, values, CHAR_TABLE_EVEID + " = ?", new String[] { String.valueOf(characterID) });
	}
	
	public void setCharQueueTime(int characterID, long queueTime)
	{
		ContentValues values = new ContentValues();
		values.put(CHAR_TABLE_QUEUETIME, queueTime);
		
		db.update(CHAR_TABLE, values, CHAR_TABLE_EVEID + " = ?", new String[] { String.valueOf(characterID) });
	}
	
	public boolean isCharEnabled(int characterID)
	{
		boolean isEnabled = false;
		
		Cursor cursor = db.query(CHAR_TABLE, new String[] { CHAR_TABLE_ENABLED }, CHAR_TABLE_EVEID + " = " + characterID, null, null, null, null);
		if (cursor.moveToFirst()) isEnabled = (cursor.getInt(0) != 0);
		
		cursor.close();
		
		return isEnabled;
	}
	
	public int deleteCharactersByKeyID(int apiKey)
	{
		return db.delete(CHAR_TABLE, CHAR_TABLE_KEYID + " = ?", new String[] { String.valueOf(apiKey) });
	}

	/**
	 * This class is designed to check if there is a database that currently
	 * exists for the given program. If the database does not exist, it creates
	 * one. After the class ensures that the database exists, this class will
	 * open the database for use. Most of this functionality will be handled by
	 * the SQLiteOpenHelper parent class. The purpose of extending this class is
	 * to tell the class how to create (or update) the database.
	 * 
	 * @author Randall Mitchell
	 * 
	 */
	private class CustomSQLiteOpenHelper extends SQLiteOpenHelper {
		public CustomSQLiteOpenHelper(Context context) {
			super(context, DB_NAME, null, DB_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			// This string is used to create the database. It should
			// be changed to suit your needs.
			String newTableQueryString = "create table " + CHAR_TABLE
					+ " (" + CHAR_TABLE_NAME + " string," 
					+ CHAR_TABLE_EVEID + " integer primary key not null," 
					+ CHAR_TABLE_CORPNAME + " string,"
					+ CHAR_TABLE_CORPID + " integer,"
					+ CHAR_TABLE_KEYID + " integer,"
					+ CHAR_TABLE_VCODE + " text,"
					+ CHAR_TABLE_ENABLED + " integer,"
					+ CHAR_TABLE_QUEUETIME + " integer);";
			// execute the query string to the database.
			db.execSQL(newTableQueryString);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			
			if (oldVersion == 1)
			{
				db.execSQL("alter table " + CHAR_TABLE + " add column " + CHAR_TABLE_QUEUETIME + " integer;");
			}
		}
	}
}
