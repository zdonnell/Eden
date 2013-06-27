package com.zdonnell.eve.staticdata;

import java.util.List;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.util.SparseArray;

import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.zdonnell.androideveapi.link.ApiCallback;

public class StaticData {
	
	private StaticDataDBHelper staticDataHelper;

	public enum Table {
		INV_TYPES("invTypes", TypeInfo.class), STA_STATIONS("staStations", StationInfo.class);

		private String name;
		private Class<?> clazz;

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
	
	public <D extends IStaticDataType> void getStaticData(ApiCallback<SparseArray<D>> callback, Class<D> clazz, Integer... uniqueIDs) {
		new StaticDataRequest<D>(callback, clazz).execute();
	}
	
	class StaticDataRequest<T extends IStaticDataType> extends AsyncTask<Integer, Integer, SparseArray<T>> {
		private ApiCallback<SparseArray<T>> onCompleteRequestCallback;
		Class<T> dataClazz;
		
		public StaticDataRequest(ApiCallback<SparseArray<T>> callback, Class<T> clazz) {
			onCompleteRequestCallback = callback;
			this.dataClazz = clazz;
		}

		@Override
		protected SparseArray<T> doInBackground(Integer... uniqueIDs) {
			try {
				QueryBuilder<T, Integer> builder = staticDataHelper.getQueryBuilder(dataClazz);
				PreparedQuery<T> preppedQuery = builder.where().in(dataClazz.newInstance().uniqueIdName(), uniqueIDs).prepare();
				
				List<T> typeList = staticDataHelper.genericDataQuery(dataClazz, preppedQuery);
				SparseArray<T> typeInfo = new SparseArray<T>(typeList.size());
				for (T info : typeList) {
					Log.d("TEST", "TESTETST");
					typeInfo.put(info.uniqueId(), info);
				}
				
				return typeInfo;
			} catch (Exception e) {
				return null;
			}
		}

		@Override
		protected void onPostExecute(SparseArray<T> storedTypes) {
			onCompleteRequestCallback.updateState(ApiCallback.STATE_CACHED_RESPONSE_ACQUIRED_VALID);
			onCompleteRequestCallback.onUpdate(storedTypes);
		}
	}
}
