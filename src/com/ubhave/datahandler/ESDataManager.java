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
import com.ubhave.datahandler.transfer.DataTransfer;
import com.ubhave.datahandler.transfer.DataTransferInterface;
import com.ubhave.datastore.DataStorageInterface;
import com.ubhave.datastore.db.DatabaseManager;
import com.ubhave.datastore.file.FileStoreManager;
import com.ubhave.datastore.none.NoStorageManager;
import com.ubhave.sensormanager.ESException;
import com.ubhave.sensormanager.data.SensorData;

public abstract class ESDataManager implements ESDataManagerInterface
{
	protected static final String TAG = "DataManager";
	protected static final Object fileTransferLock = new Object();
	private static final Object singletonLock = new Object();
	
	private static ESDataManager noStorageInstance;
	private static ESDataManager fileStorageInstance;
	private static ESDataManager databaseStorageInstance;

	protected final Context context;
	protected final DataHandlerConfig config;
	protected final DataStorageInterface storage;
	protected final DataTransferInterface transfer;
	protected DataTransferAlarmListener dataTransferAlarmListener;
	
	public static ESDataManager getInstance(final Context context, int storageType) throws ESException, DataHandlerException
	{
		if (storageType == DataStorageConfig.STORAGE_TYPE_FILES)
		{
			if (fileStorageInstance == null)
			{
				synchronized (singletonLock)
				{
					if (fileStorageInstance == null)
					{
						fileStorageInstance = new FileStoreManager(context);
					}
				}
			}
			return fileStorageInstance;
		}
		else if (storageType == DataStorageConfig.STORAGE_TYPE_DB)
		{
			if (databaseStorageInstance == null)
			{
				synchronized (singletonLock)
				{
					if (databaseStorageInstance == null)
					{
						databaseStorageInstance = new DatabaseManager(context);
					}
				}
			}
			return databaseStorageInstance;
		}
		else if (storageType == DataStorageConfig.STORAGE_TYPE_NONE)
		{
			if (noStorageInstance == null)
			{
				synchronized (singletonLock)
				{
					if (noStorageInstance == null)
					{
						noStorageInstance = new NoStorageManager(context);
					}
				}
			}
			return noStorageInstance;
		}
		else 
		{
			throw new DataHandlerException(DataHandlerException.UNKNOWN_CONFIG);
		}	
	}

	protected ESDataManager(final Context context) throws ESException, DataHandlerException
	{
		this.context = context;
		config = DataHandlerConfig.getInstance();
		transfer = new DataTransfer(context);
		storage = getStorage();
		if (storage != null)
		{
			setupAlarmForTransfer();
		}
	}
	
	protected abstract DataStorageInterface getStorage();

	private void setupAlarmForTransfer() throws DataHandlerException
	{
		int transferPolicy = (Integer) config.get(DataTransferConfig.DATA_TRANSER_POLICY);
		if (transferPolicy == DataTransferConfig.TRANSFER_PERIODICALLY)
		{
			int connectionType = (Integer) config.get(DataTransferConfig.CONNECTION_TYPE_FOR_TRANSFER);
			dataTransferAlarmListener = new DataTransferAlarmListener(context, this);
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
			else if (dataTransferAlarmListener != null)
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
			if (dataTransferAlarmListener != null)
			{
				dataTransferAlarmListener.configUpdated();
			}
		}
	}
	
	@Override
	public Object getConfig(final String key) throws DataHandlerException
	{
		return config.get(key);
	}

	@Override
	public List<SensorData> getRecentSensorData(int sensorId, long startTimestamp) throws DataHandlerException, ESException, IOException
	{
		if (storage != null)
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
		throw new DataHandlerException(DataHandlerException.NO_STORAGE);
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
			else if (storage != null)
			{
				storage.logSensorData(data, formatter);
			}
			else
			{
				throw new DataHandlerException(DataHandlerException.NO_STORAGE);
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
	public void logExtra(final String tag, final String data) throws DataHandlerException
	{
		if (shouldTransferImmediately())
		{
			transfer.postExtra(tag, data);
		}
		else if (storage != null)
		{
			storage.logExtra(tag, data);
		}
		else
		{
			throw new DataHandlerException(DataHandlerException.NO_STORAGE);
		}
	}
	
	@Override
	public boolean transferStoredData()
	{
		try
		{
			if (DataHandlerConfig.shouldLog())
			{
				Log.d(TAG, "transferStoredData()");
			}
			
			final String uploadDirectory = storage.prepareDataForUpload();
			if (uploadDirectory != null)
			{
				synchronized (fileTransferLock)
				{
					transfer.uploadData(uploadDirectory);
					storage.onDataUploaded();
				}
			}
			return true;
		}
		catch (DataHandlerException e)
		{
			if (DataHandlerConfig.shouldLog())
			{
				Log.e(TAG, e.getLocalizedMessage());
				e.printStackTrace();
			}
			return false;
		}
	}
	
	@Override
	public void postAllStoredData() throws DataHandlerException
	{
		if ((Integer) config.get(DataTransferConfig.DATA_TRANSER_POLICY) != DataTransferConfig.STORE_ONLY)
		{
			final long currentFileLife = (Long) config.get(DataStorageConfig.DATA_LIFE_MILLIS);
			config.setConfig(DataStorageConfig.DATA_LIFE_MILLIS, -1L);
			boolean dataUploaded = transferStoredData();
			config.setConfig(DataStorageConfig.DATA_LIFE_MILLIS, currentFileLife);
			if (!dataUploaded)
			{
				throw new DataHandlerException(DataHandlerException.POST_FAILED);
			}
		}
		else
		{
			if (DataHandlerConfig.shouldLog())
			{
				Log.d(TAG, "Transfer policy is store-only: nothing to do.");
			}
		}
	}
}
