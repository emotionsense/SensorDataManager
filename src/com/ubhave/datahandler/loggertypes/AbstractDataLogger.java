package com.ubhave.datahandler.loggertypes;

import org.json.JSONObject;

import android.content.Context;
import android.telephony.TelephonyManager;

import com.ubhave.dataformatter.DataFormatter;
import com.ubhave.datahandler.ESDataManager;
import com.ubhave.datahandler.config.DataHandlerConfig;
import com.ubhave.datahandler.config.DataStorageConfig;
import com.ubhave.datahandler.except.DataHandlerException;
import com.ubhave.sensormanager.data.SensorData;

public abstract class AbstractDataLogger
{
	protected final static String TAG_SURVEY_RESPONSE = "Survey";
	protected final static String TAG_INTERACTION = "Interaction";
	protected final static String TAG_TIMESTAMP = "timestamp";

	protected ESDataManager dataManager;
	protected final Context context;

	protected AbstractDataLogger(Context context)
	{
		this.context = context;
		try
		{
			dataManager = ESDataManager.getInstance(context);
			configureDataStorage();
		}
		catch (Exception e)
		{
			dataManager = null;
		}
	}

	protected void configureDataStorage()
	{
		try
		{
			dataManager.setConfig(DataHandlerConfig.PRINT_LOG_D_MESSAGES, shouldPrintLogMessages());
			dataManager.setConfig(DataStorageConfig.LOCAL_STORAGE_ROOT_DIRECTORY_NAME, getLocalStorageDirectoryName());
			dataManager.setConfig(DataStorageConfig.UNIQUE_USER_ID, getUniqueUserId());
			dataManager.setConfig(DataStorageConfig.UNIQUE_DEVICE_ID, getDeviceId());
		}
		catch (Exception e)
		{
			dataManager = null;
			e.printStackTrace();
		}
	}

	protected abstract String getLocalStorageDirectoryName();

	protected abstract String getUniqueUserId();
	
	protected abstract boolean shouldPrintLogMessages();
	
	protected String getDeviceId()
	{
		TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		try
		{
			return telephonyManager.getDeviceId();
		}
		catch (Exception e)
		{
			return "DeviceIdMissing";
		}
	}

	public void log(final String tag, final String data)
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

	public void logSensorData(final SensorData data)
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
	
	public void logSensorData(final SensorData data, final DataFormatter formatter)
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

	public void logSurveyResponse(final String jsonResponse)
	{
		log(TAG_SURVEY_RESPONSE, jsonResponse);
	}

	public void logError(final int appVersion, final String tag, final String error)
	{
		new Thread()
		{
			@Override
			public void run()
			{
				try
				{
					if (tag != null && error != null)
					{
						JSONObject json = new JSONObject();
						json.put("applicationVersion", appVersion);
						json.put("activityTag", tag);
						json.put(TAG_TIMESTAMP, System.currentTimeMillis());
						json.put("message", error);
						json.put("user", getUniqueUserId());
						dataManager.logError(json.toString());
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}.start();
	}

	/*
	 * Should use: logExtra() below instead of logInteraction()
	 * Log Interaction is missing user-id/device-id, and any other relevant app info
	 */
//	public void logInteraction(final String tag, final String action)
//	{
//		try
//		{
//			if (tag != null && action != null)
//			{
//				JSONObject json = new JSONObject();
//				json.put("tag", tag);
//				json.put(TAG_TIMESTAMP, System.currentTimeMillis());
//				json.put("action", action);
//				dataManager.logExtra(TAG_INTERACTION, json.toString());
//			}
//		}
//		catch (Exception e)
//		{
//			e.printStackTrace();
//		}
//	}
	
	public void logExtra(final String tag, final JSONObject action)
	{
		try
		{
			if (tag != null && action != null)
			{
				dataManager.logExtra(tag, action.toString());
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
