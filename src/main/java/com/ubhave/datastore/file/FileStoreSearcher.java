package com.ubhave.datastore.file;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.ubhave.dataformatter.json.JSONFormatter;
import com.ubhave.datahandler.config.DataHandlerConfig;
import com.ubhave.datahandler.except.DataHandlerException;
import com.ubhave.sensormanager.ESException;
import com.ubhave.sensormanager.data.SensorData;
import com.ubhave.sensormanager.sensors.SensorUtils;

public class FileStoreSearcher extends FileStoreReader
{
	private final static String TAG = "LogFileDataStorage";
	private final Context context;

	public FileStoreSearcher(final Context context, final FileVault vault)
	{
		super(vault);
		this.context = context;
	}
	
	public List<SensorData> getRecentSensorData(final int sensorId, long startTimestamp) throws IOException, ESException, DataHandlerException
	{
		File root = vault.getLocalDirectory();
		File directory = vault.getDirectory(root, SensorUtils.getSensorName(sensorId));
		if (DataHandlerConfig.shouldLog())
		{
			Log.d(TAG, "Search: "+directory.getAbsolutePath());
		}
		
		ArrayList<SensorData> outputList = new ArrayList<SensorData>();
		if (directory != null && directory.exists() && directory.isDirectory())
		{
			JSONFormatter jsonFormatter = JSONFormatter.getJSONFormatter(context, sensorId);
			File[] files = directory.listFiles();
			for (File file : files)
			{
				if (DataHandlerConfig.shouldLog())
				{
					Log.d(TAG, "Search: "+file.getName());
				}
				
				List<JSONObject> fileContents = readFile(directory.getName(), file);
				for (JSONObject line : fileContents)
				{
					long timestamp = jsonFormatter.parseTimeStamp(line);
					if (timestamp >= startTimestamp)
					{
						SensorData sensorData = jsonFormatter.toSensorData(line.toString());
						outputList.add(sensorData);
					}
				}
			}
		}
		return outputList;
	}
}
