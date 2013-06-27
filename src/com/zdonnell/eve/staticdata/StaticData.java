package com.zdonnell.eve.staticdata;

import android.content.Context;
import android.os.AsyncTask;
import android.util.SparseArray;

import com.zdonnell.androideveapi.link.ApiCallback;
import com.zdonnell.eve.helpers.Tools;

public class StaticData {

	private StaticTypeDatabase typeDatabase;

	private StationDatabase stationDatabase;

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

	}

	public void getTypeInfo(ApiCallback<SparseArray<TypeInfo>> callback, Integer... typeIDs) {
		new StaticTypeDatabaseRequest(callback).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, Tools.stripDuplicateIDs(typeIDs));
	}

	public void getStationInfo(ApiCallback<SparseArray<StationInfo>> callback, Integer... stationIDs) {
		new StationDatabaseRequest(callback).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, Tools.stripDuplicateIDs(stationIDs));
	}

	private class StationDatabaseRequest extends AsyncTask<Integer, Integer, SparseArray<StationInfo>> {
		private ApiCallback<SparseArray<StationInfo>> onCompleteRequestCallback;

		private Integer[] requestedTypeIDs;

		public StationDatabaseRequest(ApiCallback<SparseArray<StationInfo>> callback) {
			onCompleteRequestCallback = callback;
		}

		@Override
		protected SparseArray<StationInfo> doInBackground(Integer... typeIDs) {
			requestedTypeIDs = typeIDs;
			return stationDatabase.getStationInfo(requestedTypeIDs);
		}

		@Override
		protected void onPostExecute(SparseArray<StationInfo> storedTypes) {
			onCompleteRequestCallback.updateState(ApiCallback.STATE_CACHED_RESPONSE_ACQUIRED_VALID);
			onCompleteRequestCallback.onUpdate(storedTypes);
		}
	}

	private class StaticTypeDatabaseRequest extends AsyncTask<Integer, Integer, SparseArray<TypeInfo>> {
		private ApiCallback<SparseArray<TypeInfo>> onCompleteRequestCallback;

		private Integer[] requestedTypeIDs;

		public StaticTypeDatabaseRequest(ApiCallback<SparseArray<TypeInfo>> callback) {
			onCompleteRequestCallback = callback;
		}

		@Override
		protected SparseArray<TypeInfo> doInBackground(Integer... typeIDs) {
			requestedTypeIDs = typeIDs;
			return typeDatabase.getTypeInfo(requestedTypeIDs);
		}

		@Override
		protected void onPostExecute(SparseArray<TypeInfo> storedTypes) {
			onCompleteRequestCallback.updateState(ApiCallback.STATE_CACHED_RESPONSE_ACQUIRED_VALID);
			onCompleteRequestCallback.onUpdate(storedTypes);
		}
	}
}
