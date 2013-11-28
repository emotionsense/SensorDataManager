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
import android.util.Log;

import com.ubhave.datahandler.config.DataHandlerConfig;
import com.ubhave.datahandler.except.DataHandlerException;

public class PolicyAlarm extends BroadcastReceiver
{
	private final AlarmManager alarmManager;
	private final PendingIntent pendingIntent;

	private final String alarmId;
	private final String actionName;
	private final Context context;
	private boolean hasStarted;

	public enum TRANSFER_POLICY {
		WIFI_ONLY, ANY_NETWORK
	}

	private TRANSFER_POLICY transferPolicy;

	private AlarmListener listener;
	private final String configKeyAlarmInterval, configKeyWifiLimit;

	public PolicyAlarm(final String id, final Context context, final Intent intent, final int requestCode,
			final String actionName, String configKeyAlarmInterval, String configKeyWifiLimit)
	{
		this.alarmId = id;
		this.context = context;
		this.actionName = actionName;
		this.configKeyAlarmInterval = configKeyAlarmInterval;
		this.configKeyWifiLimit = configKeyAlarmInterval;
		
		this.hasStarted = false;
		this.transferPolicy = TRANSFER_POLICY.WIFI_ONLY;

		alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		setLastTransferAllowedTime(System.currentTimeMillis());
	}

	public void setListener(final AlarmListener listener)
	{
		this.listener = listener;
	}

	public void setTransferPolicy(TRANSFER_POLICY transferPolicy)
	{
		this.transferPolicy = transferPolicy;
	}

	public void alarmIntervalUpdated()
	{
		if (DataHandlerConfig.shouldLog())
		{
			Log.d("PolicyAlarm", "===== ALARM CONFIG UPDATING ====");
		}
		
		if (hasStarted)
		{
			stop();
			start();
		}
	}
	
	private long getValue(String key)
	{
		DataHandlerConfig config = DataHandlerConfig.getInstance();
		long value;
		try
		{
			value = (Long) config.get(key);
		}
		catch (DataHandlerException e)
		{
			value = 0;
			e.printStackTrace();
		}
		return value;
	}

	public void start()
	{
		if (!hasStarted)
		{
			hasStarted = true;
			IntentFilter intentFilter = new IntentFilter(actionName);
			long alarmInterval = getValue(configKeyAlarmInterval);
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
	
	public boolean hasStarted()
	{
		return hasStarted;
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
		// any network
		if (((transferPolicy == TRANSFER_POLICY.ANY_NETWORK) && (isConnectedToAnyNetwork()))
		// use only wifi
				|| ((transferPolicy == TRANSFER_POLICY.WIFI_ONLY) && (isConnectedToWiFi()))
				// use only wifi but if it's been more than 24 hours from the
				// last
				// upload time then use any available n/w
				|| ((transferPolicy == TRANSFER_POLICY.WIFI_ONLY) && (isLastUploadTimeoutReached()) && (isConnectedToAnyNetwork())))
		{
			return true;
		}
		else
		{
			return false;
		}
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

	private boolean isConnectedToAnyNetwork()
	{
		ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo wifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		NetworkInfo mNetwork = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

		if (wifi.isConnected())
		{
			return true;
		}

		if (mNetwork.isConnected())
		{
			return true;
		}

		return false;
	}

	private boolean isLastUploadTimeoutReached()
	{
		long lastTransferAllowed = getLastTransferAllowedTime();
		long waitForWifiInterval = getValue(configKeyWifiLimit);
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
