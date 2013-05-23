package com.zdonnell.eve.database;

import java.util.Set;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.beimin.eveapi.character.sheet.ApiSkill;
import com.beimin.eveapi.shared.wallet.journal.ApiJournalEntry;

public class WalletJournalData {

	public final static String TABLE = "wallet_journal_entries";
	
	public final static String COL_UNIQUE_ID = "ref_id";
	public final static String COL_CHAR_ID = "journal_charid";
	public final static String COL_REF_TYPE = "journal_ref_type";
	public final static String COL_OWNER_NAME1 = "journal_owner_name1";
	public final static String COL_OWNER_ID1 = "journal_owner_id1";
	public final static String COL_OWNER_NAME2 = "journal_owner_name2";
	public final static String COL_OWNER_ID2 = "journal_owner_id2";
	public final static String COL_ARG_NAME = "journal_arg_name";
	public final static String COL_ARG_ID = "journal_arg_id";
	public final static String COL_AMOUNT = "journal_amount";
	public final static String COL_BALANCE = "journal_balance";
	public final static String COL_REASON = "journal_reason";
	public final static String COL_TAX_RECEIVER_ID = "journal_tax_receiever_id";
	public final static String COL_TAX_AMOUNT = "journal_tax_amount";
	
	// the Activity or Application that is creating an object from this class.
	Context context;

	// a reference to the database used by this application/object
	private SQLiteDatabase db;
	
	public WalletJournalData(Context context) 
	{
		this.context = context;

		CustomSQLiteOpenHelper helper = new CustomSQLiteOpenHelper(context);
		this.db = helper.getWritableDatabase();
	}
	
	/**
	 * Inserts the provided journal entries.  Inserting of already existing entries
	 * should be avoided, use {@link #mostRecentId} to get the ref ID of the most recent
	 * journal entry in the database.
	 * 
	 * @param entries
	 */
	public void insertJournalEntries(Set<ApiJournalEntry> entries)
	{
		db.beginTransaction();
		
		ContentValues insertValues = new ContentValues();
		
		for (ApiSkill skill : skillSet)
		{
			insertValues.put(COL_CHAR_ID, characterID);
			insertValues.put(COL_TYPEID, skill.getTypeID());
			insertValues.put(COL_SKILLPOINTS, skill.getSkillpoints());
			insertValues.put(COL_LEVEL, skill.getLevel());
			insertValues.put(COL_UNPUBLISHED, skill.isUnpublished() ? 1 : 0);
			
			db.insert(TABLE, null, insertValues);
			insertValues.clear();
		}
		
		db.setTransactionSuccessful();
		db.endTransaction();
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
