package com.ubhave.datahandler.store;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.ubhave.dataformatter.DataFormatter;
import com.ubhave.dataformatter.json.JSONFormatter;
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

	private static HashMap<String, Object> lockMap = new HashMap<String, Object>();

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
			if (file.isFile() && file.getName().endsWith(".log"))
			{
				long update = file.lastModified();
				if (update > latestUpdate)
				{
					latestUpdate = update;
					latestFile = file;
				}
			}
		}

		if (latestFile == null || isFileDurationLimitReached(latestFile.getName(), DataHandlerConfig.DEFAULT_FILE_DURATION))
		{
			if (latestFile != null
					&& isFileDurationLimitReached(latestFile.getName(), DataHandlerConfig.DEFAULT_FILE_DURATION))
			{
				moveFilesForUploadingToServer(directoryFullPath);
			}
			latestFile = new File(directoryFullPath + "/" + System.currentTimeMillis() + ".log");

		}
		return latestFile.getAbsolutePath();
	}

	private boolean isFileDurationLimitReached(String fileName, long duration)
	{
		String timeStr = fileName.substring(0, fileName.indexOf(".log"));
		long fileTimestamp = Long.parseLong(timeStr);
		long currTime = System.currentTimeMillis();
		if ((currTime - fileTimestamp) > duration)
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	private void moveFilesForUploadingToServer(String directoryFullPath) throws DataHandlerException, IOException
	{
		Log.d(TAG, "moveFilesForUploadingToServer() " + directoryFullPath);

		File directory = new File(directoryFullPath);
		File[] files = directory.listFiles();
		for (File file : files)
		{
			if (file.getName().endsWith(".gz"))
			{
				try
				{
					DataManager.getInstance(context).moveFileToUploadDir(file);
				}
				catch (Exception e)
				{
					Log.e(TAG, Log.getStackTraceString(e));
				}
			}
			else if (isFileDurationLimitReached(file.getName(), DataHandlerConfig.DEFAULT_RECENT_DURATION))
			{
				if (file.length() <= 0)
				{
					file.delete();
				}
				else
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
	}

	private File gzipFile(File inputFile) throws IOException
	{

		byte[] buffer = new byte[1024];

		String parentFullPath = inputFile.getParent();
		File tempFile = new File(parentFullPath);
		String parentDirName = tempFile.getName();

		String gzipFileName = parentFullPath + "/" + getUniqueUserIdentifier() + "_" + parentDirName + "_"
				+ inputFile.getName() + ".gz";

		File outputFile = new File(gzipFileName);
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

	private String getUniqueUserIdentifier()
	{
		String imeiPhone = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
		return imeiPhone;
	}

	public List<SensorData> getRecentSensorData(int sensorId) throws ESException, IOException
	{
		String sensorName = SensorUtils.getSensorName(sensorId);
		ArrayList<SensorData> outputList = new ArrayList<SensorData>();

		JSONFormatter jsonFormatter = JSONFormatter.getJSONFormatter(sensorId);

		synchronized (getLock(sensorName))
		{
			String directoryFullPath = DataHandlerConfig.PHONE_STORAGE_DIR + "/" + sensorName;
			File dir = new File(directoryFullPath);
			File[] files = dir.listFiles();
			if (files != null)
			{
				for (File file : files)
				{

					BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
					while (true)
					{
						String line = br.readLine();
						if (line == null)
						{
							br.close();
							break;
						}
						else
						{
							// convert json string to sensor data object
							SensorData sensorData = jsonFormatter.toSensorData(line);
							outputList.add(sensorData);
						}
					}
				}
			}
		}

		return outputList;
	}

	private Object getLock(String key)
	{
		Object lock;
		synchronized (lockMap)
		{
			if (lockMap.containsKey(key))
			{
				lock = lockMap.get(key);
			}
			else
			{
				lock = new Object();
				lockMap.put(key, lock);
			}
		}
		return lock;
	}

	private void writeData(String directoryName, String data) throws DataHandlerException
	{
		synchronized (getLock(directoryName))
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
