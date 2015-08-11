package com.ubhave.datahandler.transfer.alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ReceiverCallNotAllowedException;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.util.Log;

import com.ubhave.datahandler.config.DataHandlerConfig;
import com.ubhave.datahandler.config.DataTransferConfig;
import com.ubhave.datahandler.transfer.DataTransfer;

public class PolicyAlarm extends BroadcastReceiver
{
	private final AlarmManager alarmManager;
	private final PendingIntent pendingIntent;

	private final static String TRANSFER_ALARM_ID = "dataTransferAlarm";
	private final static String ACTION_NAME_DATA_TRANSFER_ALARM = "com.ubhave.datahandler.sync.DATA_TRANSFER_ALARM";
	private final static int REQUEST_CODE_DATA_TRANSFER = 8951;
	
	private final Context context;
	private boolean hasStarted;

	private AlarmListener listener;

	public PolicyAlarm(final Context context)
	{
		this.context = context;
		this.hasStarted = false;

		alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		
		Intent intent = new Intent(ACTION_NAME_DATA_TRANSFER_ALARM);
		pendingIntent = PendingIntent.getBroadcast(context, REQUEST_CODE_DATA_TRANSFER, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		setLastTransferAllowedTime(System.currentTimeMillis());
		if (DataHandlerConfig.shouldLog())
		{
			Log.d(DataTransfer.TAG, "Created policy alarm with id: "+TRANSFER_ALARM_ID);
		}
	}

	public void setListener(final AlarmListener listener)
	{
		this.listener = listener;
	}

	public void alarmIntervalUpdated()
	{
		if (DataHandlerConfig.shouldLog())
		{
			Log.d(DataTransfer.TAG, "Alarm interval updated.");
		}

		if (hasStarted)
		{
			stop();
			start();
		}
	}

	public void start()
	{
		if (!hasStarted)
		{
			try
			{	
				hasStarted = true;
				IntentFilter intentFilter = new IntentFilter(ACTION_NAME_DATA_TRANSFER_ALARM);
				long alarmInterval = (Long) DataHandlerConfig.getInstance().get(DataTransferConfig.TRANSFER_ALARM_INTERVAL, 0);
				if (DataHandlerConfig.shouldLog())
				{
					Log.d(DataTransfer.TAG, "Starting policy alarm with interval: "+(alarmInterval/1000L)+" seconds.");
				}
				alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), alarmInterval, pendingIntent);
				context.registerReceiver(this, intentFilter);
			}
			catch (ReceiverCallNotAllowedException e)
			{
				if (DataHandlerConfig.shouldLog())
				{
					Log.d(DataTransfer.TAG, "Error: ReceiverCallNotAllowedException (have you created the data manager in a broadcast receiver?)");
				}
				e.printStackTrace();
			}
		}
	}

	public void stop()
	{
		if (hasStarted)
		{
			try
			{
				if (DataHandlerConfig.shouldLog())
				{
					Log.d(DataTransfer.TAG, "Stopping policy alarm.");
				}
				hasStarted = false;
				alarmManager.cancel(pendingIntent);
				context.unregisterReceiver(this);
			}
			catch (IllegalArgumentException e)
			{
				// Thrown if the data manager is created from inside a broad
				// cast receiver
				// Since the receiver will not be registered
			}
		}
	}

	private void setLastTransferAllowedTime(long timestamp)
	{
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor prefsEditor = preferences.edit();
		prefsEditor.putLong(TRANSFER_ALARM_ID, timestamp);
		prefsEditor.commit();
	}

	public long getLastTransferAllowedTime()
	{
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		return preferences.getLong(TRANSFER_ALARM_ID, 0);
	}

	@Override
	public void onReceive(final Context context, final Intent intent)
	{
		if (listener != null)
		{
			if (listener.intentMatches(intent))
			{
				if (shouldAllowTransfer())
				{
					listener.alarmTriggered();
					setLastTransferAllowedTime(System.currentTimeMillis());
				}
			}
		}
	}

	private boolean shouldAllowTransfer()
	{
		if (isConnectedToWiFi())
		{
			if (DataHandlerConfig.shouldLog())
			{
				Log.d(DataTransfer.TAG, "Wi-Fi connection available.");
			}
			return true;
		}
		else if (isConnectedToNetwork())
		{
			if (isLastUploadTimeoutReached())
			{
				if (DataHandlerConfig.shouldLog())
				{
					Log.d(DataTransfer.TAG, "Wi-Fi connection unavailable, but data limit reached.");
				}
				return true;
			}
			else
			{
				if (DataHandlerConfig.shouldLog())
				{
					Log.d(DataTransfer.TAG, "Network available, but data limit not expired.");
				}
				return false;
			}
		}
		if (DataHandlerConfig.shouldLog())
		{
			Log.d(DataTransfer.TAG, "No connection available.");
		}
		return false;
	}

	private boolean isConnectedToWiFi()
	{
		ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo wifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		if (wifi.isConnected())
		{
			return true;
		}
		return false;
	}

	private boolean isConnectedToNetwork()
	{
		ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo mNetwork = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		if (mNetwork.isConnected())
		{
			return true;
		}
		return false;
	}

	private boolean isLastUploadTimeoutReached()
	{
		long lastTransferAllowed = getLastTransferAllowedTime();
		long waitForWifiInterval = (Long) DataHandlerConfig.getInstance().get(DataTransferConfig.WAIT_FOR_WIFI_INTERVAL_MILLIS, 0);
		if ((System.currentTimeMillis() - lastTransferAllowed) > waitForWifiInterval)
		{
			return true;
		}
		else
		{
			return false;
		}
	}
}
