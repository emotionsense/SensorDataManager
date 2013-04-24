package com.ubhave.datahandler.sync;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import android.content.Context;

import com.ubhave.datahandler.config.DataHandlerConfig;
import com.ubhave.datahandler.config.FileSyncConfig;
import com.ubhave.http.WebConnection;

public class SyncRequest
{
	private final Context context;
	private final DataHandlerConfig config;
	
	private final String baseURL;
	private final String targetFile;
	private HashMap<String, String> params;
	private long syncInterval;

	public SyncRequest(Context context, String url, String file)
	{
		this.context = context;
		this.config = DataHandlerConfig.getInstance();
		
		this.targetFile = file;
		this.baseURL = url;
		this.syncInterval = FileSyncConfig.DEFAULT_SYNC_FREQUENCY;
		this.params = new HashMap<String, String>();
	}

	public void setSyncInterval(long interval)
	{
		this.syncInterval = interval;
	}

	public long getSyncInterval()
	{
		return syncInterval;
	}

	public String getBaseURL()
	{
		return baseURL;
	}

	public void setParam(String key, String value)
	{
		params.put(key, value);
	}

	public HashMap<String, String> getParams()
	{
		return params;
	}

	public void start()
	{
		// TODO
	}
	
	public void stop()
	{
		// TODO
	}

	public void sync()
	{
		try
		{
			String localFilePath = "null"; // TODO
			String remoteURL = "null"; // TODO

			String requestKey = (String) config.get(FileSyncConfig.REQUEST_TYPE_PARAM_NAME);

			HashMap<String, String> params = getRequestParams();
			params.put(requestKey, (String) config.get(FileSyncConfig.REQUEST_DATE_MODIFIED_VALUE));
			String response = WebConnection.postToServer(remoteURL, params);

			if (remoteFileLastUpdated(response) > localFileLastUpdated(localFilePath))
			{
				params.put(requestKey, (String) config.get(FileSyncConfig.REQUEST_GET_FILE_VALUE));
				String fileContents = WebConnection.postToServer(remoteURL, params);

				FileOutputStream fos = context.openFileOutput(localFilePath, Context.MODE_PRIVATE);
				fos.write(fileContents.getBytes());
				fos.close();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private HashMap<String, String> getRequestParams()
	{
		// TODO add file-specific params
		return new HashMap<String, String>();
	}
	
	private long localFileLastUpdated(final String filePath)
	{
		try
		{
			File f = new File(filePath);
			return f.lastModified();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return 0;
		}
	}
	
	private long remoteFileLastUpdated(final String serverResponse)
	{
		try
		{
			// TODO this is wrong
			String responseKey = (String) config.get(FileSyncConfig.RESPONSE_DATE_MODIFIED_KEY);
			JSONParser parser = new JSONParser();
			JSONObject response = (JSONObject) parser.parse(serverResponse);
			long date = (Long) response.get(responseKey);
			return date;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return 0;
		}
	}
}
