package com.ubhave.datastore.db;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteOpenHelper;

import org.json.JSONObject;

import android.content.Context;

import com.ubhave.dataformatter.json.JSONFormatter;
import com.ubhave.sensormanager.data.SensorData;

public class EncryptedDataTables extends SQLiteOpenHelper implements DataTablesInterface
{
	private final static Object lock = new Object();
	private final static int dbVersion = 1;
	private final static String DATABASE_NAME = "encrypted_sensor_datastore";

	private String dataPassword;
	private final HashMap<String, EncryptedDataTable> dataTableMap;

	public EncryptedDataTables(final Context context, final String dataPassword)
	{
		super(context, DATABASE_NAME, null, dbVersion);
		this.dataTableMap = new HashMap<String, EncryptedDataTable>();
		if (dataPassword == null)
		{
			this.dataPassword = "";
		}
		else
		{
			this.dataPassword = dataPassword;
		}
	}

	@Override
	public void onCreate(final SQLiteDatabase db)
	{
		for (EncryptedDataTable table : dataTableMap.values())
		{
			table.createTable(db);
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
	}

	@Override
	public Set<String> getTableNames()
	{
		return dataTableMap.keySet();
	}

	private EncryptedDataTable getTable(final String tableName)
	{
		synchronized (lock)
		{
			if (!dataTableMap.containsKey(tableName))
			{
				SQLiteDatabase database = getWritableDatabase(dataPassword);
				database.beginTransaction();
				try
				{
					EncryptedDataTable table = new EncryptedDataTable(tableName);
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
		synchronized (lock)
		{
			EncryptedDataTable table = getTable(tableName);
			SQLiteDatabase database = getWritableDatabase(dataPassword);
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

	@Override
	public List<SensorData> getRecentSensorData(final String tableName, final JSONFormatter formatter, final long timeLimit)
	{
		synchronized (lock)
		{
			List<SensorData> data = null;
			EncryptedDataTable table = getTable(tableName);
			SQLiteDatabase database = getReadableDatabase(dataPassword);
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
		synchronized (lock)
		{
			List<JSONObject> data = null;
			EncryptedDataTable table = getTable(tableName);
			SQLiteDatabase database = getReadableDatabase(dataPassword);
			database.beginTransaction();
			try
			{
				data = table.getUnsyncedData(database, maxAge);
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
	public void setSynced(final String tableName, final long maxDataAge)
	{
		synchronized (lock)
		{
			EncryptedDataTable table = getTable(tableName);
			SQLiteDatabase database = getWritableDatabase(dataPassword);
			database.beginTransaction();
			try
			{
				table.setSynced(database, maxDataAge);
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
