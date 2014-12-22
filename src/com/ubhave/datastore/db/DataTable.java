package com.ubhave.datastore.db;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.ubhave.dataformatter.json.JSONFormatter;
import com.ubhave.sensormanager.data.SensorData;

public class DataTable
{
	protected final String tableName;
	protected final static String timeStampKey = "timeStamp";
	protected final static String syncedWithServer = "synced";
	protected final static String dataKey = "data";
	
	public DataTable(final String tableName)
	{
		this.tableName = tableName.replaceAll(" ", "_");
	}
	
	public void createTable(final SQLiteDatabase database)
	{
		database.execSQL("CREATE TABLE IF NOT EXISTS " + tableName
				+ " ("
				+ timeStampKey + " INTEGER NOT NULL, "
				+ syncedWithServer + " INTEGER DEFAULT 0, "
				+ dataKey + " TEXT NOT NULL"
				+ ");");
	}
	
	public void add(final SQLiteDatabase database, final long entryTime, final String data)
	{
		ContentValues content = new ContentValues();
		content.put(timeStampKey, entryTime);
		content.put(dataKey, data);
		database.insert(tableName, null, content);
	}
	
	public List<JSONObject> getUnsyncedData(final SQLiteDatabase database)
	{
		ArrayList<JSONObject> unsyncedData = new ArrayList<JSONObject>();
		try
		{
			Cursor cursor = database.query(tableName, new String[]{dataKey}, syncedWithServer+" == ?", new String[]{"0"}, null, null, null);
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
		content.put(syncedWithServer, 1);
		database.update(tableName, content, null, null);
		
		// TODO: remove synced data
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
