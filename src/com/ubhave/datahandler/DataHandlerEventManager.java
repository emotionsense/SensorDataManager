package com.ubhave.datahandler;

import android.content.Context;

import com.ubhave.datahandler.config.DataHandlerConfig;
import com.ubhave.datahandler.config.DataTransferConfig;
import com.ubhave.datahandler.except.DataHandlerException;
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
	// private final TriggerManager triggerManager;
	private int subscriptionId;
	private int currentPolicy;

	private final DataManager dataHandler;
	
	// TODO
	// check if this class is required

	public DataHandlerEventManager(final Context context, final DataManager dataHandler) throws ESException,
			TriggerException, DataHandlerException
	{
		sensorManager = ESSensorManager.getSensorManager(context);
		// triggerManager = TriggerManager.getTriggerManager(context);

		DataHandlerConfig config = DataHandlerConfig.getInstance();
		currentPolicy = (Integer) config.get(DataTransferConfig.DATA_TRANSER_POLICY);
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
		// if (currentPolicy == DataTransferConfig.TRANFER_BULK_ON_INTERVAL)
		// {
		// triggerManager.removeTrigger(subscriptionId);
		// }
		// else
		if (currentPolicy == DataTransferConfig.TRANSFER_PERIODICALLY)
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
			// if (policy == DataTransferConfig.TRANFER_BULK_ON_INTERVAL)
			// {
			// triggerManager.addTrigger(TriggerUtils.CLOCK_TRIGGER_ON_INTERVAL,
			// this, null);
			// }
			// else
			if (policy == DataTransferConfig.TRANSFER_PERIODICALLY)
			{
				sensorManager.subscribeToSensorData(SensorUtils.SENSOR_TYPE_CONNECTION_STATE, this);
			}
		}
		// catch (TriggerException e)
		// {
		// e.printStackTrace();
		// }
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
			// TODO
			// check this implementation
			/*if (currentPolicy == DataTransferConfig.TRANSFER_ON_CONNECTION)
			{
				dataHandler.transferStoredData();
			}
			else if (currentPolicy == DataTransferConfig.TRANSFER_ON_WIFI)
			{
				if (connectionData.getNetworkType() == ConnectionStateData.WIFI_CONNECTION)
				{
					dataHandler.transferStoredData();
				}
			}*/
		}
	}
}
