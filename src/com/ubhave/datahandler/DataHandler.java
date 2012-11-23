package com.ubhave.datahandler;

import android.content.Context;

import com.ubhave.dataformatter.DataFormatter;
import com.ubhave.datahandler.store.DataStorage;
import com.ubhave.datahandler.transfer.DataTransfer;
import com.ubhave.sensormanager.ESException;
import com.ubhave.sensormanager.data.SensorData;
import com.ubhave.triggermanager.TriggerException;

public class DataHandler
{
	private static DataHandler instance;

	private final DataHandlerConfig config;
	private final DataStorage storage;
	private final DataTransfer transfer;
	private final DataHandlerEventManager eventManager;

	public static DataHandler getInstance(final Context context) throws ESException, TriggerException
	{
		if (instance == null)
		{
			instance = new DataHandler(context);
		}
		return instance;
	}

	private DataHandler(final Context context) throws ESException, TriggerException
	{
		config = DataHandlerConfig.getInstance();
		storage = new DataStorage();
		transfer = new DataTransfer();
		eventManager = new DataHandlerEventManager(context, this);
	}

	public void setConfig(final String key, final Object value) throws DataHandlerException
	{
		config.setConfig(key, value);
		if (key.equals(DataHandlerConfig.DATA_POLICY))
		{
			eventManager.setPolicy((Integer) value);
		}
	}

	public void transferStoredData()
	{
		// TODO
	}

	private boolean transferImmediately()
	{
		try
		{
			return ((Integer) config.get(DataHandlerConfig.DATA_POLICY)) == DataHandlerConfig.TRANSFER_IMMEDIATE;
		}
		catch (DataHandlerException e)
		{
			e.printStackTrace();
			return true;
		}
	}

	private DataFormatter getDataFormatter(int sensorType)
	{
		DataFormatter formatter = null;
		try
		{
			if (((Integer) config.get(DataHandlerConfig.DATA_FORMAT)) == DataHandlerConfig.JSON_FORMAT)
			{
				formatter = DataFormatter.getJSONFormatter(sensorType);
			}
			else
			{
				formatter = DataFormatter.getCSVFormatter(sensorType);
			}
		}
		catch (DataHandlerException e)
		{
			e.printStackTrace();
		}
		return formatter;
	}

	public void logSensorData(final SensorData data) throws DataHandlerException
	{
		DataFormatter formatter = getDataFormatter(data.getSensorType());
		if (transferImmediately())
		{
			transfer.postData(formatter.toString(data), (String) config.get(DataHandlerConfig.DATA_POST_TARGET_URL));
		}
		else
		{
			storage.logSensorData(data, formatter);
		}
	}

	public void logError(final String error) throws DataHandlerException
	{
		if (transferImmediately())
		{
			transfer.postError(error, (String) config.get(DataHandlerConfig.ERROR_POST_TARGET_URL));
		}
		else
		{
			storage.logError(error);
		}
	}

	public void logExtra(final String tag, final String data) throws DataHandlerException
	{
		if (transferImmediately())
		{
			transfer.postExtra(tag, data, (String) config.get(DataHandlerConfig.EXTRA_POST_TARGET_URL));
		}
		else
		{
			storage.logExtra(tag, data);
		}
	}
}
