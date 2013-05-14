package com.ubhave.datahandler;

import android.content.Context;
import android.content.Intent;

import com.ubhave.datahandler.alarm.AlarmListener;
import com.ubhave.datahandler.alarm.PolicyAlarm;
import com.ubhave.datahandler.config.DataHandlerConstants;
import com.ubhave.datahandler.config.DataTransferConfig;

public class DataTransferAlarmListener implements AlarmListener
{
	private final Context context;
	private final ESDataManager dataManager;
	private final PolicyAlarm policyAlarm;
	
	public DataTransferAlarmListener(final Context context, final ESDataManager dataManager)
	{
		this.context = context;
		this.dataManager = dataManager;
		policyAlarm = getPolicyAlarm();
	}
	
	public void setConnectionTypeAndStart(int connectionType)
	{
		if (connectionType == DataTransferConfig.CONNECTION_TYPE_WIFI)
		{
			policyAlarm.setTransferPolicy(PolicyAlarm.TRANSFER_POLICY.WIFI_ONLY);
		}
		else if (connectionType == DataTransferConfig.CONNECTION_TYPE_ANY)
		{
			policyAlarm.setTransferPolicy(PolicyAlarm.TRANSFER_POLICY.ANY_NETWORK);
		}
		
		policyAlarm.setListener(this);
		policyAlarm.start();
	}
	
	public void configUpdated()
	{
		policyAlarm.alarmIntervalUpdated();
	}
	
	public void stop()
	{
		if (policyAlarm.hasStarted())
		{
			policyAlarm.stop();
		}
	}
	
	private PolicyAlarm getPolicyAlarm()
	{
		Intent intent = new Intent(DataHandlerConstants.ACTION_NAME_DATA_TRANSFER_ALARM);
		PolicyAlarm policyAlarm = new PolicyAlarm(DataHandlerConstants.TRANSFER_ALARM_ID, context,
				intent,
				DataHandlerConstants.REQUEST_CODE_DATA_TRANSFER,
				DataHandlerConstants.ACTION_NAME_DATA_TRANSFER_ALARM,
				DataTransferConfig.TRANSFER_ALARM_INTERVAL,
				DataTransferConfig.WAIT_FOR_WIFI_INTERVAL_MILLIS);
		return policyAlarm;
	}
	
	@Override
	public boolean intentMatches(Intent intent)
	{
		return true;
	}

	@Override
	public void alarmTriggered()
	{
		new Thread()
		{
			public void run()
			{
				dataManager.transferStoredData();
			}
		}.start();
	}
}
