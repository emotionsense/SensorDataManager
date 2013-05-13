package com.ubhave.datahandler.sync;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.ubhave.http.WebConnection;

public class SyncTask extends AsyncTask<Void, Void, Void>
{
	private final static String LOG_TAG = "SyncTask";
	
	private Context context;
	private FileUpdatedListener listener;
	private String baseURL, targetFile;
	private HashMap<String, String> params;
	private String requestTypeKey, dateParamValue, fileParamValue;
	private String dateResponseFieldKey;
	
	public void setListener(FileUpdatedListener listener)
	{
		this.listener = listener;
	}
	
	public void setBaseURL(String url)
	{
		this.baseURL = url;
	}
	
	public void setTargetFile(String file)
	{
		this.targetFile = file;
	}
	
	public void setContext(Context context)
	{
		this.context = context;
	}
	
	public void setParams(HashMap<String, String> params)
	{
		this.params = params;
	}
	
	public void setRequestTypeKey(String key)
	{
		requestTypeKey = key;
	}
	
	public void setGetDateValue(String value)
	{
		dateParamValue = value;
	}
	
	public void setGetFileValue(String value)
	{
		fileParamValue = value;
	}
	
	public void setDateResponseKey(String value)
	{
		dateResponseFieldKey = value;
	}

	@Override
	protected Void doInBackground(Void... ps)
	{
		try
		{
			Log.d(LOG_TAG, "Sync attempt: "+baseURL);
			if (remoteFileLastUpdated() > localFileLastUpdated())
			{
				Log.d(LOG_TAG, "Downloading from: "+baseURL);
				params.put(requestTypeKey, fileParamValue);
				String fileContents = WebConnection.postToServer(baseURL, params);
				params.remove(requestTypeKey);

				FileOutputStream fos = context.openFileOutput(targetFile, Context.MODE_PRIVATE);
				fos.write(fileContents.getBytes());
				fos.close();
				
				if (listener != null)
				{
					listener.onFileUpdated();
				}
			}
			else
			{
				Log.d(LOG_TAG, "Nothing to sync: "+baseURL);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}
	
	private long localFileLastUpdated()
	{
		try
		{
			File f = new File(targetFile);
			long lastModified = f.lastModified();
			Log.d(LOG_TAG, "Local last modified "+targetFile+": "+lastModified);
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
			params.put(requestTypeKey, dateParamValue);
			String serverResponse = WebConnection.postToServer(baseURL, params);
			params.remove(requestTypeKey);
			
			JSONParser parser = new JSONParser();
			JSONObject response = (JSONObject) parser.parse(serverResponse);
			long lastModified = (Long) response.get(dateResponseFieldKey);
			
			Log.d(LOG_TAG, "Remote last modified "+targetFile+": "+lastModified);
			return lastModified;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return 0;
		}
	}

}
