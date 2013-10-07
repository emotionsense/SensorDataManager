package com.ubhave.datahandler;

import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.ubhave.datahandler.config.DataStorageConfig;
import com.ubhave.datahandler.config.DataTransferConfig;
import com.ubhave.datahandler.except.DataHandlerException;
import com.ubhave.sensormanager.data.SensorData;

public abstract class AbstractDataLogger
{	
	protected ESDataManager dataManager;
	protected final Context context;
	
	protected AbstractDataLogger(Context context)
	{
		this.context = context;
		try
		{
			dataManager = ESDataManager.getInstance(context);
			dataManager.setConfig(DataStorageConfig.LOCAL_STORAGE_ROOT_DIRECTORY_NAME, getLocalStorageDirectoryName());
			dataManager.setConfig(DataStorageConfig.UNIQUE_USER_ID, getUserId());
			dataManager.setConfig(DataTransferConfig.POST_DATA_URL, getDataPostURL());
			dataManager.setConfig(DataTransferConfig.POST_DATA_URL_PASSWD, getPostPassword());
			dataManager.setConfig(DataStorageConfig.FILE_LIFE_MILLIS, getFileLifeMillis());
			dataManager.setConfig(DataTransferConfig.TRANSFER_ALARM_INTERVAL, getTransferAlarmLengthMillis());
		}
		catch (Exception e)
		{
			dataManager = null;
			e.printStackTrace();
		}
	}
	
	protected abstract String getLocalStorageDirectoryName();
	
	protected abstract String getUserId();
	
	protected abstract String getDataPostURL();
	
	protected abstract String getPostPassword();
	
	protected abstract long getFileLifeMillis();
	
	protected abstract long getTransferAlarmLengthMillis();

	private void log(final String tag, final String data)
	{
		new Thread()
		{
			@Override
			public void run()
			{
				super.run();
				try
				{
					Log.d(tag, data);
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
		try
		{
			dataManager.logSensorData(data);
		}
		catch (DataHandlerException e)
		{
			e.printStackTrace();
		}
	}
	
	public void logSurveyResponse(final String jsonResponse)
	{
		log(getSurveyResponseTag(), jsonResponse);
	}
	
	protected abstract String getSurveyResponseTag();
	
	protected abstract String getUserInteractionTag();
	
	public void logError(int appVersion, String tag, String error)
	{	
		try
		{
			if (tag != null && error != null)
			{
				JSONObject json = new JSONObject();
				json.put("applicationVersion", appVersion);
				json.put("activityTag", tag);
				json.put("timestamp", System.currentTimeMillis());
				json.put("message", error);
				json.put("user", getUserId());
				dataManager.logError(json.toString());
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void logInteraction(final String tag, final String action)
	{
		try
		{
			if (tag != null && action != null)
			{
				JSONObject json = new JSONObject();
				json.put("tag", tag);
				json.put("timestamp", System.currentTimeMillis());
				json.put("action", action);
				dataManager.logExtra(getUserInteractionTag(), json.toString());
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
