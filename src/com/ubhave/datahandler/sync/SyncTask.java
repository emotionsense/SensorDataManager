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

public class SyncTask extends AsyncTask<Void, Void, Boolean>
{

	private final static String LOG_TAG = "SyncTask";
	
	private Context context;
	private String baseURL, targetFile;
	private HashMap<String, String> params;
	private String requestTypeKey, dateParamValue, fileParamValue;
	private String dateResponseFieldKey;
	
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
	protected Boolean doInBackground(Void... ps)
	{
		try
		{
			if (remoteFileLastUpdated() > localFileLastUpdated())
			{
				Log.d(LOG_TAG, "Downloading from: "+baseURL);
				params.put(requestTypeKey, fileParamValue);
				String fileContents = WebConnection.postToServer(baseURL, params);
				params.remove(requestTypeKey);

				Log.d(LOG_TAG, "Writing file: "+targetFile);
				FileOutputStream fos = context.openFileOutput(targetFile, Context.MODE_PRIVATE);
				fos.write(fileContents.getBytes());
				fos.close();
				return Boolean.TRUE;
			}
			else
			{
				return Boolean.FALSE;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return Boolean.FALSE;
		}
	}
	
	private long localFileLastUpdated()
	{
		try
		{
			File f = new File(context.getFilesDir()+"/"+targetFile);
			if (!f.exists())
			{
				Log.d(LOG_TAG, "Local file missing");
				return 0;
			}
			else
			{
				long lastModified = f.lastModified();
				Log.d(LOG_TAG, "Local last modified "+targetFile+": "+lastModified);
				return f.lastModified();
			}
			
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
			Log.d("SyncTask", serverResponse);
			
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
