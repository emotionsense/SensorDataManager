package com.ubhave.datahandler;

import android.content.Context;
import android.util.Log;

import com.ubhave.dataformatter.DataFormatter;
import com.ubhave.datahandler.config.DataHandlerConfig;
import com.ubhave.datahandler.config.DataStorageConfig;
import com.ubhave.datahandler.config.DataTransferConfig;
import com.ubhave.datahandler.except.DataHandlerException;
import com.ubhave.datahandler.transfer.DataTransfer;
import com.ubhave.datahandler.transfer.DataTransferInterface;
import com.ubhave.datahandler.transfer.DataUploadCallback;
import com.ubhave.datastore.DataStorageInterface;
import com.ubhave.datastore.db.DatabaseManager;
import com.ubhave.datastore.file.FileStoreManager;
import com.ubhave.sensormanager.ESException;
import com.ubhave.sensormanager.data.SensorData;

import java.io.IOException;
import java.util.List;

public abstract class ESDataManager implements ESDataManagerInterface
{
	private static ESDataManager instance;
	
	protected static final String TAG = "DataManager";
	protected static final Object fileTransferLock = new Object();
	protected static final Object singletonLock = new Object();
	
	protected final Context context;
	protected final DataStorageInterface storage;
	protected final DataTransferInterface transfer;
	protected DataTransferAlarmListener dataTransferAlarmListener;
	
	public static ESDataManager getInstance(final Context context, final int storageType, final String dataPassword) throws ESException, DataHandlerException
	{
		if (instance == null)
		{
			synchronized (singletonLock)
			{
				if (instance == null)
				{
					if (storageType == DataStorageConfig.STORAGE_TYPE_FILES)
					{
						instance = new FileStoreManager(context, dataPassword);
					}
					else if (storageType == DataStorageConfig.STORAGE_TYPE_DB)
					{
						instance = new DatabaseManager(context, dataPassword); 
					}
					else 
					{
						throw new DataHandlerException(DataHandlerException.UNKNOWN_CONFIG);
					}
				}
			}
		}
		else if (instance.getStorageType() != storageType)
		{
			throw new DataHandlerException(DataHandlerException.CONFIG_CONFLICT);
		}
		return instance;
	}

	protected ESDataManager(final Context context, final String dataPassword) throws ESException, DataHandlerException
	{
		this.context = context;
		this.transfer = new DataTransfer(context, dataPassword);
		this.storage = getStorage(dataPassword);
		setupAlarmForTransfer();
	}
	
	protected abstract DataStorageInterface getStorage(final String dataPassword) throws DataHandlerException;
	
	protected abstract int getStorageType();

	private void setupAlarmForTransfer() throws DataHandlerException
	{
		if (dataTransferAlarmListener != null)
		{
			dataTransferAlarmListener.stop();
			dataTransferAlarmListener = null;
		}
		
		int transferPolicy = (Integer) DataHandlerConfig.getInstance().get(DataTransferConfig.DATA_TRANSER_POLICY);
		if (DataHandlerConfig.shouldLog())
		{
			Log.d(DataTransfer.TAG, "Updating transfer alarm to: "+transferPolicy);
		}
		
		if (transferPolicy == DataTransferConfig.TRANSFER_PERIODICALLY)
		{
			dataTransferAlarmListener = new DataTransferAlarmListener(context, this);
			dataTransferAlarmListener.start();
		}
	}

	@Override
	public void setConfig(final String key, final Object value) throws DataHandlerException
	{
		DataHandlerConfig config = DataHandlerConfig.getInstance();
		if (key.equals(DataTransferConfig.DATA_TRANSER_POLICY))
		{
			int currentPolicy = (Integer) config.get(DataTransferConfig.DATA_TRANSER_POLICY);
			if ((Integer) value != currentPolicy)
			{
				if (DataHandlerConfig.shouldLog())
				{
					Log.d(DataTransfer.TAG, "Updated data transfer policy to: "+value);
				}
				config.setConfig(key, value);
				setupAlarmForTransfer();
			}
		}
		else
		{
			config.setConfig(key, value);
			if (key.equals(DataTransferConfig.TRANSFER_ALARM_INTERVAL))
			{
				if (dataTransferAlarmListener != null)
				{
					if (DataHandlerConfig.shouldLog())
					{
						Log.d(DataTransfer.TAG, "Updated alarm interval to: "+value);
					}
					dataTransferAlarmListener.configUpdated();
				}
			}
		}
	}
	
	@Override
	public Object getConfig(final String key) throws DataHandlerException
	{
		DataHandlerConfig config = DataHandlerConfig.getInstance();
		return config.get(key);
	}

	@Override
	public List<SensorData> getRecentSensorData(final int sensorId, final long startTimestamp) throws DataHandlerException, ESException, IOException
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
	
	@Override
	public void logSensorData(final SensorData data, final DataFormatter formatter) throws DataHandlerException
	{
		if (data != null)
		{
			storage.logSensorData(data, formatter);
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
		storage.logExtra(tag, data);
	}
	
	public void transferStoredData()
	{
		try
		{
			if (DataHandlerConfig.shouldLog())
			{
				Log.d(TAG, "transferStoredData()");
			}
			
			boolean hasDataToUpload = storage.prepareDataForUpload();
			if (hasDataToUpload)
			{
				synchronized (fileTransferLock)
				{
					transfer.uploadData(new DataUploadCallback[]{storage});
				}
			}
		}
		catch (DataHandlerException e)
		{
			if (DataHandlerConfig.shouldLog())
			{
				Log.e(TAG, e.getLocalizedMessage());
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void postAllStoredData(final DataUploadCallback callback) throws DataHandlerException
	{
        Log.d("DataManager", "Called exit method.");
		DataHandlerConfig config = DataHandlerConfig.getInstance();
		if ((Integer) config.get(DataTransferConfig.DATA_TRANSER_POLICY) != DataTransferConfig.STORE_ONLY)
		{
			if (DataHandlerConfig.shouldLog())
			{
				Log.d(TAG, "transferStoredData()");
			}
			
			final long currentFileLife = (Long) config.get(DataStorageConfig.DATA_LIFE_MILLIS);
			config.setConfig(DataStorageConfig.DATA_LIFE_MILLIS, 0L);
			
			if (DataHandlerConfig.shouldLog())
			{
				Log.d(TAG, "Set data life to: "+(Long) config.get(DataStorageConfig.DATA_LIFE_MILLIS));
			}

			boolean hasDataToUpload = storage.prepareDataForUpload();
			if (hasDataToUpload)
			{
				synchronized (fileTransferLock)
				{
					transfer.uploadData(new DataUploadCallback[]{storage, callback});
				}
			}
            else
            {
                if (DataHandlerConfig.shouldLog())
                {
                    Log.d(TAG, "Nothing to upload.");
                }
                callback.onDataUploaded();
            }
			
			config.setConfig(DataStorageConfig.DATA_LIFE_MILLIS, currentFileLife);
			
			if (DataHandlerConfig.shouldLog())
			{
				Log.d(TAG, "Set data life to: "+(Long) config.get(DataStorageConfig.DATA_LIFE_MILLIS));
			}
		}
		else if (DataHandlerConfig.shouldLog())
		{
			Log.d(TAG, "Transfer policy is store-only: nothing to do.");
		}
	}
}
