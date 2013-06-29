package com.zdonnell.eden.staticdata;

import java.util.List;

import android.content.Context;
import android.os.AsyncTask;
import android.util.SparseArray;

import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.zdonnell.androideveapi.link.ApiCallback;

public class StaticData {

	private StaticDataDBHelper staticDataHelper;

	/**
	 * This enum reflects the current tables being "monitored" by the local
	 * static data database. To download (and track updates for) an additional
	 * table, add an entry to list.
	 * 
	 * @author zach
	 * 
	 */
	public enum Table {
		INV_TYPES("invTypes", TypeInfo.class), STA_STATIONS("staStations", StationInfo.class);

		private String name;
		private Class<?> clazz;

		/**
		 * 
		 * @param name
		 *            the String name of the class. This string name will be
		 *            used in the {@link CheckServerDataTask} to query the
		 *            correct server table
		 * @param clazz
		 *            The class that is represented by this entry for access
		 *            purposes. for example: {@link TypeInfo}
		 */
		private Table(String name, Class<?> clazz) {
			this.name = name;
			this.clazz = clazz;
		}

		public Class<?> clazz() {
			return clazz;
		}

		@Override
		public String toString() {
			return name;
		}
	}

	public StaticData(Context context) {
		staticDataHelper = new StaticDataDBHelper(context);
	}

	/**
	 * Obtains static data (if it exists) and provides it to the specified
	 * callback as a SparseArray indexed by the provided classes uniqueId
	 * method.
	 * 
	 * @param callback
	 *            the callback to provide the acquired data to.
	 * @param clazz
	 *            the class of the individual items to be obtained
	 * @param uniqueIDs
	 *            the set of unique ids to obtain info for.
	 */
	public <D extends IStaticDataType> void getStaticData(ApiCallback<SparseArray<D>> callback, Class<D> clazz, Integer... uniqueIDs) {
		new StaticDataRequest<D>(callback, clazz).execute(uniqueIDs);
	}

	/**
	 * This task takes the provided Class type and callback to check the
	 * corresponding ORMLite table for matching entries.
	 * 
	 * @author zach
	 * 
	 * @param <T>
	 *            The {@link IStaticDataType} implementation for the requested
	 *            data
	 */
	private class StaticDataRequest<T extends IStaticDataType> extends AsyncTask<Integer, Integer, SparseArray<T>> {
		/**
		 * Reference to the callback to provide the data to
		 */
		private ApiCallback<SparseArray<T>> onCompleteRequestCallback;

		/**
		 * Reference to the data class to return
		 */
		Class<T> dataClazz;

		/**
		 * @param callback
		 *            callback to provide the obtained info to.
		 * @param clazz
		 *            the Class type of the response elements
		 */
		public StaticDataRequest(ApiCallback<SparseArray<T>> callback, Class<T> clazz) {
			onCompleteRequestCallback = callback;
			this.dataClazz = clazz;
		}

		@Override
		protected SparseArray<T> doInBackground(Integer... uniqueIDs) {
			SparseArray<T> typeInfo = new SparseArray<T>();
			try {
				QueryBuilder<T, Integer> builder = staticDataHelper.getQueryBuilder(dataClazz);
				PreparedQuery<T> preppedQuery = builder.where().in(dataClazz.newInstance().uniqueIdName(), (Object[]) uniqueIDs).prepare();

				List<T> typeList = staticDataHelper.genericDataQuery(dataClazz, preppedQuery);

				// Convert the returned List into the Eden SparseArray format.
				for(T info : typeList)
					typeInfo.put(info.uniqueId(), info);

				return typeInfo;
			} catch(Exception e) {
				// If there is an error parsing, return an empty sparse Array
				return typeInfo;
			}
		}

		@Override
		protected void onPostExecute(SparseArray<T> storedTypes) {
			onCompleteRequestCallback.updateState(ApiCallback.STATE_CACHED_RESPONSE_ACQUIRED_VALID);
			onCompleteRequestCallback.onUpdate(storedTypes);
		}
	}
}
