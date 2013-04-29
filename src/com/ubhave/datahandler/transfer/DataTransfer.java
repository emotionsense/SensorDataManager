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
import com.ubhave.http.WebConnection;

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
		// this method uploads data to the server
		// if the conditions for the transfer are met: connection type, 24 hour
		// timeout if using 3g connection

		// int connectionType =
		// DataTransferConfig.DEFAULT_CONNECTION_TYPE_FOR_TRANSFER;
		// try
		// {
		// connectionType = (Integer)
		// config.get(DataTransferConfig.CONNECTION_TYPE_FOR_TRANSFER);
		// }
		// catch (DataHandlerException e)
		// {
		// e.printStackTrace();
		// }
		//
		// // any network
		// if (((connectionType == DataTransferConfig.CONNECTION_TYPE_ANY) &&
		// (isConnectedToAnyNetwork()))
		// // use only wifi
		// || ((connectionType == DataTransferConfig.CONNECTION_TYPE_WIFI) &&
		// (isConnectedToWiFi()))
		// // use only wifi but if it's been more than 24 hours from the last
		// // upload time then use any available n/w
		// || ((connectionType == DataTransferConfig.CONNECTION_TYPE_WIFI) &&
		// (isLastUploadTimeoutReached()) && (isConnectedToAnyNetwork())))
		// {
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
		// }
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

	// private boolean isConnectedToWiFi()
	// {
	// ConnectivityManager connManager = (ConnectivityManager)
	// context.getSystemService(Context.CONNECTIVITY_SERVICE);
	// NetworkInfo wifi =
	// connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
	//
	// if (wifi.isConnected())
	// {
	// return true;
	// }
	//
	// return false;
	// }
	//
	// private boolean isConnectedToAnyNetwork()
	// {
	// ConnectivityManager connManager = (ConnectivityManager)
	// context.getSystemService(Context.CONNECTIVITY_SERVICE);
	// NetworkInfo wifi =
	// connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
	// NetworkInfo mNetwork =
	// connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
	//
	// if (wifi.isConnected())
	// {
	// return true;
	// }
	//
	// if (mNetwork.isConnected())
	// {
	// return true;
	// }
	//
	// return false;
	// }
	//
	// private boolean isLastUploadTimeoutReached()
	// {
	// // check if no files have been transfered in the last 24 hours
	//
	// long lastUploadTime = getLogsUploadTime();
	//
	// if (lastUploadTime > 0)
	// {
	// if ((System.currentTimeMillis() - lastUploadTime) > (long) (24 * 60 * 60
	// * 1000))
	// {
	// return true;
	// }
	// }
	//
	// return false;
	// }

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
