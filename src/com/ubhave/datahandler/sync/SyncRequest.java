package com.ubhave.datahandler.sync;

import java.util.HashMap;
import java.util.Set;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.ubhave.datahandler.ESDataManager;
import com.ubhave.datahandler.config.FileSyncConfig;

public class SyncRequest extends BroadcastReceiver
{
	private final static String SYNC_URL_KEY = "SyncRequest";

	
	private final Context context;
	private final String baseURL;
	private final String targetFile;
	
	private String requestTypeKey, dateParamValue, fileParamValue, dateResponseFieldKey;
	private HashMap<String, String> params;
	private long syncInterval;
	
	private FileUpdatedListener listener;
	private final AlarmManager alarmManager;
	private final PendingIntent pendingIntent;
	private boolean hasStarted, isSyncing;

	public SyncRequest(final Context context, final String url, final String file)
	{
		this.context = context;
		this.targetFile = file;
		this.baseURL = url;
		
		Intent intent = new Intent(ESDataManager.ACTION_NAME_SYNC_REQUEST_ALARM);
		intent.putExtra(SYNC_URL_KEY, url);
		alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		pendingIntent = PendingIntent.getBroadcast(context, ESDataManager.REQUEST_CODE_SYNC_REQUEST, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		hasStarted = false;
		isSyncing = false;
		
		// Set up default values
		this.params = new HashMap<String, String>();
		this.requestTypeKey = FileSyncConfig.DEFAULT_REQUEST_TYPE_PARAM;
		this.dateParamValue = FileSyncConfig.DEFAULT_REQUEST_DATE_MODIFIED;
		this.fileParamValue = FileSyncConfig.DEFAULT_REQUEST_GET_FILE;
		this.syncInterval = FileSyncConfig.DEFAULT_DAILY_SYNC_FREQUENCY;
		this.dateResponseFieldKey = FileSyncConfig.DEFAULT_RESPONSE_DATE_MODIFIED;
		this.listener = null;
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
		if (!hasStarted)
		{
			hasStarted = true;
			attemptSync();
			IntentFilter intentFilter = new IntentFilter(ESDataManager.ACTION_NAME_SYNC_REQUEST_ALARM);
			alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), syncInterval, pendingIntent);
			context.registerReceiver(this, intentFilter);
		}
	}
	
	public void stop()
	{
		if (hasStarted)
		{
			hasStarted = false;
			alarmManager.cancel(pendingIntent);
			context.unregisterReceiver(this);
		}
	}
	
	@Override
	public void onReceive(Context context, Intent intent)
	{
		String url = intent.getStringExtra(SYNC_URL_KEY);
		if (url != null)
		{
			if (baseURL.equals(url))
			{
				Log.d(SYNC_URL_KEY, "Sync attempt starting");
				attemptSync();
			}
		}
	}

	public void attemptSync()
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
						Log.d("SyncRequest", "Notifying listener");
						notifyListener();
					}
					else
					{
						Log.d("SyncRequest", "Result: "+result.toString());
						Log.d("SyncRequest", "Listener null: "+(listener==null));
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
	
	private void notifyListener()
	{
		if (listener != null)
		{
			listener.onFileUpdated();
		}
	}
}
