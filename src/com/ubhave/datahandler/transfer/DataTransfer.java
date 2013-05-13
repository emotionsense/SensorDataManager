package com.ubhave.datahandler.transfer;

import java.io.File;
import java.util.HashMap;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.ubhave.datahandler.config.DataHandlerConfig;
import com.ubhave.datahandler.config.DataStorageConfig;
import com.ubhave.datahandler.config.DataTransferConfig;
import com.ubhave.datahandler.except.DataHandlerException;

public class DataTransfer implements DataTransferInterface
{
	private final static String TAG = "DataTransfer";
	private static final String LAST_LOGS_UPLOAD_TIME = "com.ubhave.datahandler.LAST_LOGS_UPLOAD_TIME";
	private final Context context;
	private final DataHandlerConfig config;

	public DataTransfer(final Context context)
	{
		this.context = context;
		this.config = DataHandlerConfig.getInstance();

		// reset the shared preferences to app start time
		setLogsUploadTime(System.currentTimeMillis());
	}

	@Override
	public void attemptDataUpload()
	{
		try
		{
			String uploadDirectory = (String) config.get(DataStorageConfig.LOCAL_STORAGE_UPLOAD_DIRECTORY_PATH);
			File directory = new File(uploadDirectory);
			File[] files = directory.listFiles();

			for (File file : files)
			{
				HashMap<String, String> paramsMap = new HashMap<String, String>();
				paramsMap.put("password", (String) config.get(DataTransferConfig.POST_DATA_URL_PASSWD));
				String url = (String) config.get(DataTransferConfig.POST_DATA_URL);
				String response = WebConnection.postDataToServer(url, file, paramsMap);

				if (response.equals("success"))
				{
					Log.d(TAG, "file " + file + " successfully uploaded to the server");
					Log.d(TAG, "file " + file + " deleting local copy");
					file.delete();

					// update last logs upload time
					setLogsUploadTime(System.currentTimeMillis());
				}
				else
				{
					Log.d(TAG, "file " + file + " failed to upload file to the server, response received: " + response);
				}
			}

		}
		catch (DataHandlerException e)
		{
			Log.e(TAG, Log.getStackTraceString(e));
		}
	}

	private void setLogsUploadTime(long timestamp)
	{
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor prefsEditor = preferences.edit();
		prefsEditor.putLong(LAST_LOGS_UPLOAD_TIME, timestamp);
		prefsEditor.commit();
	}

	public long getLogsUploadTime()
	{
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		return preferences.getLong(LAST_LOGS_UPLOAD_TIME, 0);
	}

	@Override
	public void postData(final String data) throws DataHandlerException
	{
		String url = (String) config.get(DataTransferConfig.POST_DATA_URL);
		if (url == null)
		{
			throw new DataHandlerException(DataHandlerException.NO_URL_TARGET);
		}
		else
		{
			// TODO
			throw new DataHandlerException(DataHandlerException.UNIMPLEMENTED);
		}
	}

	@Override
	public void postError(final String error) throws DataHandlerException
	{
		String url = (String) config.get(DataTransferConfig.POST_DATA_URL);
		if (url == null)
		{
			throw new DataHandlerException(DataHandlerException.NO_URL_TARGET);
		}
		else
		{
			// TODO
			throw new DataHandlerException(DataHandlerException.UNIMPLEMENTED);
		}
	}

	@Override
	public void postExtra(final String tag, final String data) throws DataHandlerException
	{
		String url = (String) config.get(DataTransferConfig.POST_DATA_URL);
		if (url == null)
		{
			throw new DataHandlerException(DataHandlerException.NO_URL_TARGET);
		}
		else
		{
			// TODO
			throw new DataHandlerException(DataHandlerException.UNIMPLEMENTED);
		}
	}
}
