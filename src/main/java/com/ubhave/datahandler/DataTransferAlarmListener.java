package com.ubhave.datahandler;

import android.content.Context;
import android.content.Intent;

import com.ubhave.datahandler.transfer.alarm.AlarmListener;
import com.ubhave.datahandler.transfer.alarm.PolicyAlarm;

public class DataTransferAlarmListener implements AlarmListener
{
	private final ESDataManager dataManager;
	private final PolicyAlarm policyAlarm;
	
	public DataTransferAlarmListener(final Context context, final ESDataManager dataManager)
	{
		this.dataManager = dataManager;
		this.policyAlarm = new PolicyAlarm(context);
	}
	
	public void start()
	{
		policyAlarm.setListener(this);
		policyAlarm.start();
	}
	
	public void configUpdated()
	{
		policyAlarm.alarmIntervalUpdated();
	}
	
	public void stop()
	{
		policyAlarm.stop();
	}
	
	@Override
	public boolean intentMatches(final Intent intent)
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
