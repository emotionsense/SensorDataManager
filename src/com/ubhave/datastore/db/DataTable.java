package com.ubhave.datastore.db;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.database.Cursor;
import net.sqlcipher.database.SQLiteDatabase;
import android.util.Log;

import com.ubhave.dataformatter.json.JSONFormatter;
import com.ubhave.datahandler.config.DataHandlerConfig;
import com.ubhave.sensormanager.data.SensorData;

public class DataTable
{
	private final static String TAG = "DataTable";
	protected final String tableName;
	protected final static String timeStampKey = "timeStamp";
	protected final static String syncedWithServer = "synced";
	protected final static String dataKey = "data";
	
	private final static String SYNCED = "1";
	private final static String UNSYNCED = "0";
	
	public DataTable(final SQLiteDatabase database, final String tableName)
	{
		this.tableName = tableName.replaceAll(" ", "_");
		database.execSQL("CREATE TABLE IF NOT EXISTS " + tableName
				+ " ("
				+ timeStampKey + " INTEGER NOT NULL, "
				+ syncedWithServer + " INTEGER DEFAULT "+UNSYNCED+", "
				+ dataKey + " TEXT NOT NULL"
				+ ");");
	}
	
	public void add(final SQLiteDatabase database, final long entryTime, final String data) throws Exception
	{
		ContentValues content = new ContentValues();
		content.put(timeStampKey, entryTime);
		content.put(dataKey, data);
		long rowId = database.insert(tableName, null, content);
		if (rowId == -1)
		{
			throw new Exception("Data Not Inserted");
		}
		if (DataHandlerConfig.shouldLog())
		{
			Log.d(TAG, tableName+ " inserted into row: "+rowId);
		}
	}
	
	public List<JSONObject> getUnsyncedData(final SQLiteDatabase database, final long timeLimit)
	{
		ArrayList<JSONObject> unsyncedData = new ArrayList<JSONObject>();
		try
		{
			Cursor cursor = database.query(tableName, new String[]{dataKey}, syncedWithServer+" == ? AND "+timeStampKey+" > ?", new String[]{UNSYNCED, ""+timeLimit}, null, null, null);
			if (cursor != null)
			{
				int dataColumn = cursor.getColumnIndex(dataKey);
				cursor.moveToFirst();
				while (!cursor.isAfterLast())
				{
					try
					{
						JSONObject entry = new JSONObject(cursor.getString(dataColumn));
						unsyncedData.add(entry);
					}
					catch (JSONException e)
					{
						e.printStackTrace();
					}
					cursor.moveToNext();
				}
				cursor.close();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return unsyncedData;
	}
	
	public void setSynced(final SQLiteDatabase database)
	{
		ContentValues content = new ContentValues();
		content.put(syncedWithServer, SYNCED);
		database.update(tableName, content, null, null);
		int numRows = database.delete(tableName, syncedWithServer+" == ?", new String[]{SYNCED});
		if (DataHandlerConfig.shouldLog())
		{
			Log.d(TAG, "Deleted "+numRows+" synced rows from "+tableName);
		}
	}
	
	public List<SensorData> getRecentData(final SQLiteDatabase database, final JSONFormatter formatter, final long timeLimit)
	{
		ArrayList<SensorData> rows = new ArrayList<SensorData>();
		try
		{
			Cursor cursor = database.query(tableName, new String[]{dataKey}, timeStampKey+" > ?", new String[]{""+timeLimit}, null, null, null);
			if (cursor != null)
			{
				int eventIndex = cursor.getColumnIndex(dataKey);
				cursor.moveToFirst();
				while (!cursor.isAfterLast())
				{
					try
					{
						String data = cursor.getString(eventIndex);
						rows.add(formatter.toSensorData(data));
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
					cursor.moveToNext();
				}
				cursor.close();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return rows;
	}
}
