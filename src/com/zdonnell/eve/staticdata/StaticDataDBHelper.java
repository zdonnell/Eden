package com.zdonnell.eve.staticdata;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.zdonnell.eve.R;

public class StaticDataDBHelper extends OrmLiteSqliteOpenHelper {

	private static final String DATABASE_NAME = "staticdata.db";

	private static final int DATABASE_VERSION = 1;

	/**
	 * cachedDaos, mapped by their data class
	 */
	private Map<Class<?>, Dao<?, Integer>> daoCache = new HashMap<Class<?>, Dao<?, Integer>>();

	public StaticDataDBHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION, R.raw.ormlite_config);
	}

	/**
	 * This is called when the database is first created. Usually you should
	 * call createTable statements here to create the tables that will store
	 * your data.
	 */
	@Override
	public void onCreate(SQLiteDatabase db, ConnectionSource connectionSource) {
		try {
			TableUtils.createTable(connectionSource, TypeInfo.class);
			TableUtils.createTable(connectionSource, StationInfo.class);
		} catch(SQLException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * This is called when your application is upgraded and it has a higher
	 * version number. This allows you to adjust the various data to match the
	 * new version number.
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource, int oldVersion, int newVersion) {
		try {
			TableUtils.dropTable(connectionSource, TypeInfo.class, true);
			TableUtils.dropTable(connectionSource, StationInfo.class, true);
			// after we drop the old databases, we create the new ones
			onCreate(db, connectionSource);
		} catch(SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public void basicStaticDataInsert(final Class<?> clazz, final Set<?> dataObjects) throws Exception {
		getDao(clazz).callBatchTasks(new Callable<Void>() {
			public Void call() throws Exception {

				if(clazz == TypeInfo.class) {
					Dao<TypeInfo, Integer> dao = getDao(TypeInfo.class);
					for(Object o : dataObjects)
						dao.createOrUpdate((TypeInfo) o);
				} else if(clazz == StationInfo.class) {
					Dao<StationInfo, Integer> dao = getDao(StationInfo.class);
					for(Object o : dataObjects)
						dao.createOrUpdate((StationInfo) o);
				}

				return null;
			}
		});
	}

	/**
	 * Close the database connections and clear any cached DAOs.
	 */
	@Override
	public void close() {
		super.close();
		daoCache.clear();
	}
}
