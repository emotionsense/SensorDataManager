package com.ubhave.datahandler.loggertypes;

import java.util.ArrayList;

import org.json.JSONObject;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import com.ubhave.dataformatter.json.JSONFormatter;
import com.ubhave.datahandler.ESDataManager;
import com.ubhave.datahandler.config.DataHandlerConfig;
import com.ubhave.datahandler.config.DataStorageConfig;
import com.ubhave.datahandler.except.DataHandlerException;
import com.ubhave.sensormanager.ESException;
import com.ubhave.sensormanager.config.GlobalConfig;
import com.ubhave.sensormanager.data.SensorData;
import com.ubhave.sensormanager.data.log.InteractionData;

public abstract class AbstractDataLogger
{
	protected final static String LOG_TAG = "DataLogger";
	
	protected final ESDataManager dataManager;
	protected final Context context;

	protected AbstractDataLogger(final Context context, final int storageType) throws DataHandlerException, ESException
	{
		this.context = context;
		if (permissionGranted(storageType))
		{
			String dataPassword = getEncryptionPassword();
			if (dataPassword == null)
			{
				if (DataHandlerConfig.shouldLog())
				{
					Log.d(LOG_TAG, "Warning: no encryption password.");
				}
			}
			dataManager = ESDataManager.getInstance(context, storageType, dataPassword);
			configureDataStorage();
		}
		else
		{
			throw new DataHandlerException(DataHandlerException.MISSING_PERMISSIONS);
		}
	}

	protected ArrayList<String> getPermissions(int storageType)
	{
		ArrayList<String> permissions = new ArrayList<String>();
		if (storageType == DataStorageConfig.STORAGE_TYPE_FILES)
		{
			permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
		}
		return permissions;
	}
	
	private final boolean permissionGranted(int storageType)
	{
		for (String permission : getPermissions(storageType))
		{
			if (context.checkCallingOrSelfPermission(permission) != PackageManager.PERMISSION_GRANTED)
			{
				if (GlobalConfig.shouldLog())
				{
					Log.d(LOG_TAG, "Missing permission (for data logging): " + permission);
				}
				return false;
			}
		}
		return true;
	}

	public final ESDataManager getDataManager()
	{
		return dataManager;
	}

	protected void configureDataStorage() throws DataHandlerException
	{
		dataManager.setConfig(DataStorageConfig.UNIQUE_USER_ID, getUniqueUserId());
		dataManager.setConfig(DataStorageConfig.UNIQUE_DEVICE_ID, getDeviceId());
		dataManager.setConfig(DataStorageConfig.LOCAL_STORAGE_ROOT_NAME, getFileStorageName());
		dataManager.setConfig(DataHandlerConfig.PRINT_LOG_D_MESSAGES, shouldPrintLogMessages());
	}

	protected abstract String getFileStorageName();

	protected abstract String getUniqueUserId();
	
	protected abstract String getDeviceId();
	
	protected abstract boolean shouldPrintLogMessages();
	
	protected abstract String getEncryptionPassword();
	
	public final void setUniqueUserId(final String userId)
	{
		try
		{
			dataManager.setConfig(DataStorageConfig.UNIQUE_USER_ID, userId);
		}
		catch (DataHandlerException e)
		{
			if (DataHandlerConfig.shouldLog())
			{
				Log.d(LOG_TAG, ""+e.getLocalizedMessage());
				e.printStackTrace();
			}
		}
	}
	
	public final void setDeviceId(final String deviceId)
	{
		try
		{
			dataManager.setConfig(DataStorageConfig.UNIQUE_DEVICE_ID, deviceId);
		}
		catch (DataHandlerException e)
		{
			if (DataHandlerConfig.shouldLog())
			{
				Log.d(LOG_TAG, ""+e.getLocalizedMessage());
				e.printStackTrace();
			}
		}	
	}

	public final void log(final String tag, final String data)
	{
		if (tag != null && data != null)
		{
			new Thread()
			{
				@Override
				public void run()
				{
					try
					{
						dataManager.logExtra(tag, data);
					}
					catch (DataHandlerException e)
					{
						if (DataHandlerConfig.shouldLog())
						{
							Log.d(LOG_TAG, ""+e.getLocalizedMessage());
							e.printStackTrace();
						}
					}
				}
			}.start();
		}
	}

	public final void logSensorData(final SensorData data)
	{
		if (data != null)
		{
			new Thread()
			{
				@Override
				public void run()
				{
					try
					{
						dataManager.logSensorData(data);
					}
					catch (DataHandlerException e)
					{
						if (DataHandlerConfig.shouldLog())
						{
							Log.d(LOG_TAG, ""+e.getLocalizedMessage());
							e.printStackTrace();
						}
					}
				}
			}.start();
		}
		else if (DataHandlerConfig.shouldLog())
		{
			Log.d(LOG_TAG, "Failed logSensorData: null data");
		}
	}

	public final void logSensorData(final SensorData data, final JSONFormatter formatter)
	{
		if (data != null && formatter != null)
		{
			new Thread()
			{
				@Override
				public void run()
				{
					try
					{
						dataManager.logSensorData(data, formatter);
					}
					catch (DataHandlerException e)
					{
						e.printStackTrace();
					}
				}
			}.start();
		}
		else if (DataHandlerConfig.shouldLog())
		{
			Log.d(LOG_TAG, "Failed logSensorData: null data or formatter");
		}
	}

	public final void logInteraction(final InteractionData interaction)
	{
		logSensorData(interaction);
	}
	
	public final void logExtra(final String tag, final JSONObject json)
	{
		if (tag != null && json != null)
		{
			new Thread()
			{
				@Override
				public void run()
				{
					try
					{
						if (DataHandlerConfig.shouldLog())
						{
							Log.d(LOG_TAG, "logExtra: " + tag);
						}
						dataManager.logExtra(tag, json.toString());
					}
					catch (Exception e)
					{
						if (DataHandlerConfig.shouldLog())
						{
							Log.d(LOG_TAG, ""+e.getLocalizedMessage());
							e.printStackTrace();
						}
					}
				}
			}.start();
		}
		else if (DataHandlerConfig.shouldLog())
		{
			Log.d(LOG_TAG, "Failed logExtra: null arg");
		}
	}
}
