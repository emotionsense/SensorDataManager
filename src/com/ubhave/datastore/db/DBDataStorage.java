package com.ubhave.datastore.db;

import java.io.IOException;
import java.util.List;

import android.content.Context;

import com.ubhave.dataformatter.DataFormatter;
import com.ubhave.dataformatter.json.JSONFormatter;
import com.ubhave.datahandler.config.DataStorageConstants;
import com.ubhave.datahandler.except.DataHandlerException;
import com.ubhave.datastore.DataStorageInterface;
import com.ubhave.sensormanager.ESException;
import com.ubhave.sensormanager.data.SensorData;
import com.ubhave.sensormanager.sensors.SensorUtils;

public class DBDataStorage implements DataStorageInterface
{
	private final Context context;
	private final DataTables dataTables;

	public DBDataStorage(final Context context)
	{
		this.context = context;
		this.dataTables = new DataTables(context);
	}

	@Override
	public void moveArchivedFilesForUpload()
	{
		// TODO compress files into temp cache
		// try
		// {
		// String rootPath = (String)
		// config.get(DataStorageConfig.LOCAL_STORAGE_ROOT_DIRECTORY_NAME);
		// File[] rootDirectory = (new File(rootPath)).listFiles();
		// if (rootDirectory != null)
		// {
		// for (File directory : rootDirectory)
		// {
		// if (directory != null && directory.isDirectory())
		// {
		// String directoryName = directory.getName();
		// if (directoryName != null && !directoryName.contains((String)
		// config.get(DataStorageConfig.LOCAL_STORAGE_UPLOAD_DIRECTORY_NAME)))
		// {
		// synchronized (getLock(directoryName))
		// {
		// try
		// {
		// fileStoreCleaner.moveDirectoryContentsForUpload(directory.getAbsolutePath());
		// }
		// catch (Exception e)
		// {
		// e.printStackTrace();
		// }
		// }
		// }
		// }
		// }
		// }
		// }
		// catch (DataHandlerException e)
		// {
		// e.printStackTrace();
		// }
	}

	@Override
	public List<SensorData> getRecentSensorData(int sensorId, long startTimestamp) throws ESException, IOException
	{
		String tableName = SensorUtils.getSensorName(sensorId);
		JSONFormatter formatter = DataFormatter.getJSONFormatter(context, sensorId);
		return dataTables.getRecentSensorData(tableName, formatter, startTimestamp);
	}

	@Override
	public void logSensorData(final SensorData data, final DataFormatter formatter) throws DataHandlerException
	{
		String sensorName;
		try
		{
			sensorName = SensorUtils.getSensorName(data.getSensorType());
		}
		catch (ESException e)
		{
			sensorName = DataStorageConstants.UNKNOWN_SENSOR;
			e.printStackTrace();
		}
		dataTables.writeData(sensorName, formatter.toString(data));
	}

	@Override
	public void logError(final String error) throws DataHandlerException
	{
		dataTables.writeData(DataStorageConstants.ERROR_DIRECTORY_NAME, error);
	}

	@Override
	public void logExtra(final String tag, final String data) throws DataHandlerException
	{
		dataTables.writeData(tag, data);
	}
}
