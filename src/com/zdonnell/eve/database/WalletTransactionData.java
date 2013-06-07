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

import com.beimin.eveapi.shared.wallet.transactions.ApiWalletTransaction;

public class WalletTransactionData extends DatabaseTable {

	public final static String TABLE = "wallet_transactions";

	public final static String COL_ID = "transaction_ref_id";
	public final static String COL_CHAR_ID = "char_id";
	public final static String COL_DATE = "transaction_date";
	public final static String COL_QUANTITY = "journal_charid";
	public final static String COL_TYPENAME = "journal_ref_type";
	public final static String COL_TYPEID = "journal_owner_name1";
	public final static String COL_PRICE = "journal_owner_id1";
	public final static String COL_CLIENTID = "journal_owner_name2";
	public final static String COL_CLIENTNAME = "journal_owner_id2";
	public final static String COL_STATION_ID = "journal_arg_name";
	public final static String COL_STATION_NAME = "journal_arg_id";
	public final static String COL_TRANSACTIONTYPE = "journal_amount";
	public final static String COL_TRANSACTIONFOR = "journal_balance";

	private SimpleDateFormat formatter;

	public WalletTransactionData(Context context) {
		super(new DatabaseOpenHelper(context) {
			@Override
			public void onCreate(SQLiteDatabase db) {
				String newTableQueryString = "create table " + WalletTransactionData.TABLE + " ("
					+ WalletTransactionData.COL_ID + " integer primary key not null," 
					+ WalletTransactionData.COL_DATE + " text," 
					+ WalletTransactionData.COL_CHAR_ID + " integer," 
					+ WalletTransactionData.COL_QUANTITY + " integer,"
					+ WalletTransactionData.COL_TYPENAME + " text,"
					+ WalletTransactionData.COL_TYPEID + " integer,"
					+ WalletTransactionData.COL_PRICE + " real," 
					+ WalletTransactionData.COL_CLIENTID + " integer,"
					+ WalletTransactionData.COL_CLIENTNAME + " text,"
					+ WalletTransactionData.COL_STATION_ID + " integer,"		
					+ WalletTransactionData.COL_STATION_NAME + " text," 
					+ WalletTransactionData.COL_TRANSACTIONTYPE + " text,"
					+ WalletTransactionData.COL_TRANSACTIONFOR + " text,"		
					+ "UNIQUE (" + WalletTransactionData.COL_ID + ") ON CONFLICT REPLACE);";

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
	 * Inserts the provided wallet
	 */
	public void insertJournalEntries(final int characterID, final Set<ApiWalletTransaction> transactions) {
		performTransaction(new Transaction<Void>() {
			@Override
			public Void perform(SQLiteDatabase db) {
				ContentValues insertValues = new ContentValues();

				for(ApiWalletTransaction transaction : transactions) {
					insertValues.put(COL_ID, transaction.getTransactionID());
					insertValues.put(COL_DATE, formatter.format(transaction.getTransactionDateTime()));
					insertValues.put(COL_CHAR_ID, characterID);
					insertValues.put(COL_QUANTITY, transaction.getQuantity());
					insertValues.put(COL_TYPENAME, transaction.getTypeName());
					insertValues.put(COL_TYPEID, transaction.getTypeID());
					insertValues.put(COL_PRICE, transaction.getPrice());
					insertValues.put(COL_CLIENTID, transaction.getClientID());
					insertValues.put(COL_CLIENTNAME, transaction.getClientName());
					insertValues.put(COL_STATION_ID, transaction.getStationID());
					insertValues.put(COL_STATION_NAME, transaction.getStationName());
					insertValues.put(COL_TRANSACTIONTYPE, transaction.getTransactionType());
					insertValues.put(COL_TRANSACTIONFOR, transaction.getTransactionFor());

					db.insertWithOnConflict(TABLE, null, insertValues, SQLiteDatabase.CONFLICT_REPLACE);
					insertValues.clear();
				}

				return null;
			}
		});
	}

	public Set<ApiWalletTransaction> getJournalEntries(final int characterID) {
		return performTransaction(new Transaction<Set<ApiWalletTransaction>>() {
			@Override
			public Set<ApiWalletTransaction> perform(SQLiteDatabase db) {
				Set<ApiWalletTransaction> entries = new HashSet<ApiWalletTransaction>();

				Cursor c = db.query(TABLE, null, COL_CHAR_ID + " = ?", new String[] { String.valueOf(characterID) }, null, null, null);

				int unique_index = c.getColumnIndex(COL_ID);
				int date_index = c.getColumnIndex(COL_DATE);
				int quantity_index = c.getColumnIndex(COL_QUANTITY);
				int typename_index = c.getColumnIndex(COL_TYPENAME);
				int type_id_index = c.getColumnIndex(COL_TYPEID);
				int price_index = c.getColumnIndex(COL_PRICE);
				int client_id_index = c.getColumnIndex(COL_CLIENTID);
				int client_name_index = c.getColumnIndex(COL_CLIENTNAME);
				int station_id_index = c.getColumnIndex(COL_STATION_ID);
				int station_name_index = c.getColumnIndex(COL_STATION_NAME);
				int transaction_type_index = c.getColumnIndex(COL_TRANSACTIONTYPE);
				int transaction_for_index = c.getColumnIndex(COL_TRANSACTIONFOR);

				while(c.moveToNext()) {
					ApiWalletTransaction transaction = new ApiWalletTransaction();

					transaction.setTransactionID(c.getLong(unique_index));

					try {
						transaction.setTransactionDateTime(formatter.parse(c.getString(date_index)));
					} catch(ParseException e) {
						/*
						 * Let the journal entry still be assembled even if we are
						 * unable to correctly set the date
						 */
						Log.w("Eden", "Wallet Transaction Table: Failed to parse transaction date");
					}

					transaction.setQuantity(c.getInt(quantity_index));
					transaction.setTypeName(c.getString(typename_index));
					transaction.setTypeID(c.getInt(type_id_index));
					transaction.setPrice(c.getDouble(price_index));
					transaction.setClientID(c.getLong(client_id_index));
					transaction.setClientName(c.getString(client_name_index));
					transaction.setStationID(c.getInt(station_id_index));
					transaction.setStationName(c.getString(station_name_index));
					transaction.setTransactionType(c.getString(transaction_type_index));
					transaction.setTransactionFor(c.getString(transaction_for_index));

					entries.add(transaction);
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
	public long mostRecentID(final int characterID) {
		return performTransaction(new Transaction<Long>() {
			@Override
			public Long perform(SQLiteDatabase db) {
				Cursor c = db.query(TABLE, new String[] { COL_ID }, COL_CHAR_ID + " = ?", new String[] { String.valueOf(characterID) }, null, null, COL_ID + " DESC", "1");

				if(c.moveToFirst())
					return c.getLong(c.getColumnIndex(COL_ID));
				else
					return Long.valueOf(0);
			}
		});
		
	}
}
