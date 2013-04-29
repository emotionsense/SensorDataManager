package com.ubhave.datahandler;

import android.content.Context;
import android.content.Intent;

import com.ubhave.datahandler.alarm.AlarmListener;
import com.ubhave.datahandler.alarm.PolicyAlarm;
import com.ubhave.datahandler.config.DataTransferConfig;

public class DataTransferAlarm implements AlarmListener
{
	public final static int REQUEST_CODE_DATA_TRANSFER = 8951;
	public final static String ACTION_NAME_DATA_TRANSFER_ALARM = "com.ubhave.datahandler.sync.DATA_TRANSFER_ALARM";
	
	private final Context context;
	private final ESDataManager dataManager;
	private final PolicyAlarm policyAlarm;
	
	public DataTransferAlarm(final Context context, final ESDataManager dataManager)
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
	
	public void stop()
	{
		if (policyAlarm.hasStarted())
		{
			policyAlarm.stop();
		}
	}
	
	private PolicyAlarm getPolicyAlarm()
	{
		Intent intent = new Intent(ACTION_NAME_DATA_TRANSFER_ALARM);
		PolicyAlarm policyAlarm = new PolicyAlarm("dataTransferAlarm", context, intent, REQUEST_CODE_DATA_TRANSFER, ACTION_NAME_DATA_TRANSFER_ALARM);
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
