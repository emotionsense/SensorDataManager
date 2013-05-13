package com.ubhave.datahandler;

import java.io.IOException;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.ubhave.dataformatter.DataFormatter;
import com.ubhave.datahandler.alarm.AlarmListener;
import com.ubhave.datahandler.alarm.PolicyAlarm;
import com.ubhave.datahandler.config.DataHandlerConfig;
import com.ubhave.datahandler.config.DataTransferConfig;
import com.ubhave.datahandler.except.DataHandlerException;
import com.ubhave.datahandler.store.DataStorage;
import com.ubhave.datahandler.store.DataStorageInterface;
import com.ubhave.datahandler.sync.FileSyncInterface;
import com.ubhave.datahandler.sync.FileSynchronizer;
import com.ubhave.datahandler.sync.FileUpdatedListener;
import com.ubhave.datahandler.sync.SyncRequest;
import com.ubhave.datahandler.transfer.DataTransfer;
import com.ubhave.datahandler.transfer.DataTransferInterface;
import com.ubhave.sensormanager.ESException;
import com.ubhave.sensormanager.data.SensorData;

public class ESDataManager implements ESDataManagerInterface
{
	private static final String TAG = "DataManager";
	private static final Object singletonLock = new Object();
	private static ESDataManager instance;

	private static final Object fileTransferLock = new Object();

	private final Context context;
	private final DataHandlerConfig config;
	private final DataStorageInterface storage;
	private final DataTransferInterface transfer;
	private final FileSyncInterface fileSync;

	// TODO extract to appropriate config file
	public final static String ACTION_NAME_SYNC_REQUEST_ALARM = "com.ubhave.datahandler.sync.SYNC_REQUEST_ALARM";
	public final static String ACTION_NAME_DATA_TRANSFER_ALARM = "com.ubhave.datahandler.sync.DATA_TRANSFER_ALARM";

	// TODO extract to appropriate config file
	public final static int REQUEST_CODE_SYNC_REQUEST = 8950;
	public final static int REQUEST_CODE_DATA_TRANSFER = 8951;

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
		fileSync = new FileSynchronizer(context);

		setupAlarmForTransfer();
	}

	private void setupAlarmForTransfer() throws DataHandlerException
	{
		int transferPolicy = (Integer) config.get(DataTransferConfig.DATA_TRANSER_POLICY);

		if (transferPolicy == DataTransferConfig.TRANSFER_PERIODICALLY)
		{
			AlarmListener alarmListener = new AlarmListener()
			{
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
							ESDataManager.this.transferStoredData();
						}
					}.start();
				}
			};

			Intent intent = new Intent(ACTION_NAME_DATA_TRANSFER_ALARM);
			PolicyAlarm policyAlarm = new PolicyAlarm("dataTransferAlarm", context, intent, REQUEST_CODE_DATA_TRANSFER,
					ACTION_NAME_DATA_TRANSFER_ALARM);
			policyAlarm.setListener(alarmListener);
			policyAlarm.start();
		}
	}

	@Override
	public void setConfig(final String key, final Object value) throws DataHandlerException
	{
		config.setConfig(key, value);
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
		DataFormatter formatter = DataFormatter.getJSONFormatter(context, sensorType);
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

	@Override
	public int subscribeToRemoteFileUpdate(final String url, final String targetFile, FileUpdatedListener listener)
			throws DataHandlerException
	{
		return fileSync.subscribeToRemoteFileUpdate(url, targetFile, listener);
	}

	@Override
	public int subscribeToRemoteFileUpdate(final SyncRequest request, FileUpdatedListener listener)
			throws DataHandlerException
	{
		return fileSync.subscribeToRemoteFileUpdate(request, listener);
	}

	@Override
	public void unsubscribeFromRemoteFileUpdate(final int key) throws DataHandlerException
	{
		fileSync.unsubscribeFromRemoteFileUpdate(key);
	}

	@Override
	public void attemptFileSync()
	{
		fileSync.syncUpdatedFiles();
	}
}
