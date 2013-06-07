package com.zdonnell.eve.database;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.TimeZone;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.beimin.eveapi.character.skill.queue.ApiSkillQueueItem;

public class SkillQueueData extends DatabaseTable {
	public final static String TABLE = "skill_queue";

	public final static String COL_UNIQUE_ID = "_id";
	public final static String COL_CHAR_ID = "skill_charid";
	public final static String COL_TYPEID = "skill_typeid";
	public final static String COL_START_SP = "skill_startsp";
	public final static String COL_END_SP = "skill_endsp";
	public final static String COL_LEVEL = "skill_level";
	public final static String COL_START_TIME = "skill_start_time";
	public final static String COL_END_TIME = "skill_end_time";
	public final static String COL_POSITION = "skill_queue_position";

	private SimpleDateFormat formatter;

	public SkillQueueData(Context context) {
		super(new DatabaseOpenHelper(context) {
			@Override
			public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
				String newTableQueryString = "create table " + TABLE + " ("
					+ COL_UNIQUE_ID + " integer primary key not null," 
					+ COL_CHAR_ID + " integer," 
					+ COL_TYPEID + " integer,"
					+ COL_START_SP + " integer,"
					+ COL_END_SP + " integer,"
					+ COL_LEVEL + " integer,"
					+ COL_START_TIME + " text,"
					+ COL_END_TIME + " text,"
					+ COL_POSITION + " integer,"				
					+ "UNIQUE (" + COL_CHAR_ID + ", " + COL_POSITION + ") ON CONFLICT REPLACE);";

				db.execSQL(newTableQueryString);
			}
			
			@Override
			public void onCreate(SQLiteDatabase db) {
				// TODO
			}
		});
		
		formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
	}

	/**
	 * Obtains skills in the queue for the provided characterID
	 */
	public Set<ApiSkillQueueItem> getQueue(final int characterID) {
		return performTransaction(new Transaction<Set<ApiSkillQueueItem>>() {
			@Override
			public Set<ApiSkillQueueItem> perform(SQLiteDatabase db) {
				Set<ApiSkillQueueItem> queue = new HashSet<ApiSkillQueueItem>();

				Cursor c = db.query(TABLE, null, COL_CHAR_ID + " = ?", new String[] { String.valueOf(characterID) }, null, null, COL_POSITION + " ASC");
				while(c.moveToNext()) {
					ApiSkillQueueItem queuedSkill = new ApiSkillQueueItem();

					queuedSkill.setQueuePosition(c.getInt(c.getColumnIndex(COL_POSITION)));
					queuedSkill.setTypeID(c.getInt(c.getColumnIndex(COL_TYPEID)));
					queuedSkill.setLevel(c.getInt(c.getColumnIndex(COL_LEVEL)));
					queuedSkill.setStartSP(c.getInt(c.getColumnIndex(COL_START_SP)));
					queuedSkill.setEndSP(c.getInt(c.getColumnIndex(COL_END_SP)));

					try {
						Date startTime = formatter.parse(c.getString(c.getColumnIndex(COL_START_TIME)));
						Date endTime = formatter.parse(c.getString(c.getColumnIndex(COL_END_TIME)));

						queuedSkill.setStartTime(startTime);
						queuedSkill.setEndTime(endTime);
					} catch(ParseException e) {
						// TODO handle exception
						e.printStackTrace();
					}

					queue.add(queuedSkill);
				}
				
				c.close();

				return queue;
			}
		});
	}

	/**
	 * Inserts the provided skills and links with the provided characterID. If
	 * the database already contains an entry for a characterID + queue position
	 * it will be replaced
	 */
	public void setQueueSkills(final int characterID, final Set<ApiSkillQueueItem> queue) {
		performTransaction(new Transaction<Void>() {
			@Override
			public Void perform(SQLiteDatabase db) {
				ContentValues insertValues = new ContentValues();
				for(ApiSkillQueueItem queuedSkill : queue) {
					insertValues.put(COL_CHAR_ID, characterID);
					insertValues.put(COL_TYPEID, queuedSkill.getTypeID());
					insertValues.put(COL_POSITION, queuedSkill.getQueuePosition());
					insertValues.put(COL_LEVEL, queuedSkill.getLevel());
					insertValues.put(COL_START_SP, queuedSkill.getStartSP());
					insertValues.put(COL_END_SP, queuedSkill.getEndSP());
					insertValues.put(COL_START_TIME, formatter.format(queuedSkill.getStartTime()));
					insertValues.put(COL_END_TIME, formatter.format(queuedSkill.getEndTime()));

					db.insert(TABLE, null, insertValues);
					insertValues.clear();
				}

				return null;
			}
		});
	}
}
