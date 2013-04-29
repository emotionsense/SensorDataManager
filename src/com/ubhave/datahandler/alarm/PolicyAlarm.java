package com.ubhave.datahandler.alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;

public class PolicyAlarm extends BroadcastReceiver
{
	private final AlarmManager alarmManager;
	private final PendingIntent pendingIntent;
	
	private final String alarmId;
	private final String actionName;
	private final Context context;
	
	private long alarmInterval;
	private long waitForWifiInterval;
	private boolean hasStarted;
	
	private AlarmListener listener;
	
	public PolicyAlarm(final String id, final Context context, final Intent intent, final int requestCode, final String actionName)
	{
		this.alarmId = id;
		this.context = context;
		this.actionName = actionName;
		
		// TODO extract to appropriate config file
		this.alarmInterval = 15 * 60 * 1000; // 15 mins
		this.waitForWifiInterval = 24 * 60 * 60 * 1000; // 24 hrs
		this.hasStarted = false;
		
		alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		setLastTransferAllowedTime(System.currentTimeMillis());
	}
	
	public void setListener(final AlarmListener listener)
	{
		this.listener = listener;
	}
	
	public void setWaitForWifiLimit(long limit)
	{
		/*
		 * TODO
		 * if (waitForWifiInterval < alarmInterval)
		 * then if there is an active connection, transfers will always be allowed
		 * -- should this throw an exception?
		 */
		this.waitForWifiInterval = limit;
	}
	
	public void setSyncInterval(long syncInterval)
	{
		this.alarmInterval = syncInterval;
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
			hasStarted = true;
			IntentFilter intentFilter = new IntentFilter(actionName);
			alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), alarmInterval, pendingIntent);
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
	
	private void setLastTransferAllowedTime(long timestamp)
	{
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor prefsEditor = preferences.edit();
		prefsEditor.putLong(alarmId, timestamp);
		prefsEditor.commit();
	}
	
	public long getLastTransferAllowedTime()
	{
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		return preferences.getLong(alarmId, 0);
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
		ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo wifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		if (wifi.isConnected())
		{
			return true;
		}
		else
		{
			NetworkInfo mNetwork = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
			if (mNetwork.isConnected())
			{
				long lastTransferAllowed = getLastTransferAllowedTime();
				if ((System.currentTimeMillis() - lastTransferAllowed) > waitForWifiInterval)
				{
					return true;
				}
			}
		}
		return false;
	}
}
