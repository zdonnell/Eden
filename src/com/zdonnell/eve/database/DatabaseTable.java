package com.zdonnell.eve.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Base class for accessing the cache database table
 */
public abstract class DatabaseTable {
	public static final String DB_NAME = "api_data_db";
	public static final int DB_VERSION = 1;

	private SQLiteDatabase db;

	protected abstract static class DatabaseOpenHelper extends SQLiteOpenHelper {
		public DatabaseOpenHelper(Context context) {
			super(context, DB_NAME, null, DB_VERSION);
		}
	}

	/**
	 * Perform actions on the database
	 */
	protected interface Transaction<T> {
		T perform(SQLiteDatabase db);
	}

	public DatabaseTable(DatabaseOpenHelper helper) {
		db = helper.getWritableDatabase();
	}

	/**
	 * Safely performs a database transaction
	 */
	protected <T> T performTransaction(Transaction<T> transaction) {
		T result = null;

		synchronized(db) {
			db.acquireReference();
			db.beginTransaction();
			try {
				result = transaction.perform(db);
				db.setTransactionSuccessful();
			} finally {
				db.endTransaction();
			}
			db.releaseReference();
		}

		return result;
	}
}
