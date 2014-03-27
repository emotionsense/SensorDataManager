package com.ubhave.datahandler;

import java.io.IOException;
import java.util.List;

import android.content.Context;
import android.util.Log;

import com.ubhave.dataformatter.DataFormatter;
import com.ubhave.datahandler.config.DataHandlerConfig;
import com.ubhave.datahandler.config.DataStorageConfig;
import com.ubhave.datahandler.config.DataTransferConfig;
import com.ubhave.datahandler.except.DataHandlerException;
import com.ubhave.datahandler.store.DataStorage;
import com.ubhave.datahandler.store.DataStorageInterface;
import com.ubhave.datahandler.transfer.DataTransfer;
import com.ubhave.datahandler.transfer.DataTransferInterface;
import com.ubhave.sensormanager.ESException;
import com.ubhave.sensormanager.data.SensorData;

public class ESDataManager implements ESDataManagerInterface
{
	private static final String TAG = "DataManager";
	private static final Object singletonLock = new Object();
	private static final Object fileTransferLock = new Object();
	private static ESDataManager instance;

	private final Context context;
	private final DataHandlerConfig config;
	private final DataStorageInterface storage;
	private final DataTransferInterface transfer;
	private final DataTransferAlarmListener dataTransferAlarmListener;

	public static ESDataManager getInstance(final Context context) throws ESException, DataHandlerException
	{
		if (instance == null)
		{
			synchronized (singletonLock)
			{
				if (instance == null)
				{
					instance = new ESDataManager(context);
				}
			}
		}
		return instance;
	}

	private ESDataManager(final Context context) throws ESException, DataHandlerException
	{
		this.context = context;
		config = DataHandlerConfig.getInstance();
		storage = new DataStorage(context, fileTransferLock);
		transfer = new DataTransfer(context);
		
		dataTransferAlarmListener = new DataTransferAlarmListener(context, this);
		setupAlarmForTransfer();
	}

	private void setupAlarmForTransfer() throws DataHandlerException
	{
		int transferPolicy = (Integer) config.get(DataTransferConfig.DATA_TRANSER_POLICY);
		if (transferPolicy == DataTransferConfig.TRANSFER_PERIODICALLY)
		{
			int connectionType = (Integer) config.get(DataTransferConfig.CONNECTION_TYPE_FOR_TRANSFER);
			dataTransferAlarmListener.setConnectionTypeAndStart(connectionType);
		}
	}

	@Override
	public void setConfig(final String key, final Object value) throws DataHandlerException
	{
		config.setConfig(key, value);
		if (key.equals(DataTransferConfig.DATA_TRANSER_POLICY))
		{
			if (((Integer) value) == DataTransferConfig.TRANSFER_PERIODICALLY)
			{
				setupAlarmForTransfer();
			}
			else
			{
				if (DataHandlerConfig.shouldLog())
				{
					Log.d("PolicyAlarm", "===== Stopping Policy Alarm ====");
				}
				dataTransferAlarmListener.stop();
			}
		}
		else if (key.equals(DataTransferConfig.TRANSFER_ALARM_INTERVAL))
		{
			dataTransferAlarmListener.configUpdated();
		}
	}
	
	@Override
	public Object getConfig(final String key) throws DataHandlerException
	{
		return config.get(key);
	}

	@Override
	public List<SensorData> getRecentSensorData(int sensorId, long startTimestamp) throws ESException, IOException
	{
		long startTime = System.currentTimeMillis();
		List<SensorData> recentData = storage.getRecentSensorData(sensorId, startTimestamp);
		long duration = System.currentTimeMillis() - startTime;

		if (DataHandlerConfig.shouldLog())
		{
			Log.d(TAG, "getRecentSensorData() duration for processing (ms) : " + duration);
		}
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
	
	@Override
	public void logSensorData(final SensorData data, DataFormatter formatter) throws DataHandlerException
	{
		if (data != null)
		{
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
	public void logSensorData(final SensorData data) throws DataHandlerException
	{
		if (data != null)
		{
			DataFormatter formatter = DataFormatter.getJSONFormatter(context, data.getSensorType());
			logSensorData(data, formatter);
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
	
	@Override
	public void postAllStoredData() throws DataHandlerException
	{
		if ((Integer) config.get(DataTransferConfig.DATA_TRANSER_POLICY) != DataTransferConfig.STORE_ONLY)
		{
			long currentFileLife = (Long) config.get(DataStorageConfig.FILE_LIFE_MILLIS);
			config.setConfig(DataStorageConfig.FILE_LIFE_MILLIS, -1L);
			storage.moveArchivedFilesForUpload();
			synchronized (fileTransferLock)
			{
				transfer.uploadData();
			}
			config.setConfig(DataStorageConfig.FILE_LIFE_MILLIS, currentFileLife);
		}
	}
}
