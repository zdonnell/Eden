package com.zdonnell.eden.staticdata;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.zdonnell.eden.R;

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
		} catch (SQLException e) {
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
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	public <T> void genericDataInsert(Class<T> clazz, final Set<?> dataSet) throws Exception {
		final Dao<T, ?> dao = getDao(clazz);
		dao.callBatchTasks(new Callable<Void>() {
			public Void call() throws Exception {
				for (Object data : dataSet)
					dao.createOrUpdate((T) data);
				return null;
			}
		});
	}

	public <T> List<T> genericDataQuery(Class<T> clazz, PreparedQuery<T> preppedQuery) throws Exception {
		final Dao<T, ?> dao = getDao(clazz);
		return dao.query(preppedQuery);
	}

	public <T> QueryBuilder<T, Integer> getQueryBuilder(Class<T> clazz) throws Exception {
		final Dao<T, Integer> dao = getDao(clazz);
		return dao.queryBuilder();
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
