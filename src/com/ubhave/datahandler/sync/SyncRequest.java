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
			if (remoteFileLastUpdated() > localFileLastUpdated())
			{
				String requestKey = (String) config.get(FileSyncConfig.REQUEST_TYPE_PARAM_NAME);
				params.put(requestKey, (String) config.get(FileSyncConfig.REQUEST_GET_FILE_VALUE));
				String fileContents = WebConnection.postToServer(baseURL, params);
				params.remove(requestKey);

				FileOutputStream fos = context.openFileOutput(targetFile, Context.MODE_PRIVATE);
				fos.write(fileContents.getBytes());
				fos.close();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private long localFileLastUpdated()
	{
		try
		{
			File f = new File(targetFile);
			return f.lastModified();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return 0;
		}
	}
	
	private long remoteFileLastUpdated()
	{
		try
		{
			String requestKey = (String) config.get(FileSyncConfig.REQUEST_TYPE_PARAM_NAME);
			params.put(requestKey, (String) config.get(FileSyncConfig.REQUEST_DATE_MODIFIED_VALUE));
			String serverResponse = WebConnection.postToServer(baseURL, params);
			params.remove(requestKey);
			
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
