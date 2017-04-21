package com.ubhave.datastore.db;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.json.JSONObject;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.ubhave.dataformatter.json.JSONFormatter;
import com.ubhave.datahandler.config.DataHandlerConfig;
import com.ubhave.sensormanager.data.SensorData;

public class UnencryptedDataTables extends SQLiteOpenHelper implements DataTablesInterface
{
	private final static Object lock = new Object();
	private final static int dbVersion = 2;
	private static final String DATABASE_NAME = "sensor_datastore";

	private final HashMap<String, UnencryptedDataTable> dataTableMap;

	public UnencryptedDataTables(final Context context)
	{
		super(context, DATABASE_NAME, null, dbVersion);
		Log.d(DatabaseStorage.TAG, "UnencryptedDataTables constructor: "+DATABASE_NAME);
		this.dataTableMap = new HashMap<String, UnencryptedDataTable>();
	}

	@Override
	public void onCreate(final SQLiteDatabase db)
	{
		Log.d(DatabaseStorage.TAG, "Database onCreate()");
		for (UnencryptedDataTable table : dataTableMap.values())
		{
			if (DataHandlerConfig.shouldLog())
			{
				Log.d(DatabaseStorage.TAG, "Creating table in onCreate(): "+table.getName()+".");
			}
			table.createTable(db);
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
	}

	public Set<String> getTableNames()
	{
		return dataTableMap.keySet();
	}

	private UnencryptedDataTable getTable(final String tableName)
	{
		synchronized (lock)
		{
			if (!dataTableMap.containsKey(tableName))
			{
				if (DataHandlerConfig.shouldLog())
				{
					Log.d(DatabaseStorage.TAG, "Adding: "+tableName+" to table map.");
				}
				SQLiteDatabase database = getWritableDatabase();
				database.beginTransaction();
				try
				{
					UnencryptedDataTable table = new UnencryptedDataTable(tableName);
					table.createTable(database);
					dataTableMap.put(tableName, table);
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

	@Override
	public void writeData(final String tableName, final String data)
	{
		if (DataHandlerConfig.shouldLog())
		{
			Log.d(DatabaseStorage.TAG, "Writing to table: "+tableName+".");
		}
		
		UnencryptedDataTable table = getTable(tableName);
		SQLiteDatabase database = getWritableDatabase();
		database.beginTransaction();
		try
		{
			table.add(database, System.currentTimeMillis(), data);
			database.setTransactionSuccessful();
		}
		catch (Exception e)
		{
			Log.d(DatabaseStorage.TAG, ""+e.getLocalizedMessage());
			e.printStackTrace();
		}
		finally
		{
			database.endTransaction();
			database.close();
			close();
		}
	}

	@Override
	public List<SensorData> getRecentSensorData(final String tableName, final JSONFormatter formatter, final long timeLimit)
	{
		synchronized (lock)
		{
			List<SensorData> data = null;
			UnencryptedDataTable table = getTable(tableName);
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

	@Override
	public List<JSONObject> getUnsyncedData(final String tableName, final long maxAge)
	{
		if (DataHandlerConfig.shouldLog())
		{
			Log.d(DatabaseStorage.TAG, "Get unsynced data from: "+tableName+".");
		}
		synchronized (lock)
		{
			List<JSONObject> data = null;
			UnencryptedDataTable table = getTable(tableName);
			SQLiteDatabase database = getReadableDatabase();
			database.beginTransaction();
			try
			{
				data = table.getUnsyncedData(database, maxAge);
				database.setTransactionSuccessful();
				Log.d(DatabaseStorage.TAG, "Retrieved "+data.size()+" entries.");
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

	@Override
	public void setSynced(final String tableName, final long syncTime)
	{
		synchronized (lock)
		{
			UnencryptedDataTable table = getTable(tableName);
			SQLiteDatabase database = getWritableDatabase();
			database.beginTransaction();
			try
			{
				table.setSynced(database, syncTime);
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
