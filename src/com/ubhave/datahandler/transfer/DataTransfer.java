package com.ubhave.datahandler.transfer;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.ubhave.datahandler.config.DataHandlerConfig;
import com.ubhave.datahandler.config.DataStorageConfig;
import com.ubhave.datahandler.config.DataTransferConfig;
import com.ubhave.datahandler.except.DataHandlerException;
import com.ubhave.datahandler.http.WebConnection;

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
		setLogsUploadTime(System.currentTimeMillis());
	}
	
	@Override
	public void uploadData() throws DataHandlerException
	{
		String uploadDirectory = (String) config.get(DataStorageConfig.LOCAL_STORAGE_UPLOAD_DIRECTORY_PATH);
		File directory = new File(uploadDirectory);
		File[] files = directory.listFiles();
		if (files != null)
		{
			for (File file : files)
			{
				HashMap<String, String> paramsMap = getPostParams();
				String url = (String) config.get(DataTransferConfig.POST_DATA_URL);
				String response = WebConnection.postDataToServer(url, file, paramsMap);

				if (response.equals(config.get(DataTransferConfig.POST_RESPONSE_ON_SUCCESS)))
				{
					if (DataHandlerConfig.shouldLog())
					{
						Log.d(TAG, "file " + file + " successfully uploaded to the server");
						Log.d(TAG, "file " + file + " deleting local copy");
					}
					file.delete();
					setLogsUploadTime(System.currentTimeMillis());
				}
				else 
				{
					if (DataHandlerConfig.shouldLog())
					{
						Log.d(TAG, "file " + file + " failed to upload file to the server, response received: " + response);
					}
					throw new DataHandlerException(DataHandlerException.POST_FAILED);
				}
			}
		}
	}

	@Override
	public void attemptDataUpload()
	{
		try
		{
			uploadData();
		}
		catch (Exception e)
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
	
	private HashMap<String, String> getPostParams()
	{
		HashMap<String, String> paramsMap = new HashMap<String, String>();
		if (config.containsConfig(DataTransferConfig.POST_PARAMETERS))
		{
			try
			{
				JSONObject json = (JSONObject) config.get(DataTransferConfig.POST_PARAMETERS);
				Iterator<?> keyIterator = json.keys();
				while (keyIterator.hasNext())
				{
					try
					{
						String key = (String) keyIterator.next();
						String value = json.getString(key);
						paramsMap.put(key, value);
					}
					catch (JSONException e)
					{
						e.printStackTrace();
					}
				}
			}
			catch (DataHandlerException e)
			{
				e.printStackTrace();
			}
		}
		return paramsMap;
	}
	
	private void post(final String url, final String data) throws DataHandlerException
	{
		try
		{
			String dataKey = (String) config.get(DataTransferConfig.POST_RAW_DATA_KEY);
			JSONObject dataParam = new JSONObject();
			dataParam.put(dataKey, data);
			
			HashMap<String, String> paramsMap = getPostParams();
			paramsMap.put(dataKey, data);
			
			String response = WebConnection.postToServer(url, paramsMap);
			String expectedResponse = (String) config.get(DataTransferConfig.POST_RESPONSE_ON_SUCCESS); 
			if (!response.equals(expectedResponse))
			{
				throw new DataHandlerException(DataHandlerException.POST_FAILED);
			}
		}
		catch (JSONException e)
		{
			e.printStackTrace();
			throw new DataHandlerException(DataHandlerException.POST_FAILED);
		}
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
			post(url, data);
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
			post(url, error);
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
			post(url, data);
		}
	}
}
