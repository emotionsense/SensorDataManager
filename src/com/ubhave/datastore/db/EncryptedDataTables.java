package com.ubhave.datastore.db;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteOpenHelper;

import org.json.JSONObject;

import android.content.Context;

import com.ubhave.dataformatter.json.JSONFormatter;
import com.ubhave.datahandler.config.DataHandlerConfig;
import com.ubhave.datahandler.config.DataStorageConfig;
import com.ubhave.sensormanager.data.SensorData;

public class EncryptedDataTables extends SQLiteOpenHelper implements DataTablesInterface
{
	private final static Object lock = new Object();
	private final static int dbVersion = 1;

	private String dataPassword;
	private final HashMap<String, EncryptedDataTable> dataTableMap;

	public EncryptedDataTables(final Context context, final String dbName, final String dataPassword)
	{
		super(context, dbName, null, dbVersion);
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

	private List<JSONObject> getData(final String tableName, final long timeLimit)
	{
		synchronized (lock)
		{
			List<JSONObject> data = null;
			EncryptedDataTable table = getTable(tableName);
			SQLiteDatabase database = getReadableDatabase(dataPassword);
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

	@Override
	public List<JSONObject> getUnsyncedData(final String tableName)
	{
		DataHandlerConfig config = DataHandlerConfig.getInstance();
		long timeLimit = (Long) config.get(DataStorageConfig.DATA_LIFE_MILLIS, DataStorageConfig.DEFAULT_FILE_LIFE_MILLIS);
		return getData(tableName, timeLimit);
	}

	@Override
	public void setSynced(final String tableName)
	{
		synchronized (lock)
		{
			EncryptedDataTable table = getTable(tableName);
			SQLiteDatabase database = getWritableDatabase(dataPassword);
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
