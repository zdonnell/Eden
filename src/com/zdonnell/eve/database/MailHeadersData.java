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

import com.beimin.eveapi.character.mail.messages.ApiMailMessage;

public class MailHeadersData extends DatabaseTable 
{
	public final static String TABLE = "mail_headers";
	public final static String COL_UNIQUE_ID = "_id";
	public final static String COL_CHAR_ID = "char_id";
	public final static String COL_SENDER_ID = "sender_id";
	public final static String COL_DATE = "date";
	public final static String COL_TITLE = "attr_implant_bonus";
	public final static String COL_CORP_OR_ALLIANCE_RECIPIENT = "corp_alliance";
	public final static String COL_CHAR_RECIPIENT = "corp_alliance";
	public final static String COL_LIST_RECIPIENT = "corp_alliance";
	public final static String COL_READ = "read";
	
	private SimpleDateFormat formatter;
	
	public MailHeadersData(Context context) 
	{
		super(new DatabaseTable.DatabaseOpenHelper(context) 
		{
			@Override
			public void onCreate(SQLiteDatabase db) 
			{
				String newTableQueryString = "create table " + TABLE + " ("
					
					+ COL_UNIQUE_ID + " integer primary key not null," 
					+ COL_CHAR_ID + " integer," 
					+ COL_SENDER_ID + " integer," 
					+ COL_DATE + " text,"
					+ COL_TITLE + " text,"
					+ COL_CORP_OR_ALLIANCE_RECIPIENT + " integer,"
					+ COL_CHAR_RECIPIENT + " text,"			
					+ COL_LIST_RECIPIENT + " text,"			
					+ COL_READ + " integer,"			

					+ "UNIQUE (" + COL_UNIQUE_ID + ") ON CONFLICT REPLACE);";

				db.execSQL(newTableQueryString);
			}

			@Override
			public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) 
			{
				// TODO
			}
		});
		
		formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
	}
	
	/**
	 * Gets all mail headers for the specified character
	 * 
	 * @param characterID
	 * @return
	 */
	public Set<ApiMailMessage> getMailHeaders(final int characterID)
	{
		return performTransaction(new DatabaseTable.Transaction<Set<ApiMailMessage>>() 
		{
			@Override
			public Set<ApiMailMessage> perform(SQLiteDatabase db) 
			{
				Cursor c = db.query(TABLE, null, COL_CHAR_ID + " = ?", new String[] { String.valueOf(characterID) }, null, null, null);
				
				Set<ApiMailMessage> mailHeaders = new HashSet<ApiMailMessage>();
				
				int messageIDIndex = c.getColumnIndex(COL_UNIQUE_ID);
				int senderIDIndex = c.getColumnIndex(COL_SENDER_ID);
				int dateIndex = c.getColumnIndex(COL_DATE);
				int titleIndex = c.getColumnIndex(COL_TITLE);
				int corpAllianceIndex = c.getColumnIndex(COL_CORP_OR_ALLIANCE_RECIPIENT);
				int charRecipIndex = c.getColumnIndex(COL_CHAR_RECIPIENT);
				int listRecipIndex = c.getColumnIndex(COL_LIST_RECIPIENT);
				int readIndex = c.getColumnIndex(COL_READ);
				
				while (c.moveToNext())
				{
					ApiMailMessage messageHeader = new ApiMailMessage();
					
					messageHeader.setMessageID(c.getLong(messageIDIndex));
					messageHeader.setRead(c.getInt(readIndex) == 1);
					messageHeader.setSenderID(c.getLong(senderIDIndex));
					
					try { messageHeader.setSentDate(formatter.parse(c.getString(dateIndex))); } 
					catch (ParseException e) 
					{
						Log.w("Eden", "Get Mail Headers: Unable to parse mail header send date");
					}
					
					messageHeader.setTitle(c.getString(titleIndex));
					messageHeader.setToCharacterIDs(c.getString(charRecipIndex));
					messageHeader.setToCorpOrAllianceID(c.getLong(corpAllianceIndex));
					messageHeader.setToListIDs(c.getString(listRecipIndex));
					
					mailHeaders.add(messageHeader);
				}
				
				return mailHeaders;
			}
		});
	}
	
	/**
	 * Stores the provided mail headers
	 * 
	 * @param characterID
	 * @param mailHeaders
	 */
	public void setMailHeaders(final int characterID, final Set<ApiMailMessage> mailHeaders)
	{
		performTransaction(new DatabaseTable.Transaction<Void>() 
		{
			@Override
			public Void perform(SQLiteDatabase db) 
			{
				ContentValues insertValues = new ContentValues();
				
				for(ApiMailMessage mailHeader : mailHeaders) 
				{
					insertValues.put(COL_UNIQUE_ID, mailHeader.getMessageID());
					insertValues.put(COL_CHAR_ID, characterID);
					insertValues.put(COL_SENDER_ID, mailHeader.getSenderID());
					insertValues.put(COL_DATE, formatter.format(mailHeader.getSentDate()));
					insertValues.put(COL_TITLE, mailHeader.getTitle());
					insertValues.put(COL_CORP_OR_ALLIANCE_RECIPIENT, mailHeader.getToCorpOrAllianceID());					
					insertValues.put(COL_CHAR_RECIPIENT, mailHeader.getToCharacterIDs());
					insertValues.put(COL_LIST_RECIPIENT, mailHeader.getToListIDs());
					insertValues.put(COL_READ, mailHeader.isRead() ? 1 : 0);
					
					db.insertWithOnConflict(TABLE, null, insertValues, SQLiteDatabase.CONFLICT_REPLACE);
					insertValues.clear();
				}
				
				return null;
			}
		});
	}
}
