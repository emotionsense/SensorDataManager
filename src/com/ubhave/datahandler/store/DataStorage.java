package com.ubhave.datahandler.store;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;

import com.ubhave.dataformatter.DataFormatter;
import com.ubhave.datahandler.DataHandlerConfig;
import com.ubhave.datahandler.DataHandlerException;
import com.ubhave.sensormanager.ESException;
import com.ubhave.sensormanager.data.SensorData;
import com.ubhave.sensormanager.sensors.SensorUtils;

public class DataStorage
{
	private final static String UNKNOWN_SENSOR = "Unknown_Sensor";
	private final static String ERROR_DIRECTORY = "Error_Log";

	private final Context context;

	public DataStorage(Context context)
	{
		this.context = context;
	}

	private File getDirectory(String directory) throws DataHandlerException
	{
		File dir = context.getDir(directory, Context.MODE_PRIVATE);
		DataHandlerConfig config = DataHandlerConfig.getInstance();
		if (config.containsConfig(DataHandlerConfig.FILE_STORAGE_QUOTA))
		{
			long quota = (Long) config.get(DataHandlerConfig.FILE_STORAGE_QUOTA);
			if (dir.length() > quota)
			{
				throw new DataHandlerException(DataHandlerException.STORAGE_OVER_QUOTA);
			}
		}
		
		if (dir != null)
		{
			return dir;
		}
		else
		{
			throw new DataHandlerException(DataHandlerException.STORAGE_CREATE_ERROR);
		}
	}

	private String getFileName(File directory) throws DataHandlerException
	{
		File[] files = directory.listFiles();
		long latestUpdate = Long.MIN_VALUE;
		File latestFile = null;
		for (File file : files)
		{
			if (file.isFile())
			{
				long update = file.lastModified();
				if (update > latestUpdate)
				{
					latestUpdate = update;
					latestFile = file;
				}
			}
		}
		
		long fileQuota = (Long) DataHandlerConfig.getInstance().get(DataHandlerConfig.FILE_MAX_SIZE);
		if (latestFile == null || latestFile.length() > fileQuota)
		{
			latestFile = new File(System.currentTimeMillis()+".log");
		}
		return latestFile.getAbsolutePath();
	}

	private void writeData(String directory, String data) throws DataHandlerException
	{
		File dir = getDirectory(directory);
		String fileName = getFileName(dir);
		try
		{
			FileOutputStream fos = context.openFileOutput(fileName, Context.MODE_APPEND);
			fos.write(data.getBytes());
			fos.close();
		}
		catch (IOException e)
		{
			throw new DataHandlerException(DataHandlerException.IO_EXCEPTION);
		}
	}

	public void logSensorData(final SensorData data, final DataFormatter formatter) throws DataHandlerException
	{
		String directory;
		try
		{
			directory = SensorUtils.getSensorName(data.getSensorType());
		}
		catch (ESException e)
		{
			directory = UNKNOWN_SENSOR;
		}
		writeData(directory, formatter.toString(data));
	}

	public void logError(final String error) throws DataHandlerException
	{
		writeData(ERROR_DIRECTORY, error);
	}

	public void logExtra(final String tag, final String data) throws DataHandlerException
	{
		writeData(tag, data);
	}
}
