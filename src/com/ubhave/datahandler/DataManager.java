package com.ubhave.datahandler;

import java.io.IOException;
import java.util.List;

import android.content.Context;
import android.util.Log;

import com.ubhave.dataformatter.DataFormatter;
import com.ubhave.datahandler.config.DataHandlerConfig;
import com.ubhave.datahandler.config.DataStorageConfig;
import com.ubhave.datahandler.config.DataTransferConfig;
import com.ubhave.datahandler.store.DataStorage;
import com.ubhave.datahandler.store.DataStorageInterface;
import com.ubhave.datahandler.transfer.DataTransfer;
import com.ubhave.datahandler.transfer.DataTransferInterface;
import com.ubhave.sensormanager.ESException;
import com.ubhave.sensormanager.data.SensorData;
import com.ubhave.triggermanager.TriggerException;

public class DataManager implements DataManagerInterface
{
	private static final String TAG = "DataManager";
	private static final Object singletonLock = new Object();
	private static DataManager instance;

	private static final Object fileTransferLock = new Object();

	private final Context context;
	private final DataHandlerConfig config;
	private final DataStorageInterface storage;
	private final DataTransferInterface transfer;
	private final DataHandlerEventManager eventManager;

	public static DataManager getInstance(final Context context) throws ESException, TriggerException
	{
		if (instance == null)
		{
			synchronized (singletonLock)
			{
				if (instance == null)
				{
					instance = new DataManager(context);
				}
			}
		}
		return instance;
	}

	private DataManager(final Context context) throws ESException, TriggerException
	{
		this.context = context;
		config = DataHandlerConfig.getInstance();
		storage = new DataStorage(context, fileTransferLock);
		transfer = new DataTransfer(context);
		eventManager = new DataHandlerEventManager(context, this);
	}

	@Override
	public void setConfig(final String key, final Object value) throws DataHandlerException
	{
		config.setConfig(key, value);
		if (key.equals(DataTransferConfig.DATA_TRANSER_POLICY))
		{
			eventManager.setPolicy((Integer) value);
		}
	}

	@Override
	public List<SensorData> getRecentSensorData(int sensorId, long startTimestamp) throws ESException, IOException
	{
		long startTime = System.currentTimeMillis();
		List<SensorData> recentData = storage.getRecentSensorData(sensorId, startTimestamp);
		long duration = System.currentTimeMillis() - startTime;

		Log.d(TAG, "getRecentSensorData() duration for processing (ms) : " + duration);

		return recentData;
	}

	private boolean shouldTransferImmediately()
	{
		try
		{
			return ((Integer) config.get(DataTransferConfig.DATA_TRANSER_POLICY)) == DataTransferConfig.TRANSFER_IMMEDIATE;
		}
		catch (DataHandlerException e)
		{
			e.printStackTrace();
			return false;
		}
	}

	private DataFormatter getDataFormatter(int sensorType)
	{
		DataFormatter formatter = null;
		try
		{
			if (((Integer) config.get(DataStorageConfig.LOCAL_STORAGE_DATA_FORMAT)) == DataStorageConfig.JSON_FORMAT)
			{
				formatter = DataFormatter.getJSONFormatter(context, sensorType);
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

	@Override
	public void logSensorData(final SensorData data) throws DataHandlerException
	{
		if (data != null)
		{
			DataFormatter formatter = getDataFormatter(data.getSensorType());
			if (shouldTransferImmediately())
			{
				transfer.postData(formatter.toString(data));
			}
			else
			{
				storage.logSensorData(data, formatter);
			}
		}
	}

	@Override
	public void logError(final String error) throws DataHandlerException
	{
		if (shouldTransferImmediately())
		{
			transfer.postError(error);
		}
		else
		{
			storage.logError(error);
		}
	}

	@Override
	public void logExtra(final String tag, final String data) throws DataHandlerException
	{
		if (shouldTransferImmediately())
		{
			transfer.postExtra(tag, data);
		}
		else
		{
			storage.logExtra(tag, data);
		}
	}

	@Override
	public void transferStoredData()
	{
		storage.moveArchivedFilesForUpload();
		synchronized (fileTransferLock)
		{
			transfer.attemptDataUpload();
		}
	}
}
