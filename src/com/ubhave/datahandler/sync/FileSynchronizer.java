package com.ubhave.datahandler.sync;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import android.content.Context;

import com.ubhave.datahandler.DataHandlerException;
import com.ubhave.datahandler.config.DataHandlerConfig;
import com.ubhave.datahandler.config.FileSyncConfig;
import com.ubhave.http.WebConnection;

public class FileSynchronizer implements FileSyncInterface
{
	private final Context context;
	private final DataHandlerConfig config;
	
	public FileSynchronizer(Context context)
	{
		this.context = context;
		config = DataHandlerConfig.getInstance();
	}
	
	@Override
	public int addRemoteToSyncList(final String url, final HashMap<String, String> queryParameters, final String filePath) throws DataHandlerException
	{
		throw new DataHandlerException(DataHandlerException.UNIMPLEMENTED);
	}
	
	@Override
	public void removeFromSyncList(int id) throws DataHandlerException
	{
		throw new DataHandlerException(DataHandlerException.UNIMPLEMENTED);
	}
	
	@Override
	public void syncUpdatedFiles()
	{
		// TODO add support for more than 1 file
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
