package com.ubhave.datastore.db;

import java.util.ArrayList;
import java.util.List;

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
	
//	public void dropTable(final SQLiteDatabase database)
//	{
//		database.execSQL("DROP TABLE IF EXISTS " + tableName);
//	}
//	public void removeContents(final SQLiteDatabase database)
//	{
//		database.delete(tableName, null, null);
//	}
//	public void upgradeTable(final SQLiteDatabase database)
//	{
//		dropTable(database);
//		createTable(database);
//	}
	
	public void add(final SQLiteDatabase database, final long entryTime, final String data)
	{
		// TODO: is there a limit to the String length?
		ContentValues content = new ContentValues();
		content.put(timeStampKey, entryTime);
		content.put(dataKey, data);
		database.insert(tableName, null, content);
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
