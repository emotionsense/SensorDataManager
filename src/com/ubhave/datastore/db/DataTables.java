package com.ubhave.datastore.db;

import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.ubhave.dataformatter.json.JSONFormatter;
import com.ubhave.sensormanager.data.SensorData;

public class DataTables extends SQLiteOpenHelper
{
	private final static String dbName = "com.ubhave.datastore";
	private final static int dbVersion = 1;
	private final HashMap<String, DataTable> dataTableMap;

	public DataTables(final Context context)
	{
		super(context, dbName, null, dbVersion);
		this.dataTableMap = new HashMap<String, DataTable>();
	}

	@Override
	public void onCreate(SQLiteDatabase db)
	{
		// Nothing
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
		// TODO Auto-generated method stub
	}

	protected DataTable getTable(final String tableName)
	{
		if (!dataTableMap.containsKey(tableName))
		{
			dataTableMap.put(tableName, new DataTable(tableName));
		}
		return dataTableMap.get(tableName);
	}

	public void writeData(final String tableName, final String data)
	{
		DataTable table = getTable(tableName);
		SQLiteDatabase database = getWritableDatabase();
		database.beginTransaction();
		try
		{
			table.add(database, System.currentTimeMillis(), data);
			database.setTransactionSuccessful();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			database.endTransaction();
		}
	}

	public List<SensorData> getRecentSensorData(final String tableName, final JSONFormatter formatter, final long timeLimit)
	{
		DataTable table = getTable(tableName);
		SQLiteDatabase database = getReadableDatabase();
		List<SensorData> data = table.getRecentData(database, formatter, timeLimit);
		database.close();
		return data;
	}
}
