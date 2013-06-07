package com.zdonnell.eve.database;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.beimin.eveapi.eve.character.CharacterInfoResponse;
import com.beimin.eveapi.shared.character.EveBloodline;
import com.beimin.eveapi.shared.character.EveRace;

public class CharacterInfoData extends DatabaseTable {

	public final static String TABLE = "character_info";

	public final static String COL_CHARACTER_ID = "_id";
	public final static String COL_RACE = "character_race";
	public final static String COL_BLOODLINE = "character_bloodline";
	public final static String COL_WALLET_BALLANCE = "character_wallbalance";
	public final static String COL_SKILLPOINTS = "character_skillpoints";
	public final static String COL_SHIP_NAME = "character_curshipname";
	public final static String COL_SHIP_TYPEID = "character_curshiptypeid";
	public final static String COL_CORP_DATE = "character_curcorpjoindate";
	public final static String COL_ALLIANCE_ID = "character_allianceid";
	public final static String COL_ALLIANCE = "character_alliance";
	public final static String COL_ALLIANCE_DATE = "character_alliancedate";
	public final static String COL_LAST_KNOWN_LOC = "character_lastknownlocation";
	public final static String COL_SEC_STATUS = "character_security";

	private SimpleDateFormat formatter;

	public CharacterInfoData(Context context) {
		super(new DatabaseOpenHelper(context) {
			@Override
			public void onCreate(SQLiteDatabase db) {
				String newTableQueryString = "create table " + TABLE + " ("
					+ COL_CHARACTER_ID + " integer primary key not null," 
					+ COL_RACE + " text," 
					+ COL_BLOODLINE + " text,"
					+ COL_WALLET_BALLANCE + " real,"
					+ COL_SKILLPOINTS + " integer,"
					+ COL_SHIP_NAME + " text,"
					+ COL_SHIP_TYPEID + " integer,"
					+ COL_CORP_DATE + " text,"
					+ COL_ALLIANCE_ID + " integer,"
					+ COL_ALLIANCE + " text,"		
					+ COL_ALLIANCE_DATE + " text,"				
					+ COL_LAST_KNOWN_LOC + " text,"				
					+ COL_SEC_STATUS + " real,"				
	
					+ "UNIQUE (" + COL_CHARACTER_ID + ") ON CONFLICT REPLACE);";

				db.execSQL(newTableQueryString);
			}

			@Override
			public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
				// TODO Auto-generated method stub
			}
		});

		formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	}

	public void setCharacterInfo(final CharacterInfoResponse characterInfo) {
		performTransaction(new Transaction<Void>() {
			@Override
			public Void perform(SQLiteDatabase db) {
				ContentValues insertValues = new ContentValues();

				insertValues.put(COL_CHARACTER_ID, characterInfo.getCharacterID());
				insertValues.put(COL_RACE, characterInfo.getRace().name());
				insertValues.put(COL_BLOODLINE, characterInfo.getBloodline().name());
				insertValues.put(COL_WALLET_BALLANCE, characterInfo.getAccountBalance());
				insertValues.put(COL_SKILLPOINTS, characterInfo.getSkillPoints());
				insertValues.put(COL_SHIP_NAME, characterInfo.getShipName());
				insertValues.put(COL_SHIP_TYPEID, characterInfo.getShipTypeID());
				insertValues.put(COL_CORP_DATE, formatter.format(characterInfo.getCorporationDate()));
				insertValues.put(COL_ALLIANCE_ID, characterInfo.getAllianceID());
				insertValues.put(COL_ALLIANCE, characterInfo.getAlliance());

				if(characterInfo.getAllianceDate() != null)
					insertValues.put(COL_ALLIANCE_DATE, formatter.format(characterInfo.getAllianceDate()));

				insertValues.put(COL_LAST_KNOWN_LOC, characterInfo.getLastKnownLocation());
				insertValues.put(COL_SEC_STATUS, characterInfo.getSecurityStatus());

				db.insertWithOnConflict(TABLE, null, insertValues, SQLiteDatabase.CONFLICT_REPLACE);

				return null;
			}
		});
	}

	/**
	 * 
	 * @param characterID
	 * @return
	 */
	public CharacterInfoResponse getCharacterInfo(final int characterID) {
		return performTransaction(new Transaction<CharacterInfoResponse>() {
			@Override
			public CharacterInfoResponse perform(SQLiteDatabase db) {
				CharacterInfoResponse characterInfo = new CharacterInfoResponse();

				Cursor c = db.query(TABLE, null, COL_CHARACTER_ID + " = ?", new String[] { String.valueOf(characterID) }, null, null, null);

				if(c.moveToFirst()) {
					characterInfo.setCharacterID(c.getLong(c.getColumnIndex(COL_CHARACTER_ID)));
					characterInfo.setRace(EveRace.valueOf(c.getString(c.getColumnIndex(COL_RACE))));
					characterInfo.setBloodline(EveBloodline.valueOf(c.getString(c.getColumnIndex(COL_BLOODLINE))));
					characterInfo.setAccountBalance(c.getDouble(c.getColumnIndex(COL_WALLET_BALLANCE)));
					characterInfo.setSkillPoints(c.getInt(c.getColumnIndex(COL_SKILLPOINTS)));
					characterInfo.setShipName(c.getString(c.getColumnIndex(COL_SHIP_NAME)));
					characterInfo.setShipTypeID(c.getInt(c.getColumnIndex(COL_SHIP_TYPEID)));

					try {
						characterInfo.setCorporationDate(formatter.parse(c.getString(c.getColumnIndex(COL_CORP_DATE))));
					} catch(ParseException e) {
						e.printStackTrace();
					}

					characterInfo.setAllianceID(c.getLong(c.getColumnIndex(COL_ALLIANCE_ID)));
					characterInfo.setAlliance(c.getString(c.getColumnIndex(COL_ALLIANCE)));

					try {
						characterInfo.setAllianceDate(formatter.parse(c.getString(c.getColumnIndex(COL_ALLIANCE_DATE))));
					} catch(ParseException e) {
						e.printStackTrace();
					}

					characterInfo.setLastKnownLocation(c.getString(c.getColumnIndex(COL_LAST_KNOWN_LOC)));
					characterInfo.setSecurityStatus(c.getDouble(c.getColumnIndex(COL_SEC_STATUS)));
				}

				c.close();

				return characterInfo;
			}
		});
	}
}
