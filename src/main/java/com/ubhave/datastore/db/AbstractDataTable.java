package com.ubhave.datastore.db;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.database.Cursor;

import com.ubhave.dataformatter.json.JSONFormatter;
import com.ubhave.sensormanager.data.SensorData;

public abstract class AbstractDataTable
{	
	protected final String tableName;
	protected final static String primaryKey = "primaryKey";
	protected final static String timeStampKey = "timeStamp";
	protected final static String syncedWithServer = "synced";
	protected final static String dataKey = "data";
	
	protected final static String UNSYNCED_WHERE = syncedWithServer+" == ?";
	protected final static String UNSYNCED_AND_OLDER_THAN = UNSYNCED_WHERE+" AND "+timeStampKey+" < ?";
	protected final static String TIME_GREATER_THAN = timeStampKey+" > ?";
	
	protected final static int SYNCED = 1;
	protected final static int UNSYNCED = 0;
	
	public AbstractDataTable(final String tableName)
	{
		this.tableName = tableName.replaceAll(" ", "_").toLowerCase(Locale.US);
	}
	
	public String getName()
	{
		return tableName;
	}
	
	protected String getCreateTableQuery()
	{
		return "CREATE TABLE IF NOT EXISTS " + tableName+ " ("
				+ primaryKey + " INTEGER PRIMARY KEY, "
				+ timeStampKey + " INTEGER NOT NULL, "
				+ syncedWithServer + " INTEGER NOT NULL, "
				+ dataKey + " TEXT NOT NULL"
				+ ");";
	}
	
	protected ContentValues getContentValues(final long entryTime, final String data)
	{
		ContentValues content = new ContentValues();
		content.put(timeStampKey, entryTime);
		content.put(dataKey, data);
		content.put(syncedWithServer, UNSYNCED);
		return content;
	}
	
	protected List<JSONObject> formatCursorToJSON(final Cursor cursor)
	{
		ArrayList<JSONObject> unsyncedData = new ArrayList<JSONObject>();
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
		return unsyncedData;
	}
	
	protected ContentValues getSyncedContentValues()
	{
		ContentValues content = new ContentValues();
		content.put(syncedWithServer, SYNCED);
		return content;
	}
	
	protected List<SensorData> formatToSensorData(final JSONFormatter formatter, final Cursor cursor)
	{
		ArrayList<SensorData> rows = new ArrayList<SensorData>();
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
		return rows;
	}
}
