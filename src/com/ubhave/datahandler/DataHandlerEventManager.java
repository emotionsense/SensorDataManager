package com.ubhave.datahandler;

import android.content.Context;

import com.ubhave.datahandler.config.DataTransferConfig;
import com.ubhave.sensormanager.ESException;
import com.ubhave.sensormanager.ESSensorManager;
import com.ubhave.sensormanager.SensorDataListener;
import com.ubhave.sensormanager.data.SensorData;
import com.ubhave.sensormanager.data.pushsensor.ConnectionStateData;
import com.ubhave.sensormanager.sensors.SensorUtils;
import com.ubhave.triggermanager.TriggerException;
import com.ubhave.triggermanager.TriggerReceiver;

public class DataHandlerEventManager implements TriggerReceiver, SensorDataListener
{
	private final ESSensorManager sensorManager;
//	private final TriggerManager triggerManager;
	private int subscriptionId;
	private int currentPolicy;
	
	private final DataManager dataHandler;

	public DataHandlerEventManager(final Context context, final DataManager dataHandler) throws ESException, TriggerException
	{
		sensorManager = ESSensorManager.getSensorManager(context);
//		triggerManager = TriggerManager.getTriggerManager(context);
		
		/*
		 * TODO: implement transfer policy
		 */
		currentPolicy = DataTransferConfig.TRANSFER_ON_CONNECTION;
		this.dataHandler = dataHandler;
	}

	public void setPolicy(final int policy) throws DataHandlerException
	{
		if (currentPolicy != policy)
		{
			removeListener();
			addListener(policy);
			currentPolicy = policy;
		}
	}

	private void removeListener()
	{
		// TODO: untested
//		if (currentPolicy == DataTransferConfig.TRANFER_BULK_ON_INTERVAL)
//		{
//			triggerManager.removeTrigger(subscriptionId);
//		}
//		else
			if (currentPolicy == DataTransferConfig.TRANSFER_ON_CONNECTION || currentPolicy == DataTransferConfig.TRANSFER_ON_WIFI)
		{
			try
			{
				sensorManager.unsubscribeFromSensorData(subscriptionId);
			}
			catch (ESException e)
			{
				e.printStackTrace();
			}
		}
	}

	private void addListener(int policy)
	{
		try
		{
			// TODO: untested
//			if (policy == DataTransferConfig.TRANFER_BULK_ON_INTERVAL)
//			{
//				triggerManager.addTrigger(TriggerUtils.CLOCK_TRIGGER_ON_INTERVAL, this, null);
//			}
//			else
				if (policy == DataTransferConfig.TRANSFER_ON_WIFI)
			{
				sensorManager.subscribeToSensorData(SensorUtils.SENSOR_TYPE_CONNECTION_STATE, this);
			}
		}
//		catch (TriggerException e)
//		{
//			e.printStackTrace();
//		}
		catch (ESException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void onCrossingLowBatteryThreshold(boolean arg0)
	{
		// Nothing to do
	}

	@Override
	public void onNotificationTriggered()
	{
		dataHandler.transferStoredData();
	}

	@Override
	public void onDataSensed(SensorData data)
	{
		ConnectionStateData connectionData = (ConnectionStateData) data;
		if (connectionData.isConnected())
		{
			if (currentPolicy == DataTransferConfig.TRANSFER_ON_CONNECTION)
			{
				dataHandler.transferStoredData();
			}
			else if (currentPolicy == DataTransferConfig.TRANSFER_ON_WIFI)
			{
				if (connectionData.getNetworkType() == ConnectionStateData.WIFI_CONNECTION)
				{
					dataHandler.transferStoredData();
				}
			}
		}
	}
}
