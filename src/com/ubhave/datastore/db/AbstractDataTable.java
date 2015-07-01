package com.ubhave.datastore.db;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.database.Cursor;

import com.ubhave.dataformatter.json.JSONFormatter;
import com.ubhave.sensormanager.data.SensorData;

public abstract class AbstractDataTable
{	
	protected final String tableName;
	protected final static String timeStampKey = "timeStamp";
	protected final static String syncedWithServer = "synced";
	protected final static String dataKey = "data";
	
	protected final static String SYNCED = "1";
	protected final static String UNSYNCED = "0";
	
	public AbstractDataTable(final String tableName)
	{
		this.tableName = tableName.replaceAll(" ", "_");
	}
	
	protected String getCreateTableQuery()
	{
		return "CREATE TABLE IF NOT EXISTS " + tableName
				+ "("
				+ timeStampKey + " INTEGER NOT NULL, "
				+ syncedWithServer + " INTEGER DEFAULT "+UNSYNCED+", "
				+ dataKey + " TEXT NOT NULL"
				+ ");";
	}
	
	protected ContentValues getContentValues(final long entryTime, final String data)
	{
		ContentValues content = new ContentValues();
		content.put(timeStampKey, entryTime);
		content.put(dataKey, data);
		return content;
	}
	
	protected List<JSONObject> getUnsyncedData(final Cursor cursor)
	{
		ArrayList<JSONObject> unsyncedData = new ArrayList<JSONObject>();
		try
		{
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
	
	protected ContentValues getSyncedContentValues()
	{
		ContentValues content = new ContentValues();
		content.put(syncedWithServer, SYNCED);
		return content;
	}
	
	protected List<SensorData> getRecentData(final JSONFormatter formatter, final Cursor cursor)
	{
		ArrayList<SensorData> rows = new ArrayList<SensorData>();
		try
		{
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
