package com.ubhave.datahandler.loggertypes;

import java.util.ArrayList;

import org.json.JSONObject;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import com.ubhave.dataformatter.DataFormatter;
import com.ubhave.datahandler.ESDataManager;
import com.ubhave.datahandler.config.DataHandlerConfig;
import com.ubhave.datahandler.config.DataStorageConfig;
import com.ubhave.datahandler.except.DataHandlerException;
import com.ubhave.datahandler.logdata.ApplicationError;
import com.ubhave.datahandler.logdata.LogExtra;
import com.ubhave.datahandler.logdata.UserInteraction;
import com.ubhave.sensormanager.ESException;
import com.ubhave.sensormanager.config.GlobalConfig;
import com.ubhave.sensormanager.data.SensorData;

public abstract class AbstractDataLogger
{
	private final static String LOG_TAG = "AbstractDataLogger";
	protected ESDataManager dataManager;
	protected final Context context;
	protected final int storageType;

	protected AbstractDataLogger(final Context context, final int storageType) throws DataHandlerException, ESException
	{
		this.context = context;
		this.storageType = storageType;
		if (permissionGranted(storageType))
		{
			dataManager = ESDataManager.getInstance(context, storageType);
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
	
	protected boolean permissionGranted(int storageType)
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

	public ESDataManager getDataManager()
	{
		return dataManager;
	}

	protected void configureDataStorage()
	{
		try
		{
			dataManager.setConfig(DataHandlerConfig.PRINT_LOG_D_MESSAGES, shouldPrintLogMessages());
			dataManager.setConfig(DataStorageConfig.UNIQUE_USER_ID, getUniqueUserId());
			dataManager.setConfig(DataStorageConfig.UNIQUE_DEVICE_ID, getDeviceId());
			if (storageType == DataStorageConfig.STORAGE_TYPE_FILES)
			{
				dataManager.setConfig(DataStorageConfig.LOCAL_STORAGE_ROOT_NAME, getStorageName());
			}
		}
		catch (Exception e)
		{
			dataManager = null;
			e.printStackTrace();
		}
	}

	protected abstract String getStorageName();

	protected abstract String getUniqueUserId();

	protected abstract boolean shouldPrintLogMessages();

	protected abstract String getDeviceId();

	public void log(final String tag, final String data)
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
						e.printStackTrace();
					}
				}
			}.start();
		}
	}

	public void logSensorData(final SensorData data)
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
						e.printStackTrace();
					}
				}
			}.start();
		}
		else if (DataHandlerConfig.shouldLog())
		{
			Log.d(LOG_TAG, "Failed logSensorData: null data");
		}
	}

	public void logSensorData(final SensorData data, final DataFormatter formatter)
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

	public void logError(final ApplicationError error)
	{
		if (error != null)
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
							Log.d(LOG_TAG, "logError: " + error.getDataType());
						}
						JSONObject json = error.format(getUniqueUserId(), getDeviceId());
						dataManager.logError(json.toString());
					}
					catch (Exception e)
					{
						if (DataHandlerConfig.shouldLog())
						{
							Log.d(LOG_TAG, "logError: " + e.getLocalizedMessage());
						}
						e.printStackTrace();
					}
				}
			}.start();
		}
		else if (DataHandlerConfig.shouldLog())
		{
			Log.d(LOG_TAG, "Attempted to log null ApplicationError");
		}
	}

	public void logInteraction(final UserInteraction interaction)
	{
		if (interaction != null)
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
							Log.d(LOG_TAG, "logInteraction: " + interaction.getDataType());
						}
						JSONObject json = interaction.format(getUniqueUserId(), getDeviceId());
						dataManager.logExtra(UserInteraction.TAG, json.toString());
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}
			}.start();
		}
		else if (DataHandlerConfig.shouldLog())
		{
			Log.d(LOG_TAG, "Attempted to log null interaction.");
		}
	}

	public void logExtra(final String tag, final LogExtra action)
	{
		if (tag != null && action != null)
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
						JSONObject json = action.format(getUniqueUserId(), getDeviceId());
						dataManager.logExtra(tag, json.toString());
					}
					catch (Exception e)
					{
						e.printStackTrace();
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
