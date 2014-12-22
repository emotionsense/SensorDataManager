package com.ubhave.datastore.db;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.json.JSONObject;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.ubhave.dataformatter.json.JSONFormatter;
import com.ubhave.sensormanager.data.SensorData;

public class DataTables extends SQLiteOpenHelper
{
	private final static int dbVersion = 1;
	
	private final HashMap<String, DataTable> dataTableMap;

	public DataTables(final Context context, final String dbName)
	{
		super(context, dbName, null, dbVersion);
		this.dataTableMap = new HashMap<String, DataTable>();
	}

	@Override
	public void onCreate(final SQLiteDatabase db)
	{}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{}
	
	public Set<String> getTableNames()
	{
		return dataTableMap.keySet();
	}

	public DataTable getTable(final String tableName)
	{
		if (!dataTableMap.containsKey(tableName))
		{
			SQLiteDatabase database = getWritableDatabase();
			database.beginTransaction();
			try
			{
				dataTableMap.put(tableName, new DataTable(database, tableName));
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
	
	public List<JSONObject> getUnsyncedData(final String tableName)
	{
		DataTable table = getTable(tableName);
		SQLiteDatabase database = getReadableDatabase();
		List<JSONObject> data = table.getUnsyncedData(database);
		database.close();
		return data;
	}
	
	public void setSynced(final String tableName)
	{	
		DataTable table = getTable(tableName);
		SQLiteDatabase database = getWritableDatabase();
		database.beginTransaction();
		try
		{
			table.setSynced(database);
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
}
