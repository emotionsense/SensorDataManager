package com.ubhave.datastore.db;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.json.JSONObject;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.ubhave.dataformatter.json.JSONFormatter;
import com.ubhave.datahandler.config.DataHandlerConfig;
import com.ubhave.datahandler.config.DataStorageConfig;
import com.ubhave.sensormanager.data.SensorData;

public class DataTables extends SQLiteOpenHelper
{
	private final static Object lock = new Object();
	private final static int dbVersion = 1;

	private final HashMap<String, DataTable> dataTableMap;

	public DataTables(final Context context, final String dbName)
	{
		super(context, dbName, null, dbVersion);
		this.dataTableMap = new HashMap<String, DataTable>();
	}

	@Override
	public void onCreate(final SQLiteDatabase db)
	{
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
	}

	public Set<String> getTableNames()
	{
		return dataTableMap.keySet();
	}

	public DataTable getTable(final String tableName)
	{
		synchronized (lock)
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
					database.close();
				}
			}
			return dataTableMap.get(tableName);
		}
	}

	public void writeData(final String tableName, final String data)
	{
		synchronized (lock)
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
	}

	public List<SensorData> getRecentSensorData(final String tableName, final JSONFormatter formatter, final long timeLimit)
	{
		synchronized (lock)
		{
			List<SensorData> data = null;
			DataTable table = getTable(tableName);
			SQLiteDatabase database = getReadableDatabase();
			database.beginTransaction();
			try
			{
				data = table.getRecentData(database, formatter, timeLimit);
				database.setTransactionSuccessful();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			finally
			{
				database.endTransaction();
				database.close();
			}
			return data;
		}
	}

	private List<JSONObject> getData(final String tableName, final long timeLimit)
	{
		synchronized (lock)
		{
			List<JSONObject> data = null;
			DataTable table = getTable(tableName);
			SQLiteDatabase database = getReadableDatabase();
			database.beginTransaction();
			try
			{
				data = table.getUnsyncedData(database, timeLimit);
				database.setTransactionSuccessful();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			finally
			{
				database.endTransaction();
				database.close();
			}
			return data;
		}
	}
	
	public List<JSONObject> getUnsyncedData(final String tableName)
	{
		long timeLimit;
		try
		{
			DataHandlerConfig config = DataHandlerConfig.getInstance();
			timeLimit = (Long) config.get(DataStorageConfig.DATA_LIFE_MILLIS);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			timeLimit = DataStorageConfig.DEFAULT_FILE_LIFE_MILLIS;
		}
		
		return getData(tableName, timeLimit);
	}

	public void setSynced(final String tableName)
	{
		synchronized (lock)
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
				database.close();
			}
		}
	}
}
