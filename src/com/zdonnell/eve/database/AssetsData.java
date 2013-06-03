package com.zdonnell.eve.database;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.beimin.eveapi.shared.assetlist.EveAsset;

public class AssetsData {

	public final static String TABLE = "assets";
	
	public final static String COL_ID = "assets_item_id";
	public final static String COL_LOC_ID = "assets_location_id";
	public final static String COL_CHAR_ID = "assets_char_id";
	public final static String COL_PARENT_ID = "assets_parent_id";
	public final static String COL_TYPE_ID = "assets_type_id";
	public final static String COL_QUANTITY = "assets_quantity";
	public final static String COL_FLAG = "assets_flag";
	public final static String COL_SINGLETON = "assets_singleton";
	public final static String COL_RAW_QUANTITY = "assets_raw_quantity";
	
	// the Activity or Application that is creating an object from this class.
	Context context;

	// a reference to the database used by this application/object
	private SQLiteDatabase db;
		
	public AssetsData(Context context) 
	{
		this.context = context;

		CustomSQLiteOpenHelper helper = new CustomSQLiteOpenHelper(context);
		this.db = helper.getWritableDatabase();
	}
	
	/**	 
	 * Stores the provided assets in the database
	 *  
	 * @param characterID
	 * @param transactions
	 */
	public void setAssets(int characterID, Set<EveAsset<EveAsset<?>>> assets)
	{
		// Clearing out all the assets from the database is easier than checking row by row to see
		// which assets still exist in the newest API response
		db.delete(TABLE, COL_CHAR_ID + " = ?", new String[] { String.valueOf(characterID) });
		
		db.beginTransaction();
		
		for (EveAsset<EveAsset<?>> assetItem : assets) insertAsset(assetItem, characterID, null);
		
		db.setTransactionSuccessful();
		db.endTransaction();
	}
	
	/**
	 * Inserts the provided asset and all of it's children into the database
	 * 
	 * @param assetItem
	 * @param insertValues
	 * @param parentItemID
	 */
	private void insertAsset(EveAsset<?> assetItem, int characterID, Long parentItemID)
	{
		ContentValues insertValues = new ContentValues();
		
		insertValues.put(COL_ID, assetItem.getItemID());
		insertValues.put(COL_LOC_ID, assetItem.getLocationID());
		insertValues.put(COL_CHAR_ID, characterID);
		insertValues.put(COL_PARENT_ID, parentItemID);
		insertValues.put(COL_TYPE_ID, assetItem.getTypeID());
		insertValues.put(COL_QUANTITY, assetItem.getQuantity());
		insertValues.put(COL_FLAG, assetItem.getFlag());
		insertValues.put(COL_SINGLETON, assetItem.getSingleton() ? 1 : 0);
		insertValues.put(COL_RAW_QUANTITY, assetItem.getRawQuantity());
		
		db.insert(TABLE, null, insertValues);
		insertValues.clear();
		
		@SuppressWarnings("unchecked")
		Collection<EveAsset<?>> containedAssets = (Collection<EveAsset<?>>) assetItem.getAssets();
		for (EveAsset<?> containedAsset : containedAssets) insertAsset(containedAsset, characterID, assetItem.getItemID());
	}
	
	/**

	 */
	public Set<EveAsset<EveAsset<?>>> getAssets(int characterID)
	{
		Set<EveAsset<EveAsset<?>>> assets = new LinkedHashSet<EveAsset<EveAsset<?>>>();
		
		Cursor c = db.query(TABLE, null, COL_CHAR_ID + " = ? AND " + COL_PARENT_ID + " IS NULL", new String[] { String.valueOf(characterID) }, null, null, null);		
		
		for (EveAsset<EveAsset<?>> childAsset : buildAssets(c)) assets.add(childAsset);
		
		c.close();
		
		return assets;
	}
	
	private Set<EveAsset<EveAsset<?>>> buildAssets(Cursor c)
	{
		Set<EveAsset<EveAsset<?>>> assets = new LinkedHashSet<EveAsset<EveAsset<?>>>();
		
		int id_index = c.getColumnIndex(COL_ID);
		int loc_id_index = c.getColumnIndex(COL_LOC_ID);
		int type_id_index = c.getColumnIndex(COL_TYPE_ID);
		int quantity_index = c.getColumnIndex(COL_QUANTITY);
		int flag_index = c.getColumnIndex(COL_FLAG);
		int singleton_index = c.getColumnIndex(COL_SINGLETON);
		int raw_quantity_index = c.getColumnIndex(COL_RAW_QUANTITY);
		
		while (c.moveToNext())
		{
			EveAsset<EveAsset<?>> asset = new EveAsset<EveAsset<?>>();
			
			asset.setItemID(c.getLong(id_index));
			asset.setLocationID(c.getLong(loc_id_index));
			asset.setTypeID(c.getInt(type_id_index));
			asset.setQuantity(c.getInt(quantity_index));
			asset.setFlag(c.getInt(flag_index));
			asset.setSingleton(c.getInt(singleton_index) == 1);
			asset.setRawQuantity(c.getInt(raw_quantity_index));
			
			Cursor c2 = db.query(TABLE, null, COL_PARENT_ID + " = ?", new String[] { String.valueOf(asset.getItemID()) }, null, null, null);
			for (EveAsset<EveAsset<?>> childAsset : buildAssets(c2)) asset.addAsset(childAsset);
			c2.close();
			
			assets.add(asset);
		}
		
		return assets;
	}
	
	private class CustomSQLiteOpenHelper extends SQLiteOpenHelper {
		
		public CustomSQLiteOpenHelper(Context context) 
		{
			super(context, Database.DB_NAME, null, Database.DB_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) 
		{
			Database.onCreate(db);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) 
		{
			Database.onUpdate(db, oldVersion, newVersion);
		}
	}
}
