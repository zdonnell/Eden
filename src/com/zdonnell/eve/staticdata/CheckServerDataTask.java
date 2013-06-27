package com.zdonnell.eve.staticdata;

import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.JsonReader;
import android.util.JsonToken;
import android.util.Log;

public class CheckServerDataTask extends AsyncTask<Void, Void, Void> {

	private final static String SERVER_STATUS_URL = "http://zdonnell.com/eve/api/status";
	private final static String SERVER_GENERIC_URL = "http://zdonnell.com/eve/api/";

	private final Context context;

	public CheckServerDataTask(Context context) {
		this.context = context;
	}

	@Override
	protected Void doInBackground(Void... params) {
		int serverStaticDBVersion = checkServerStaticDBVersion();

		// Check to see if any of the static data tables are "out of date"
		for(StaticData.Table table : StaticData.Table.values())
			if(serverStaticDBVersion > checkLocalStaticDBVersion(table))
				downloadNewStaticData(serverStaticDBVersion, table);

		return null;
	}

	/**
	 * Checks the static data server to see what the most current static db
	 * version is.
	 * 
	 * @return the version of the current static data db, or
	 *         {@link Integer#MIN_VALUE} if the value could not be determined.
	 */
	private int checkServerStaticDBVersion() {
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(SERVER_STATUS_URL);

		String rawResponse = null;
		int dbVersion = Integer.MIN_VALUE;

		try {
			HttpResponse response = httpclient.execute(httppost);
			HttpEntity returnEntity = response.getEntity();

			if(returnEntity != null) {
				rawResponse = EntityUtils.toString(returnEntity);
				JSONObject jsonResponse = new JSONObject(rawResponse);
				dbVersion = jsonResponse.getInt("DBVersionCode");
			}
		} catch(Exception e) {
			Log.w("Eden: Static Data Downloader", "Failed to obtain server database version code");
		}

		return dbVersion;
	}

	/**
	 * Checks the local version of the static data.
	 * 
	 * @return the version of the local static data db, or
	 *         {@link Integer#MIN_VALUE} if the database does not exist
	 */
	private int checkLocalStaticDBVersion(StaticData.Table table) {
		SharedPreferences prefs = context.getSharedPreferences("Eden_static_data", Context.MODE_PRIVATE);
		return prefs.getInt("static_data_dbversion_" + table, Integer.MIN_VALUE);
	}

	/**
	 * Attempts to download and store the recently updated static data.
	 */
	private void downloadNewStaticData(int newDBVersion, StaticData.Table table) {
		Class<?> dataClazz = table.clazz();
		Set<Object> dataSet = new HashSet<Object>();

		try {
			URL url = new URL(SERVER_GENERIC_URL + "/" + table);
			JsonReader jsonReader = new JsonReader(new InputStreamReader(url.openStream()));

			jsonReader.beginArray();
			while(jsonReader.hasNext()) {
				try {
					Object data = dataClazz.newInstance();
					jsonReader.beginObject();
					while(jsonReader.hasNext()) {
						String name = jsonReader.nextName();
						setField(name, data, jsonReader);
					}
					jsonReader.endObject();
					dataSet.add(data);
				} catch(Exception e) {
					Log.w("Eden: Static Data Downloader", "Failed to parse " + table + " entity");
				}
			}
			jsonReader.endArray();
			jsonReader.close();

			// TODO replace this code with more generic ORM db code
			switch(table) {
				case INV_TYPES:
					new StaticTypeDatabase(context).insertTypeInfo(dataSet);
					break;
				case STA_STATIONS:
					new StationDatabase(context).insertStationInfo(dataSet);
					break;
			}

			updateLocalDBVersion(newDBVersion, table);
		} catch(Exception e) {
			Log.w("Eden: Static Data Downloader", "Failed to acquire " + table + " dataset");
		}
	}

	private void setField(String fieldName, Object data, JsonReader jsonReader) throws IllegalArgumentException, IllegalAccessException, IOException {
		Field field = null;
		try {
			field = data.getClass().getField(fieldName);
		} catch(NoSuchFieldException e) {
			jsonReader.skipValue();
			return; // If there isn't a field in the data class matching a JSON
					// entry, just return
		}
		if(jsonReader.peek() == JsonToken.NULL) {
			jsonReader.skipValue();
			return;
		} else if(field.getType() == int.class) {
			field.setInt(data, jsonReader.nextInt());
		} else if(field.getType() == float.class) {
			field.setFloat(data, (float) jsonReader.nextDouble());
		} else if(field.getType() == boolean.class) {
			field.setBoolean(data, jsonReader.nextBoolean());
		} else if(field.getType() == double.class) {
			field.setDouble(data, jsonReader.nextDouble());
		} else if(field.getType() == long.class) {
			field.setLong(data, jsonReader.nextLong());
		} else if(field.getType() == String.class) {
			field.set(data, jsonReader.nextString());
		} else {
			jsonReader.skipValue();
		}
	}

	/**
	 * Sets the local static data db version number to the provided value
	 * 
	 * @param newDBVersion
	 */
	private void updateLocalDBVersion(int newDBVersion, StaticData.Table table) {
		SharedPreferences prefs = context.getSharedPreferences("Eden_static_data", Context.MODE_PRIVATE);
		prefs.edit().putInt("static_data_dbversion_" + table, newDBVersion).commit();
	}
}
