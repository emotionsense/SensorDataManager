package com.ubhave.datastore.db;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.ubhave.dataformatter.DataFormatter;
import com.ubhave.dataformatter.json.JSONFormatter;
import com.ubhave.datahandler.config.DataHandlerConfig;
import com.ubhave.datahandler.config.DataStorageConfig;
import com.ubhave.datahandler.config.DataStorageConstants;
import com.ubhave.datahandler.except.DataHandlerException;
import com.ubhave.datastore.DataStorageInterface;
import com.ubhave.sensormanager.ESException;
import com.ubhave.sensormanager.data.SensorData;
import com.ubhave.sensormanager.sensors.SensorUtils;

public class DBDataStorage implements DataStorageInterface
{
	private static final String TAG = "LogDBDataStorage";
	private static final String DEFAULT_DB_NAME = "com.ubhave.datastore";
	private static final Object fileTransferLock = new Object();
	
	private final Context context;
	private final DataTables dataTables;

	public DBDataStorage(final Context context)
	{
		this.context = context;
		this.dataTables = new DataTables(context, getDBName());
	}
	
	private String getDBName()
	{
		try
		{
			String dbName = (String) DataHandlerConfig.getInstance().get(DataStorageConfig.LOCAL_STORAGE_ROOT_NAME);
			return dbName;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return DEFAULT_DB_NAME;
		}
	}
	
	@Override
	public void onDataUploaded()
	{
		for (String tableName : dataTables.getTableNames())
		{
			dataTables.setSynced(tableName);
		}
	}

	@Override
	public String prepareDataForUpload()
	{
		DataHandlerConfig config = DataHandlerConfig.getInstance();
		File outputDir = context.getCacheDir();
		if (!outputDir.exists())
		{
			outputDir.mkdirs();
		}
		for (String tableName : dataTables.getTableNames())
		{
			try
			{
				List<JSONObject> entries = dataTables.getUnsyncedData(tableName);
				if (DataHandlerConfig.shouldLog())
				{
					Log.d(TAG, "Prepare: "+tableName+" has "+entries.size()+" entries.");
				}
				if (!entries.isEmpty())
				{
					String gzipFileName = config.getIdentifier() + "_"
							+ tableName + "_"
							+ System.currentTimeMillis()
							+ ".log"
							+ DataStorageConstants.ZIP_FILE_SUFFIX;
					
					synchronized (fileTransferLock)
					{
						File outputFile = new File(outputDir, gzipFileName);
						if (DataHandlerConfig.shouldLog())
						{
							Log.d(TAG, "Writing to: "+outputFile.getAbsolutePath());
						}

						GZIPOutputStream gzipOS = new GZIPOutputStream(new FileOutputStream(outputFile));
						try
						{
							Writer writer = new OutputStreamWriter(gzipOS, "UTF-8");
							try
							{
								for (JSONObject entry : entries)
								{
									writer.write(entry.toString() + "\n");
								}
							}
							finally
							{
								writer.flush();
								gzipOS.finish();
								writer.close();
							}
						}
						finally
						{
							gzipOS.close();
						}
					}
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		return outputDir.getAbsolutePath();
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
