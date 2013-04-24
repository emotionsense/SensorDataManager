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

import com.ubhave.datahandler.config.FileSyncConfig;

public class SyncRequest extends BroadcastReceiver
{
	private final static String SYNC_URL_KEY = "SyncRequest";
	public static final String ACTION_NAME = "com.ubhave.datahandler.sync.SYNC_REQUEST_ALARM";
	private final static int REQUEST_CODE = 112;
	
	private final Context context;
	private FileUpdatedListener listener;
	private final String baseURL;
	private final String targetFile;
	
	private String requestTypeKey, dateParamValue, fileParamValue, dateResponseFieldKey;
	private HashMap<String, String> params;
	private long syncInterval;
	
	private final AlarmManager alarmManager;
	private final PendingIntent pendingIntent;
	private boolean hasStarted, isSyncing;

	public SyncRequest(final Context context, final String url, final String file)
	{
		this.context = context;
		this.targetFile = file;
		this.baseURL = url;
		
		Intent intent = new Intent(ACTION_NAME);
		intent.putExtra(SYNC_URL_KEY, url);
		alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		pendingIntent = PendingIntent.getBroadcast(context, REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
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
			IntentFilter intentFilter = new IntentFilter(ACTION_NAME);
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
		Log.d(SYNC_URL_KEY, "Broadcast Received: "+url);
		if (url != null)
		{
			if (baseURL.equals(url))
			{
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
				public void onPreExecute()
				{
					isSyncing = true;
				}
				
				@Override
				protected void onPostExecute(Void result)
				{
					isSyncing = false;
				}
			};
			s.setContext(context);
			s.setListener(listener);
			s.setBaseURL(baseURL);
			s.setTargetFile(targetFile);
			s.setParams(params);
			s.setRequestTypeKey(requestTypeKey);
			s.setGetDateValue(dateParamValue);
			s.setGetFileValue(fileParamValue);
			s.setDateResponseKey(dateResponseFieldKey);
			s.execute();
		}
	}
}
