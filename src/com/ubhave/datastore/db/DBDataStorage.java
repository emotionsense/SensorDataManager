package com.ubhave.datastore.db;

import java.io.IOException;
import java.util.List;

import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.ubhave.dataformatter.DataFormatter;
import com.ubhave.dataformatter.json.JSONFormatter;
import com.ubhave.datahandler.config.DataHandlerConfig;
import com.ubhave.datahandler.config.DataStorageConfig;
import com.ubhave.datahandler.config.DataStorageConstants;
import com.ubhave.datahandler.except.DataHandlerException;
import com.ubhave.datahandler.transfer.async.UploadVault;
import com.ubhave.datahandler.transfer.async.UploadVaultInterface;
import com.ubhave.datastore.DataStorageInterface;
import com.ubhave.sensormanager.ESException;
import com.ubhave.sensormanager.data.SensorData;
import com.ubhave.sensormanager.sensors.SensorUtils;

public class DBDataStorage implements DataStorageInterface
{
	private static final String TAG = "LogDBDataStorage";

	private final Context context;
	private final DataHandlerConfig config;
	private final DataTablesInterface dataTables;
	private final UploadVaultInterface uploadVault;

	public DBDataStorage(final Context context, final String dataPassword)
	{
		this.context = context;
		this.config = DataHandlerConfig.getInstance();
		if (dataPassword == null)
		{
			this.dataTables = new UnencryptedDataTables(context, getDBName());
		}
		else
		{
			this.dataTables = new EncryptedDataTables(context, getDBName(), dataPassword);
		}
		this.uploadVault = UploadVault.getInstance(context, dataPassword);
	}

	private String getDBName()
	{
		return (String) config.get(DataStorageConfig.LOCAL_STORAGE_ROOT_NAME, DataStorageConfig.DEFAULT_DB_NAME);
	}

	@Override
	public void onDataUploaded()
	{
//		getCleanCacheDir();
		for (String tableName : dataTables.getTableNames())
		{
			Log.d("DB", tableName + " set synced");
			dataTables.setSynced(tableName);
		}
	}

	@Override
	public boolean prepareDataForUpload()
	{
		if (DataHandlerConfig.shouldLog())
		{
			Log.d(TAG, "DB prepareDataForUpload()");
		}

		int written = 0;
		for (String tableName : dataTables.getTableNames())
		{
			try
			{
				List<JSONObject> entries = dataTables.getUnsyncedData(tableName);
				if (DataHandlerConfig.shouldLog())
				{
					Log.d(TAG, "Prepare: " + tableName + " has " + entries.size() + " entries for upload.");
				}
				if (!entries.isEmpty())
				{
					uploadVault.writeData(tableName, entries);
					written++;
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		if (written == 0)
		{
			Log.d(TAG, "DB prepareDataForUpload(): no data to upload.");
		}
		return written != 0;
	}

	@Override
	public List<SensorData> getRecentSensorData(int sensorId, long startTimestamp) throws ESException, IOException
	{
		String tableName = SensorUtils.getSensorName(sensorId);
		JSONFormatter formatter = DataFormatter.getJSONFormatter(context, sensorId);
		return dataTables.getRecentSensorData(tableName, formatter, startTimestamp);
	}

	@Override
	public void logSensorData(final SensorData data, final DataFormatter formatter) throws DataHandlerException
	{
		String sensorName;
		try
		{
			sensorName = SensorUtils.getSensorName(data.getSensorType());
		}
		catch (ESException e)
		{
			sensorName = DataStorageConstants.UNKNOWN_SENSOR;
			e.printStackTrace();
		}
		dataTables.writeData(sensorName, formatter.toString(data));
	}

	@Override
	public void logExtra(final String tag, final String data) throws DataHandlerException
	{
		dataTables.writeData(tag, data);
	}
}
