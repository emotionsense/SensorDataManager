package com.ubhave.datahandler;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.util.Log;

import com.ubhave.dataformatter.DataFormatter;
import com.ubhave.datahandler.store.DataStorage;
import com.ubhave.datahandler.transfer.DataTransfer;
import com.ubhave.datahandler.transfer.WebConnection;
import com.ubhave.sensormanager.ESException;
import com.ubhave.sensormanager.data.SensorData;
import com.ubhave.triggermanager.TriggerException;

public class DataManager
{
	private static final String TAG = "DataManager";

	private static DataManager instance;

	private final DataHandlerConfig config;
	private final DataStorage storage;
	private final DataTransfer transfer;
	private final DataHandlerEventManager eventManager;

	private final String LAST_LOGS_UPLOAD_TIME = "com.ubhave.datahandler.LAST_LOGS_UPLOAD_TIME";

	private Context context;

	private static final Object singletonLock = new Object();
	private final Object fileTransferLock = new Object();

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
		storage = new DataStorage(context);
		transfer = new DataTransfer();
		eventManager = new DataHandlerEventManager(context, this);

		// reset the shared preferences to app start time
		updateLogsUploadSharedPrefs(System.currentTimeMillis());
	}

	public void setConfig(final String key, final Object value) throws DataHandlerException
	{
		config.setConfig(key, value);
		if (key.equals(DataHandlerConfig.DATA_POLICY))
		{
			eventManager.setPolicy((Integer) value);
		}
	}

	public void moveFileToUploadDir(final File file)
	{
		// start a background thread to move files + transfer log files to the server
		new Thread()
		{
			public void run()
			{
				// move files
				synchronized (fileTransferLock)
				{
					File directory = new File(DataHandlerConfig.SERVER_UPLOAD_DIR);
					if (!directory.exists())
					{
						directory.mkdirs();
					}
					file.renameTo(new File(directory.getAbsolutePath() + "/" + file.getName()));
				}
				// transfer log files to the server
				DataManager.this.transferStoredData();
			}
		}.start();
	}
	
	private void updateLogsUploadSharedPrefs(long timestamp)
	{
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor prefsEditor = preferences.edit();
		prefsEditor.putLong(LAST_LOGS_UPLOAD_TIME, timestamp);
		prefsEditor.commit();
	}

	public void transferStoredData()
	{
		synchronized (fileTransferLock)
		{
			if (isConnectedToANetwork())
			{
				File directory = new File(DataHandlerConfig.SERVER_UPLOAD_DIR);
				File[] files = directory.listFiles();
				for (File file : files)
				{
					try
					{
						HashMap<String, String> paramsMap = new HashMap<String, String>();
						paramsMap.put("password", (String) config.get(DataHandlerConfig.DATA_POST_TARGET_URL_PASSWD));
						String url = (String) config.get(DataHandlerConfig.DATA_POST_TARGET_URL);
						String response = WebConnection.postDataToServer(url, file, paramsMap);

						if (response.equals("success"))
						{
							Log.d(TAG, "file " + file + " successfully uploaded to the server");
							Log.d(TAG, "file " + file + " deleting local copy");
							file.delete();

							// update last logs upload time
							updateLogsUploadSharedPrefs(System.currentTimeMillis());
						}
						else
						{
							Log.d(TAG, "file " + file + " failed to upload file to the server, response received: "
									+ response);
						}
					}
					catch (DataHandlerException e)
					{
						Log.e(TAG, Log.getStackTraceString(e));
					}
				}
			}
		}
	}

	public List<SensorData> getRecentSensorData(int sensorId, long startTimestamp) throws ESException, IOException
	{
		long startTime = System.currentTimeMillis();
		List<SensorData> recentData = storage.getRecentSensorData(sensorId, startTimestamp);
		long duration = System.currentTimeMillis() - startTime;

		Log.d(TAG, "getRecentSensorData() duration for processing (ms) : " + duration);

		return recentData;
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

	public void logSensorData(final SensorData data) throws DataHandlerException
	{
		if (data != null)
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

	public boolean isConnectedToANetwork()
	{
		ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo wifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		NetworkInfo mNetwork = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

		if (wifi.isConnected())
		{
			return true;
		}

		// check if no files have been transfered in the last 24 hours
		// if yes then use mobile network

		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		long lastUploadTime = preferences.getLong(LAST_LOGS_UPLOAD_TIME, 0);

		if (lastUploadTime > 0)
		{
			if ((System.currentTimeMillis() - lastUploadTime) > (long)(24 * 60 * 60 * 1000))
			{
				if (mNetwork.isConnected())
				{
					return true;
				}
			}
		}

		return false;
	}
}
