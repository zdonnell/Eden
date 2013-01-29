package com.zdonnell.eve.api;

import java.util.ArrayList;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.zdonnell.eve.api.account.EveCharacter;

public class AccountDB {

	// the Activity or Application that is creating an object from this class.
	Context context;

	// a reference to the database used by this application/object
	private SQLiteDatabase db;

	// These constants are specific to the database. They should be
	// changed to suit your needs.
	private final String DB_NAME = "cache_db";
	private final int DB_VERSION = 1;

	public final static String TABLE_NAME = "cache_status";

	public final static String TABLE_ID = "_id";
	public final static String TABLE_RESULT_URL = "result_url";
	public final static String TABLE_UNIQUE = "cache_unique";
	public final static String TABLE_EXPIRE = "cached_until";

	public AccountDB(Context context) {
		this.context = context;

		CustomSQLiteOpenHelper helper = new CustomSQLiteOpenHelper(context);
		this.db = helper.getWritableDatabase();
	}

	private class CustomSQLiteOpenHelper extends SQLiteOpenHelper {
		public CustomSQLiteOpenHelper(Context context) {
			super(context, DB_NAME, null, DB_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			String newTableQueryString = "create table " + TABLE_NAME + " ("
					+ TABLE_ID + " integer primary key autoincrement not null,"
					+ TABLE_RESULT_URL + " text," + TABLE_UNIQUE + " text,"
					+ TABLE_EXPIRE + " integer" + ");";

			db.execSQL(newTableQueryString);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

		}
	}

	public ArrayList<EveCharacter> characters(APICredentials credentials) {
		// TODO Auto-generated method stub
		return null;
	}
}
