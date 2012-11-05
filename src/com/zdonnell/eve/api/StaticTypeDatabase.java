package com.zdonnell.eve.api;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class StaticTypeDatabase {
	
	Context context;

	private SQLiteDatabase db;
	
	private static final String DB_NAME = "static_types";
    private static final String DB_PATH = "/data/data/com.zdonnell.eve/databases/";

	private final int DB_VERSION = 1;

	public StaticTypeDatabase(Context context) 
	{
		this.context = context;

		StaticTypeSQLHelper helper = new StaticTypeSQLHelper(context);
		this.db = helper.getWritableDatabase();
	}

	public StaticTypeInfo getTypeInformation()
	{
		return null;
	}
	
	public String getTypeName(int typeID) 
	{	
		String query = "SELECT typeName FROM invTypes WHERE typeID=?";
		Cursor cursor = db.rawQuery(query, new String[] { String.valueOf(typeID) });

		String retString = "";
		if (cursor.moveToFirst()) retString = cursor.getString(0);
		
		cursor.close();
		
		return retString;
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
			try 
			{
				Log.d("DATABASE COPY", "1");
				createDatabase();
			} 
			catch (IOException e) { e.printStackTrace(); }
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) 
		{

		}
		
		/**
	     * Creates a empty database on the system and rewrites it with your own database.
	     * */
	    public void createDatabase() throws IOException
	    { 
	    	SharedPreferences preferences = context.getSharedPreferences("settings", 0);
	    	boolean dbInitialized = preferences.getBoolean("importedStaticDB", false);
	    	
	    	if (!dbInitialized)
	    	{
				Log.d("DATABASE COPY", "3");

	        	try 
	        	{
	    			copyDataBase();
	    			preferences.edit().putBoolean("importedStaticDB", true).commit();
	    		}
	        	catch (IOException e) { throw new Error("Error copying database"); }
	    	}
	    }
	    
	    /**
	     * Check if the database already exist to avoid re-copying the file each time you open the application.
	     * @return true if it exists, false if it doesn't
	     */
	    private boolean checkDataBase(){
	 
	    	SQLiteDatabase checkDB = null;
	 
	    	try
	    	{
				Log.d("DATABASE COPY", "2");

	    		String myPath = DB_PATH + DB_NAME;
	    		checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
	    	}
	    	catch(SQLiteException e){ e.printStackTrace(); }
	 
	    	if (checkDB != null) checkDB.close();
	 
	    	return checkDB != null ? true : false;
	    }
	    
	    /**
	     * Copies your database from your local assets-folder to the just created empty database in the
	     * system folder, from where it can be accessed and handled.
	     * This is done by transfering bytestream.
	     * */
	    private void copyDataBase() throws IOException{
	 
			Log.d("DATABASE COPY", "4");

	    	InputStream myInput = context.getAssets().open("packaged_db/" + DB_NAME);
	 
	    	String outFileName = DB_PATH + DB_NAME;
	    	OutputStream myOutput = new FileOutputStream(outFileName);
	 
	    	byte[] buffer = new byte[1024];
	    	int length;
	    	
	    	while ((length = myInput.read(buffer)) > 0)
	    	{
	    		myOutput.write(buffer, 0, length);
	    	}
	 
	    	myOutput.flush();
	    	myOutput.close();
	    	myInput.close();
	    	
			Log.d("DATABASE COPY", "5");
	    }
	}
}
