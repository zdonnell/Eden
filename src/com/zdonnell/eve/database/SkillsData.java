package com.zdonnell.eve.database;

import java.util.HashSet;
import java.util.Set;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.beimin.eveapi.character.sheet.ApiSkill;

public class SkillsData extends DatabaseTable {
	public final static String TABLE = "skills";
	public final static String COL_UNIQUE_ID = "_id";
	public final static String COL_CHAR_ID = "skill_charid";
	public final static String COL_TYPEID = "skill_typeid";
	public final static String COL_SKILLPOINTS = "skill_skillpoints";
	public final static String COL_LEVEL = "skill_level";
	public final static String COL_UNPUBLISHED = "skill_published";

	public SkillsData(Context context) {
		super(new DatabaseOpenHelper(context) {
			@Override
			public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
				String newTableQueryString = "create table " + TABLE + " ("
					+ COL_UNIQUE_ID + " integer primary key not null," 
					+ COL_CHAR_ID + " integer," 
					+ COL_TYPEID + " integer,"
					+ COL_SKILLPOINTS + " integer,"
					+ COL_LEVEL + " integer,"
					+ COL_UNPUBLISHED + " integer,"
					+ "UNIQUE (" + COL_CHAR_ID + ", " + COL_TYPEID + ") ON CONFLICT REPLACE);";

				db.execSQL(newTableQueryString);
			}

			@Override
			public void onCreate(SQLiteDatabase db) {
				// TODO
			}
		});
	}

	/**
	 * Obtains set of skills for the provided characterID
	 */
	public Set<ApiSkill> getSkills(final int characterID) {
		return performTransaction(new Transaction<Set<ApiSkill>>() {
			@Override
			public Set<ApiSkill> perform(SQLiteDatabase db) {
				Set<ApiSkill> skillSet = new HashSet<ApiSkill>();
				String[] columnsToReturn = new String[] { COL_TYPEID, COL_SKILLPOINTS, COL_LEVEL, COL_UNPUBLISHED };

				Cursor c = db.query(TABLE, columnsToReturn, COL_CHAR_ID + " = ?", new String[] { String.valueOf(characterID) }, null, null, null);
				while(c.moveToNext()) {
					ApiSkill skill = new ApiSkill();
					skill.setLevel(c.getInt(c.getColumnIndex(COL_LEVEL)));
					skill.setSkillpoints(c.getInt(c.getColumnIndex(COL_SKILLPOINTS)));
					skill.setTypeID(c.getInt(c.getColumnIndex(COL_TYPEID)));
					skill.setUnpublished(c.getInt(c.getColumnIndex(COL_UNPUBLISHED)) == 1 ? true : false);

					skillSet.add(skill);
				}
				c.close();

				return skillSet;
			}
		});
	}

	/**
	 * Inserts the provided skills and links with the provided characterID. If
	 * the database already contains an entry for a characterID + skill typeID
	 * it will be replaced
	 */
	public void storeSkills(final int characterID, final Set<ApiSkill> skillSet) {
		performTransaction(new Transaction<Void>() {
			@Override
			public Void perform(SQLiteDatabase db) {
				ContentValues insertValues = new ContentValues();
				for(ApiSkill skill : skillSet) {
					insertValues.put(COL_CHAR_ID, characterID);
					insertValues.put(COL_TYPEID, skill.getTypeID());
					insertValues.put(COL_SKILLPOINTS, skill.getSkillpoints());
					insertValues.put(COL_LEVEL, skill.getLevel());
					insertValues.put(COL_UNPUBLISHED, skill.isUnpublished() ? 1 : 0);

					db.insert(TABLE, null, insertValues);
					insertValues.clear();
				}

				return null;
			}
		});
	}
}
