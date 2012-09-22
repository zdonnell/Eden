package com.zdonnell.eve;

import java.util.ArrayList;

import com.zdonnell.eve.api.account.EveCharacter;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class CharacterDB {
	// the Activity or Application that is creating an object from this class.
	Context context;

	// a reference to the database used by this application/object
	private SQLiteDatabase db;

	// These constants are specific to the database. They should be
	// changed to suit your needs.
	private final String DB_NAME = "char_db";
	private final int DB_VERSION = 1;

	// These constants are specific to the database table. They should be
	// changed to suit your needs.
	public final static String CHAR_TABLE = "char_table";

	public final static String CHAR_TABLE_ID = "_id";

	public final static String CHAR_TABLE_NAME = "char_name";
	public final static String CHAR_TABLE_EVEID = "char_eveid";
	public final static String CHAR_TABLE_CORPID = "char_corpid";
	public final static String CHAR_TABLE_CORPNAME = "char_corpname";

	public CharacterDB(Context context) {
		this.context = context;

		// create or open the database
		CustomSQLiteOpenHelper helper = new CustomSQLiteOpenHelper(context);
		this.db = helper.getWritableDatabase();
	}

	/**********************************************************************
	 * ADDING A ROW TO THE DATABASE TABLE
	 * 
	 * This is an example of how to add a row to a database table using this
	 * class. You should edit this method to suit your needs.
	 * 
	 * the key is automatically assigned by the database
	 * 
	 * @param rowStringOne
	 *            the value for the row's first column
	 * @param rowStringTwo
	 *            the value for the row's second column
	 */
	public void addCharacter(EveCharacter character) {
		// this is a key value pair holder used by android's SQLite functions
		ContentValues values = new ContentValues();
		values.put(CHAR_TABLE_NAME, character.name);
		values.put(CHAR_TABLE_EVEID, character.charID);
		values.put(CHAR_TABLE_CORPNAME, character.corpName);
		values.put(CHAR_TABLE_CORPID, character.corpID);

		// ask the database object to insert the new data
		try {

			db.insert(CHAR_TABLE_NAME, null, values);
		} catch (Exception e) {

			Log.e("DB ERROR", e.toString());
			e.printStackTrace();
		}
	}

	/**********************************************************************
	 * RETRIEVING ALL ROWS FROM THE DATABASE TABLE
	 * 
	 * This is an example of how to retrieve all data from a database table
	 * using this class. You should edit this method to suit your needs.
	 * 
	 * the key is automatically assigned by the database
	 */

	public Cursor allCharacters() {

		// this is a database call that creates a "cursor" object.
		// the cursor object store the information collected from the
		// database and is used to iterate through the data.
		Cursor cursor = null;

		try {
			// ask the database object to create the cursor.
			cursor = db.query(CHAR_TABLE_NAME, new String[] { CHAR_TABLE_ID, CHAR_TABLE_NAME, CHAR_TABLE_EVEID, CHAR_TABLE_CORPNAME, CHAR_TABLE_CORPID },
					null, null, null, null, null);

		} catch (SQLException e) {
			Log.e("DB Error", e.toString());
			e.printStackTrace();
		}

		// return the ArrayList that holds the data collected from
		// the database.
		return cursor;
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
			String newTableQueryString = "create table " + CHAR_TABLE_NAME
					+ " (" + CHAR_TABLE_ID
					+ " integer primary key autoincrement not null,"
					+ CHAR_TABLE_NAME + " string," + CHAR_TABLE_EVEID
					+ " integer," + CHAR_TABLE_CORPNAME + " string,"
					+ CHAR_TABLE_CORPID + " integer" + ");";
			// execute the query string to the database.
			db.execSQL(newTableQueryString);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

		}
	}
}
