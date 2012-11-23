package com.ubhave.datahandler.store;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.ubhave.dataformatter.DataFormatter;
import com.ubhave.datahandler.DataHandlerException;
import com.ubhave.sensormanager.ESException;
import com.ubhave.sensormanager.data.SensorData;
import com.ubhave.sensormanager.sensors.SensorUtils;

public class DataStorage
{
	private final static String UNKNOWN_SENSOR = "Unknown_Sensor";
	private BufferedWriter writer;

	public boolean createDirectory(final String dir)
	{
		File directory = new File(dir);
		if (!directory.exists())
		{
			return directory.mkdirs();
		}
		return true;
	}

	public void openFile(final String fn) throws IOException
	{
		
	}

	public void writeLine(final String fn, final String data) throws IOException
	{
		
	}

	public void closeFile() throws IOException
	{
		
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
		
		try
		{
			String fn = directory + "/";
			writer = new BufferedWriter(new FileWriter(fn, true));
			writer.write(formatter.toString(data));
			writer.newLine();
			writer.flush();
			writer.close();
		}
		catch (IOException e)
		{
			// TODO
			// throw new
			// DataHandlerException(DataHandlerException.STORAGE_OVER_QUOTA);
		}
	}

	public void logError(final String error) throws DataHandlerException
	{
		// TODO
	}

	public void logExtra(final String tag, final String data) throws DataHandlerException
	{
		// TODO
	}
}
