package com.ubhave.datahandler.sync;

import java.util.HashMap;
import java.util.Set;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.ubhave.datahandler.alarm.AlarmListener;
import com.ubhave.datahandler.alarm.PolicyAlarm;
import com.ubhave.datahandler.config.DataHandlerConstants;
import com.ubhave.datahandler.config.FileSyncConfig;

public class SyncRequest implements AlarmListener
{
	private final static String SYNC_URL_KEY = "SyncRequest";

	private final Context context;
	private final String baseURL;
	private final String targetFile;
	
	private String requestTypeKey, dateParamValue, fileParamValue, dateResponseFieldKey;
	private HashMap<String, String> params;
	private long syncInterval;
	
	private FileUpdatedListener listener;
	private final PolicyAlarm alarm;
	private boolean isSyncing;

	public SyncRequest(final Context context, final String url, final String file)
	{
		this.context = context;
		this.targetFile = file;
		this.baseURL = url;
		
		alarm = getAlarm();
		isSyncing = false;
		
		// Set up default values
		this.params = new HashMap<String, String>();
		this.requestTypeKey = FileSyncConfig.DEFAULT_REQUEST_TYPE_PARAM;
		this.dateParamValue = FileSyncConfig.DEFAULT_REQUEST_DATE_MODIFIED;
		this.fileParamValue = FileSyncConfig.DEFAULT_REQUEST_GET_FILE;
		this.syncInterval = FileSyncConfig.DEFAULT_WIFI_SYNC_LIMIT;
		this.dateResponseFieldKey = FileSyncConfig.DEFAULT_RESPONSE_DATE_MODIFIED;
		this.listener = null;
	}
	
	private PolicyAlarm getAlarm()
	{
		Intent intent = new Intent(DataHandlerConstants.ACTION_NAME_SYNC_REQUEST_ALARM);
		intent.putExtra(SYNC_URL_KEY, baseURL);
		
		PolicyAlarm alarm = new PolicyAlarm(targetFile, context, intent, DataHandlerConstants.REQUEST_CODE_SYNC_REQUEST, DataHandlerConstants.ACTION_NAME_SYNC_REQUEST_ALARM);
		alarm.setListener(this);
		alarm.setSyncInterval(FileSyncConfig.DEFAULT_SYNC_FREQUENCY);
		alarm.setWaitForWifiLimit(FileSyncConfig.DEFAULT_WIFI_SYNC_LIMIT);
		
		return alarm;
	}
	
	public void setListener(FileUpdatedListener listener)
	{
		this.listener = listener;
	}
	
	public void setParams(HashMap<String, String> params)
	{
		this.params = params;
	}
	
	public void setRequestTypeKey(String key)
	{
		requestTypeKey = key;
	}
	
	public void setGetDateValue(String value)
	{
		dateParamValue = value;
	}
	
	public void setGetFileValue(String key, String value)
	{
		fileParamValue = value;
	}
	
	public void setDateResponseKey(String value)
	{
		dateResponseFieldKey = value;
	}
	
	public boolean equals(final SyncRequest otherRequest)
	{
		if (baseURL.equals(otherRequest.getBaseURL()))
		{
			HashMap<String, String> otherParams = otherRequest.getParams();
			Set<String> paramKeys = params.keySet();
			if (paramKeys.containsAll(otherParams.keySet()))
			{
				for (String key : paramKeys)
				{
					if (!params.get(key).equals(otherParams.get(key)))
					{
						return false;
					}
				}
				return true;
			}
		}
		return false;
	}
	
	public String getBaseURL()
	{
		return baseURL;
	}

	public void setSyncInterval(long interval)
	{
		this.syncInterval = interval;
	}

	public long getSyncInterval()
	{
		return syncInterval;
	}

	public void setParam(final String key, final String value)
	{
		params.put(key, value);
	}

	public HashMap<String, String> getParams()
	{
		return params;
	}

	public void start()
	{
		alarm.start();
	}
	
	public void stop()
	{
		alarm.stop();
	}
	
	private void notifyListener()
	{
		if (listener != null)
		{
			listener.onFileUpdated();
		}
	}

	@Override
	public void alarmTriggered()
	{
		if (!isSyncing)
		{
			SyncTask s = new SyncTask()
			{
				@Override
				protected void onPreExecute()
				{
					super.onPreExecute();
					isSyncing = true;
				}
				
				@Override
				protected void onPostExecute(Boolean result)
				{
					super.onPostExecute(result);
					if (result && listener != null)
					{
						notifyListener();
					}
					isSyncing = false;
				}
				
			};
			s.setContext(context);
			s.setBaseURL(baseURL);
			s.setTargetFile(targetFile);
			s.setParams(params);
			s.setRequestTypeKey(requestTypeKey);
			s.setGetDateValue(dateParamValue);
			s.setGetFileValue(fileParamValue);
			s.setDateResponseKey(dateResponseFieldKey);
			s.execute();
		}
		else
		{
			Log.d(SYNC_URL_KEY, "Sync in progress");
		}
	}

	@Override
	public boolean intentMatches(Intent intent)
	{
		String url = intent.getStringExtra(SYNC_URL_KEY);
		if (url != null)
		{
			return baseURL.equals(url);
		}
		return false;
	}
}
