package com.ubhave.datahandler.store;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

import android.content.Context;
import android.util.Log;

import com.ubhave.dataformatter.DataFormatter;
import com.ubhave.datahandler.DataHandlerConfig;
import com.ubhave.datahandler.DataHandlerException;
import com.ubhave.datahandler.DataManager;
import com.ubhave.sensormanager.ESException;
import com.ubhave.sensormanager.data.SensorData;
import com.ubhave.sensormanager.sensors.SensorUtils;

public class DataStorage
{
	private static final String TAG = "DataStorage";

	private final static String UNKNOWN_SENSOR = "Unknown_Sensor";
	private final static String ERROR_DIRECTORY_NAME = "Error_Log";

	private final Context context;

	public DataStorage(Context context)
	{
		this.context = context;
	}

	private String getFileName(String directoryFullPath) throws DataHandlerException, IOException
	{
		File directory = new File(directoryFullPath);
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
			if (latestFile != null && latestFile.length() > fileQuota)
			{
				moveFilesForUploadingToServer(directoryFullPath);
			}
			latestFile = new File(directoryFullPath + "/" + System.currentTimeMillis() + ".log");

		}
		return latestFile.getAbsolutePath();
	}

	private void moveFilesForUploadingToServer(String directoryFullPath) throws DataHandlerException, IOException
	{
		Log.d(TAG, "moveFilesForUploadingToServer() " + directoryFullPath);

		File directory = new File(directoryFullPath);
		File[] files = directory.listFiles();
		for (File file : files)
		{
			long fileQuota = (Long) DataHandlerConfig.getInstance().get(DataHandlerConfig.FILE_MAX_SIZE);
			if (file.length() > fileQuota)
			{
				Log.d(TAG, "gzip file " + file);
				File gzippedFile = gzipFile(file);

				try
				{
					DataManager.getInstance(context).moveFileToUploadDir(gzippedFile);
					Log.d(TAG, "moved file " + gzippedFile.getAbsolutePath() + " to server upload dir");
					Log.d(TAG, "deleting file: " + file.getAbsolutePath());
					file.delete();
				}
				catch (Exception te)
				{
					Log.e(TAG, Log.getStackTraceString(te));
				}
			}
		}
	}

	private File gzipFile(File inputFile) throws IOException
	{

		byte[] buffer = new byte[1024];

		File outputFile = new File(inputFile.getAbsolutePath() + ".gz");
		GZIPOutputStream gzipOS = new GZIPOutputStream(new FileOutputStream(outputFile));
		FileInputStream in = new FileInputStream(inputFile);

		int len;
		while ((len = in.read(buffer)) > 0)
		{
			gzipOS.write(buffer, 0, len);
		}

		in.close();

		gzipOS.finish();
		gzipOS.close();

		return outputFile;
	}

	private void writeData(String directoryName, String data) throws DataHandlerException
	{
		try
		{
			String directoryFullPath = DataHandlerConfig.PHONE_STORAGE_DIR + "/" + directoryName;
			File file = new File(directoryFullPath);
			if (!file.exists())
			{
				file.mkdirs();
			}

			String fileFullPath = getFileName(directoryFullPath);

			file = new File(fileFullPath);
			if (!file.exists())
			{
				file.createNewFile();
			}
			// append mode
			FileOutputStream fos = new FileOutputStream(file, true);
			fos.write(data.getBytes());
			fos.write("\n".getBytes());
			fos.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
			throw new DataHandlerException(DataHandlerException.IO_EXCEPTION);
		}
	}

	public void logSensorData(final SensorData data, final DataFormatter formatter) throws DataHandlerException
	{
		String sensorName;
		try
		{
			sensorName = SensorUtils.getSensorName(data.getSensorType());
		}
		catch (ESException e)
		{
			sensorName = UNKNOWN_SENSOR;
		}
		String directoryName = sensorName;

		writeData(directoryName, formatter.toString(data));
	}

	public void logError(final String error) throws DataHandlerException
	{
		writeData(ERROR_DIRECTORY_NAME, error);
	}

	public void logExtra(final String tag, final String data) throws DataHandlerException
	{
		writeData(tag, data);
	}
}
