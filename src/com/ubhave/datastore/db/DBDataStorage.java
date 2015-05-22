package com.ubhave.datastore.db;

import java.io.File;
import java.io.IOException;
import java.util.List;

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
//	private static final Object fileTransferLock = new Object();

	private final Context context;
	private final DataHandlerConfig config;
	private final DataTables dataTables;

	public DBDataStorage(final Context context)
	{
		this.context = context;
		this.config = DataHandlerConfig.getInstance();
		this.dataTables = new DataTables(context, getDBName());
	}

	private String getDBName()
	{
		try
		{
			return (String) config.get(DataStorageConfig.LOCAL_DB_STORAGE_NAME);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return DataStorageConfig.DEFAULT_DB_NAME;
		}
	}

	@Override
	public void onDataUploaded()
	{
//		getCleanCacheDir();
		for (String tableName : dataTables.getTableNames())
		{
			Log.d("DB", tableName + " set synced");
			dataTables.setSynced(tableName);
		}
	}

//	private synchronized int writeMaxEntries(final File outputFile, List<JSONObject> entries) throws IOException, ZipException
//	{
//		int written = 0;
//		final ZipOutputStream out = new ZipOutputStream(new FileOutputStream(outputFile));
//		File tmpFile = File.createTempFile("data", null);
//		try
//		{
//			List<JSONObject> entriesCopy = new ArrayList<JSONObject>();
//			for (JSONObject e : entries)
//			{
//				entriesCopy.add(e);
//			}
//
//			// First, write dummy data to tmpFile to get right parameters
//			OutputStream os = new FileOutputStream(tmpFile);
//			for (int i = 0; i < DataStorageConstants.UPLOAD_FILE_MAX_LINES; i++)
//			{
//				if (!entriesCopy.isEmpty())
//				{
//					JSONObject entry = entries.get(0);
//					byte[] data = (entry.toString() + "\n").getBytes();
//					os.write(data, 0, data.length);
//					entriesCopy.remove(0);
//				}
//				else
//				{
//					break;
//				}
//			}
//			os.close();
//
//			out.putNextEntry(tmpFile, parameters);
//
//			for (int i = 0; i < DataStorageConstants.UPLOAD_FILE_MAX_LINES; i++)
//			{
//				if (!entries.isEmpty())
//				{
//					JSONObject entry = entries.get(0);
//					byte[] data = (entry.toString() + "\n").getBytes();
//					out.write(data, 0, data.length);
//					entries.remove(0);
//					written++;
//				}
//				else
//				{
//					break;
//				}
//			}
//		}
//		finally
//		{
//			tmpFile.delete();
//			out.closeEntry();
//			out.finish();
//			out.close();
//		}
//
//		return written;
//	}

//	private void writeEntries(final File outputDir, final String id, final String tableName, final List<JSONObject> entries) throws IOException
//	{
//		while (!entries.isEmpty())
//		{
//			synchronized (fileTransferLock)
//			{
//				String zipFileName = id + "_" + tableName + "_" + System.currentTimeMillis() + DataStorageConstants.LOG_FILE_SUFFIX + DataStorageConstants.ZIP_FILE_SUFFIX;
//
//				final File outputFile = new File(outputDir, zipFileName);
//				if (DataHandlerConfig.shouldLog())
//				{
//					Log.d(TAG, "Writing to: " + outputFile.getAbsolutePath());
//				}
//				int written;
//				try
//				{
//					written = writeMaxEntries(outputFile, entries);
//				}
//				catch (ZipException e)
//				{
//					Log.e(TAG, "Failed writing " + outputFile.getAbsolutePath());
//					e.printStackTrace();
//					outputFile.delete();
//					break;
//				}
//				if (written == 0)
//				{
//					outputFile.delete();
//					break;
//				}
//			}
//		}
//	}

	@Override
	public String prepareDataForUpload()
	{
		if (DataHandlerConfig.shouldLog())
		{
			Log.d(TAG, "DB prepareDataForUpload()");
		}

		try
		{
			String id = config.getIdentifier();
			File outputDir = null;//getCleanCacheDir();

			int written = 0;
			for (String tableName : dataTables.getTableNames())
			{
				try
				{
					List<JSONObject> entries = dataTables.getUnsyncedData(tableName);
					if (DataHandlerConfig.shouldLog())
					{
						Log.d(TAG, "Prepare: " + tableName + " has " + entries.size() + " entries for upload.");
					}
					if (!entries.isEmpty())
					{
//						writeEntries(outputDir, id, tableName, entries);
						written++;
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
			if (written == 0)
			{
				Log.d(TAG, "DB prepareDataForUpload(): no data to upload.");
				return null;
			}
			return outputDir.getAbsolutePath();
		}
		catch (DataHandlerException e)
		{
			if (DataHandlerConfig.shouldLog())
			{
				e.printStackTrace();
			}

			return null;
		}
	}

	@Override
	public List<SensorData> getRecentSensorData(int sensorId, long startTimestamp) throws ESException, IOException
	{
		String tableName = SensorUtils.getSensorName(sensorId);
		JSONFormatter formatter = DataFormatter.getJSONFormatter(context, sensorId);
		// TODO this will break if the user isn't using this formatter to store data
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
	public void logExtra(final String tag, final String data) throws DataHandlerException
	{
		dataTables.writeData(tag, data);
	}
}
