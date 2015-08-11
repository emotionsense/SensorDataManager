package com.ubhave.datastore.db;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.ubhave.dataformatter.DataFormatter;
import com.ubhave.dataformatter.json.JSONFormatter;
import com.ubhave.datahandler.config.DataHandlerConfig;
import com.ubhave.datahandler.config.DataStorageConfig;
import com.ubhave.datahandler.except.DataHandlerException;
import com.ubhave.datahandler.transfer.async.UploadVault;
import com.ubhave.datahandler.transfer.async.UploadVaultInterface;
import com.ubhave.datastore.DataStorageInterface;
import com.ubhave.sensormanager.ESException;
import com.ubhave.sensormanager.data.SensorData;
import com.ubhave.sensormanager.sensors.SensorUtils;

public class DatabaseStorage implements DataStorageInterface
{
	public static final String TAG = "DatabaseStorage";

	private final Context context;
	private final DataTablesInterface dataTables;
	private final UploadVaultInterface uploadVault;

	public DatabaseStorage(final Context context, final String dataPassword) throws DataHandlerException
	{
		this.context = context;
		this.uploadVault = UploadVault.getInstance(context, dataPassword);
		if (dataPassword == null)
		{
			Log.d(TAG, "Creating unencrypted data storage");
			this.dataTables = new UnencryptedDataTables(context);
		}
		else
		{
			Log.d(TAG, "Creating encrypted data storage");
			this.dataTables = new EncryptedDataTables(context, dataPassword);
		}
	}
	
	private long getPrepareTime()
	{
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		return preferences.getLong(TAG, 0);
	}
	
	private void setPrepareTime(long time)
	{
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor prefsEditor = preferences.edit();
		prefsEditor.putLong(TAG, time);
		prefsEditor.commit();
	}

	@Override
	public void onDataUploaded()
	{
		long dataUploadTime = getPrepareTime();
		for (String tableName : dataTables.getTableNames())
		{
			if (DataHandlerConfig.shouldLog())
			{
				Log.d(TAG, tableName + " set synced");
			}
			dataTables.setSynced(tableName, dataUploadTime);
		}
	}
	
	@Override
	public void onDataUploadFailed()
	{
		try
		{
			File uploadDirectory = uploadVault.getUploadDirectory();
			File[] uploadFiles = uploadDirectory.listFiles();
			if (uploadFiles != null && uploadFiles.length != 0)
			{
				for (File file : uploadFiles)
				{
					if (DataHandlerConfig.shouldLog())
					{
						Log.d(TAG, "Remove upload file: "+file.getName());
					}
					file.delete();
				}
			}
		}
		catch (DataHandlerException e)
		{
			if (DataHandlerConfig.shouldLog())
			{
				e.printStackTrace();
			}
		}
	}

	@Override
	public boolean prepareDataForUpload()
	{
		int written = 0;
		DataHandlerConfig config = DataHandlerConfig.getInstance();
		long timeLimit = (Long) config.get(DataStorageConfig.DATA_LIFE_MILLIS, DataStorageConfig.DEFAULT_FILE_LIFE_MILLIS);
		long maxDataAge = System.currentTimeMillis() - timeLimit;
		setPrepareTime(maxDataAge);
		
		if (DataHandlerConfig.shouldLog())
		{
			Log.d(TAG, "Preparing data for upload.");
			Log.d(DatabaseStorage.TAG, "Max data age: "+maxDataAge);
		}	
		
		for (String tableName : dataTables.getTableNames())
		{
			try
			{
				List<JSONObject> entries = dataTables.getUnsyncedData(tableName, maxDataAge);
				if (DataHandlerConfig.shouldLog())
				{
					Log.d(TAG, "Table: " + tableName + " has " + entries.size() + " entries for upload.");
				}
				if (!entries.isEmpty())
				{
					uploadVault.writeData(tableName, entries);
					written++;
				}
			}
			catch (Exception e)
			{
				if (DataHandlerConfig.shouldLog())
				{
					e.printStackTrace();
				}
			}
		}
		if (written == 0)
		{
			Log.d(TAG, "No data to upload.");
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
			dataTables.writeData(sensorName, formatter.toString(data));
		}
		catch (ESException e)
		{
			throw new DataHandlerException(DataHandlerException.UNKNOWN_CONFIG);
		}
	}

	@Override
	public void logExtra(final String tag, final String data) throws DataHandlerException
	{
		dataTables.writeData(tag, data);
	}
}
