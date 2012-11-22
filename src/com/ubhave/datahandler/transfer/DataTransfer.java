package com.ubhave.datahandler.transfer;

import java.util.ArrayList;

import android.content.Context;

import com.ubhave.sensormanager.ESException;
import com.ubhave.sensormanager.ESSensorManager;
import com.ubhave.sensormanager.SensorDataListener;
import com.ubhave.sensormanager.data.SensorData;
import com.ubhave.sensormanager.data.pushsensor.ConnectionStateData;
import com.ubhave.sensormanager.sensors.SensorUtils;

public class DataTransfer implements SensorDataListener
{
	public static final int IMMEDIATE_UPLOAD = 0;
	public static final int WIFI_ONLY_UPLOAD = 1;

	private final ArrayList<String> transferQueue;
	private boolean transfersAllowed;

	private ESSensorManager sensorManager;
	private int subscriptionId, uploadPolicy;

	public DataTransfer(final Context context)
	{
		transferQueue = loadQueueFromFile();
		
		transfersAllowed = true;
		uploadPolicy = IMMEDIATE_UPLOAD;
		
		try
		{
			sensorManager = ESSensorManager.getSensorManager(context);
			subscriptionId = sensorManager.subscribeToSensorData(SensorUtils.SENSOR_TYPE_CONNECTION_STATE, this);
		}
		catch (ESException e)
		{
			e.printStackTrace();
		}
	}
	
	public void setUploadPolicy(int policy)
	{
		uploadPolicy = policy;
	}

	public void queue(final String data)
	{
		transferQueue.add(data);
		if (uploadPolicy == IMMEDIATE_UPLOAD)
		{
			flushQueue();
		}
	}
	
	public void saveQueueToFile()
	{
		// Save to queue to file (e.g. when service is getting destroyed)
		
	}
	
	public ArrayList<String> loadQueueFromFile()
	{
		ArrayList<String> queue = new ArrayList<String>();
		// load from file
		// delete file
		return queue;
	}
	
	private void flushQueue()
	{
		if (transfersAllowed)
		{
			
		}
	}

	@Override
	public void onCrossingLowBatteryThreshold(boolean isBelowThreshold)
	{
		try
		{
			if (isBelowThreshold)
			{
				transfersAllowed = false;
				sensorManager.pauseSubscription(subscriptionId);
			}
			else
			{
				transfersAllowed = true;
				sensorManager.unPauseSubscription(subscriptionId);
			}
		}
		catch (ESException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void onDataSensed(SensorData data)
	{
		ConnectionStateData connectionData = (ConnectionStateData) data;
		if (connectionData.isConnectedOrConnecting())
		{
			if (uploadPolicy == IMMEDIATE_UPLOAD)
			{
				flushQueue();
			}
			else if (uploadPolicy == WIFI_ONLY_UPLOAD)
			{
				if (connectionData.getNetworkType() == ConnectionStateData.WIFI_CONNECTION)
				{
					flushQueue();
				}
			}
		}
	}
}
