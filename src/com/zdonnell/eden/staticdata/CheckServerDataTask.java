package com.zdonnell.eden.staticdata;

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

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.util.JsonReader;
import android.util.JsonToken;
import android.util.Log;

import com.zdonnell.eden.R;

/**
 * This Task serves as the primary means of keeping the local static data
 * database up to do. This should be called whenever it is assumed data may be
 * out of date or a new table becomes "tracked"
 * 
 * @author zach
 * 
 */
public class CheckServerDataTask extends AsyncTask<Void, Void, Void> {

	private final static String SERVER_STATUS_URL = "http://zdonnell.com/eve/api/status";
	private final static String SERVER_GENERIC_URL = "http://zdonnell.com/eve/api/";

	private final Context context;
	
	public CheckServerDataTask(Context context) {
		this.context = context;
	}

	@Override
	protected Void doInBackground(Void... params) {
		ServerVersionInfo info = checkServerStaticDBVersion();
		NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		
		// Check to see if any of the static data tables are "out of date"
		for(StaticData.Table table : StaticData.Table.values())
			if(info.versionNumber > checkLocalStaticDBVersion(table)) {
				notificationManager.notify(0, makeNotification(table, info.versionString));			
				downloadNewStaticData(info.versionNumber, table);
			}

		notificationManager.cancel(0);
		return null;
	}

	/**
	 * Checks the static data server to see what the most current static db
	 * version is.
	 * 
	 * @return the version of the current static data db, or
	 *         {@link Integer#MIN_VALUE} if the value could not be determined.
	 */
	private ServerVersionInfo checkServerStaticDBVersion() {
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(SERVER_STATUS_URL);

		String rawResponse = null;

		ServerVersionInfo info = new ServerVersionInfo();
		info.versionNumber = Integer.MIN_VALUE;
		
		try {
			HttpResponse response = httpclient.execute(httppost);
			HttpEntity returnEntity = response.getEntity();

			if(returnEntity != null) {
				rawResponse = EntityUtils.toString(returnEntity);
				JSONObject jsonResponse = new JSONObject(rawResponse);
				info.versionNumber = jsonResponse.getInt("DBVersionCode");
				info.versionString = jsonResponse.getString("DBVersionName");
			}
		} catch(Exception e) {
			Log.w("Eden: Static Data Downloader", "Failed to obtain server database version code");
		}

		return info;
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
	 * Downloads the static data and uses ORMLite to store the data<br>
	 * <br>
	 * 
	 * This works by using reflection to instantiate the correct Data classes as
	 * specified by the provided table enum. The values are set from the json by
	 * matching the json attribute names to field names in the corresponding
	 * class type. If the class type does not have a field with a name matching
	 * any given json attribute name, it will be skipped.<br>
	 * 
	 * @param newDBVersion
	 *            the database version for the data on the server
	 * @param table
	 *            the Table that needs to be downloaded.
	 * 
	 * @see IStaticDataType
	 */
	private void downloadNewStaticData(int newDBVersion, StaticData.Table table) {
		Class<?> dataClazz = table.clazz();
		final Set<Object> dataSet = new HashSet<Object>();

		try {
			URL url = new URL(SERVER_GENERIC_URL + "/" + table);
			JsonReader jsonReader = new JsonReader(new InputStreamReader(url.openStream()));
			jsonReader.beginArray();

			// Loop through all rows in the table
			while(jsonReader.hasNext()) {
				try {
					Object data = dataClazz.newInstance();
					jsonReader.beginObject();
					// Loop through all attributes of the jsonObject and try to
					// set a matching field in the instantiated data class.
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

			new StaticDataDBHelper(context).genericDataInsert(dataClazz, dataSet);

			updateLocalDBVersion(newDBVersion, table);
		} catch(Exception e) {
			Log.w("Eden: Static Data Downloader", "Failed to acquire " + table + " dataset");
		}
	}

	/**
	 * Sets field with the given name in the given object to the next value in
	 * the jsonReader.<br>
	 * <br>
	 * 
	 * It is not assumed that there will be a field matching the provided field
	 * name as this method is called for every jsonEntry for a given jsonObject.
	 * If there is no field of the given name, the current value is skipped.<br>
	 * <br>
	 * 
	 * If the field is found, but it's type cannot be determined, the value will
	 * be skipped.<br>
	 * 
	 * @param fieldName
	 *            the string name of the field to set
	 * @param data
	 *            the object to check for the fieldName in
	 * @param jsonReader
	 *            the jsonReader which should be pointing to the next available
	 *            value.
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws IOException
	 */
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
	
	/**
	 * returns an assembled notification
	 * 
	 * @param table
	 * @param dbString
	 * @return
	 */
	private Notification makeNotification(StaticData.Table table, String dbString) {
		return new NotificationCompat.Builder(context)
		        .setSmallIcon(R.drawable.ic_launcher)
		        .setContentTitle("Downloading: " + dbString + " static data")
		        .setContentText("table: " + table)
		        .setTicker("Downloading: " + dbString + " static data")
		        .setOngoing(true).build();
	}
	
	/**
	 * Simple wrapper class for the server db info
	 * 
	 * @author zdonnell
	 *
	 */
	private class ServerVersionInfo {
		public String versionString = "";
		public int versionNumber = Integer.MIN_VALUE;
	}
}
