package com.zdonnell.eve.database;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.beimin.eveapi.shared.wallet.journal.ApiJournalEntry;

public class WalletJournalData extends DatabaseTable {

	public final static String TABLE = "wallet_journal_entries";

	public final static String COL_REF_ID = "ref_id";
	public final static String COL_DATE = "journal_date";
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

	private SimpleDateFormat formatter;

	public WalletJournalData(Context context) {
		super(new DatabaseOpenHelper(context) {
			@Override
			public void onCreate(SQLiteDatabase db) {
				String newTableQueryString = "create table " + TABLE + " ("
					+ COL_REF_ID + " integer primary key not null," 
					+ COL_DATE + " text," 
					+ COL_CHAR_ID + " integer," 
					+ COL_REF_TYPE + " text,"
					+ COL_OWNER_NAME1 + " text,"
					+ COL_OWNER_ID1 + " integer,"
					+ COL_OWNER_NAME2 + " text," 
					+ COL_OWNER_ID2 + " integer,"
					+ COL_ARG_NAME + " text,"
					+ COL_ARG_ID + " integer,"		
					+ COL_AMOUNT + " real," 
					+ COL_BALANCE + " real,"
					+ COL_REASON + " text,"
					+ COL_TAX_RECEIVER_ID + " integer,"	
					+ COL_TAX_AMOUNT + " real,"		
					+ "UNIQUE (" + COL_REF_ID + ") ON CONFLICT REPLACE);";

				db.execSQL(newTableQueryString);
			}

			@Override
			public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
				// TODO
			}
		});

		formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
	}

	/**
	 * Inserts the provided journal entries. Inserting of already existing
	 * entries should be avoided, use {@link #mostRecentId} to get the ref ID of
	 * the most recent journal entry in the database.
	 * 
	 * @param entries
	 */
	public void insertJournalEntries(final int characterID, final Set<ApiJournalEntry> entries) {
		performTransaction(new Transaction<Void>() {
			@Override
			public Void perform(SQLiteDatabase db) {
				ContentValues insertValues = new ContentValues();
				for(ApiJournalEntry entry : entries) {
					insertValues.put(COL_REF_ID, entry.getRefID());
					insertValues.put(COL_DATE, formatter.format(entry.getDate()));
					insertValues.put(COL_CHAR_ID, characterID);
					insertValues.put(COL_REF_TYPE, entry.getRefType().getId());
					insertValues.put(COL_OWNER_NAME1, entry.getOwnerName1());
					insertValues.put(COL_OWNER_ID1, entry.getOwnerID1());
					insertValues.put(COL_OWNER_NAME2, entry.getOwnerName2());
					insertValues.put(COL_OWNER_ID2, entry.getOwnerID2());
					insertValues.put(COL_ARG_NAME, entry.getArgName1());
					insertValues.put(COL_ARG_ID, entry.getArgID1());
					insertValues.put(COL_AMOUNT, entry.getAmount());
					insertValues.put(COL_BALANCE, entry.getBalance());
					insertValues.put(COL_REASON, entry.getReason());
					insertValues.put(COL_TAX_RECEIVER_ID, entry.getTaxReceiverID());
					insertValues.put(COL_TAX_AMOUNT, entry.getTaxAmount());

					db.insertWithOnConflict(TABLE, null, insertValues, SQLiteDatabase.CONFLICT_REPLACE);
					insertValues.clear();
				}
				
				return null;
			}
		});
	}

	public Set<ApiJournalEntry> getJournalEntries(final int characterID) {
		return performTransaction(new Transaction<Set<ApiJournalEntry>>() {
			@Override
			public Set<ApiJournalEntry> perform(SQLiteDatabase db) {
				Set<ApiJournalEntry> entries = new HashSet<ApiJournalEntry>();

				Cursor c = db.query(TABLE, null, COL_CHAR_ID + " = ?", new String[] { String.valueOf(characterID) }, null, null, null);

				int unique_index = c.getColumnIndex(COL_REF_ID);
				int date_index = c.getColumnIndex(COL_DATE);
				int ref_type_index = c.getColumnIndex(COL_REF_TYPE);
				int owner_name_index = c.getColumnIndex(COL_OWNER_NAME1);
				int owner_id_index = c.getColumnIndex(COL_OWNER_ID1);
				int owner_name2_index = c.getColumnIndex(COL_OWNER_NAME2);
				int owner_id2_index = c.getColumnIndex(COL_OWNER_ID2);
				int arg_name_index = c.getColumnIndex(COL_ARG_NAME);
				int arg_id_index = c.getColumnIndex(COL_ARG_ID);
				int amount_index = c.getColumnIndex(COL_AMOUNT);
				int balance_index = c.getColumnIndex(COL_BALANCE);
				int reason_index = c.getColumnIndex(COL_REASON);
				int tax_receiver_id_index = c.getColumnIndex(COL_TAX_RECEIVER_ID);
				int tax_amount_index = c.getColumnIndex(COL_TAX_AMOUNT);

				while(c.moveToNext()) {
					ApiJournalEntry entry = new ApiJournalEntry();

					entry.setRefID(c.getLong(unique_index));

					try {
						entry.setDate(formatter.parse(c.getString(date_index)));
					} catch(ParseException e) {
						/*
						 * Let the journal entry still be assembled even if we are
						 * unable to correctly set the date
						 */
						Log.w("Eden", "Wallet Journal Entry Table: Failed to parse journal entry date");
					}

					entry.setRefTypeID(c.getInt(ref_type_index));
					entry.setOwnerName1(c.getString(owner_name_index));
					entry.setOwnerID1(c.getLong(owner_id_index));
					entry.setOwnerName2(c.getString(owner_name2_index));
					entry.setOwnerID2(c.getLong(owner_id2_index));
					entry.setArgName1(c.getString(arg_name_index));
					entry.setArgID1(c.getLong(arg_id_index));
					entry.setAmount(c.getDouble(amount_index));
					entry.setBalance(c.getDouble(balance_index));
					entry.setReason(c.getString(reason_index));
					entry.setTaxReceiverID(c.getLong(tax_receiver_id_index));
					entry.setTaxAmount(c.getDouble(tax_amount_index));

					entries.add(entry);
				}

				return entries;
			}
		});
	}

	/**
	 * Obtains the most recent refID for the character specified.
	 * 
	 * @param characterID
	 * @return the newest refID or 0 if no entries were found.
	 */
	public long mostRecentRefID(final int characterID) {
		return performTransaction(new Transaction<Long>() {
			@Override
			public Long perform(SQLiteDatabase db) {
				Cursor c = db.query(TABLE, new String[] { COL_REF_ID }, COL_CHAR_ID + " = ?", new String[] { String.valueOf(characterID) }, null, null, COL_REF_ID + " DESC", "1");

				if(c.moveToFirst())
					return c.getLong(c.getColumnIndex(COL_REF_ID));
				else
					return Long.valueOf(0);
			}
		});
	}
}
