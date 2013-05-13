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
import com.ubhave.datahandler.transfer.DataTransferInterface;
import com.ubhave.sensormanager.ESException;
import com.ubhave.sensormanager.data.SensorData;
import com.ubhave.sensormanager.sensors.SensorUtils;

public class DataStorage implements DataStorageInterface
{
	private static final String TAG = "DataStorage";
	private static final Object fileTransferLock = new Object();
	private static final String UNKNOWN_SENSOR = "Unknown_Sensor";
	private static final String ERROR_DIRECTORY_NAME = "Error_Log";

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
		if (files != null)
		{
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
		}
		
		if (latestFile != null)
		{
			// TODO this should not check against a default value
			if (isFileDurationLimitReached(latestFile.getName(), DataHandlerConfig.DEFAULT_FILE_DURATION))
			{
//				moveFilesForUploadingToServer(directoryFullPath); // TODO removed for now
				latestFile = new File(directoryFullPath + "/" + System.currentTimeMillis() + ".log");
			}
		}
		else
		{
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
	
	private void moveFileToUploadDir(final File file, final DataTransferInterface transfer)
	{
		// start a background thread to move files + transfer log files to the server
		new Thread()
		{
			public void run()
			{
				// move files
				synchronized (fileTransferLock)
				{
					File directory = new File(DataHandlerConfig.SERVER_UPLOAD_DIR);
					if (!directory.exists())
					{
						directory.mkdirs();
					}
					file.renameTo(new File(directory.getAbsolutePath() + "/" + file.getName()));
				}
				transfer.attemptDataUpload(fileTransferLock);
			}
		}.start();
	}

	private void moveDirectoryContentsForUpload(String directoryFullPath, final DataTransferInterface transfer) throws DataHandlerException, IOException
	{
		Log.d(TAG, "moveFilesForUploadingToServer() " + directoryFullPath);
		File directory = new File(directoryFullPath);
		File[] files = directory.listFiles();
		for (File file : files)
		{
			if (file.getName().endsWith(".gz"))
			{
				moveFileToUploadDir(file, transfer);
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
					moveFileToUploadDir(gzippedFile, transfer);
					Log.d(TAG, "moved file " + gzippedFile.getAbsolutePath() + " to server upload dir");
					Log.d(TAG, "deleting file: " + file.getAbsolutePath());
					file.delete();
				}
			}
		}
	}
	
	@Override
	public void movesFilesAndUpload(final DataTransferInterface transfer)
	{
		File[] rootDirectory = (new File(DataHandlerConfig.PHONE_STORAGE_DIR)).listFiles();
		for (File directory : rootDirectory)
		{
			String directoryName = directory.getName();
			if (!directoryName.contains(DataHandlerConfig.UPLOAD_DIRECTORY))
			{
				synchronized (getLock(directoryName))  // TODO check/test lock is correct
				{
					try
					{
						moveDirectoryContentsForUpload(directory.getAbsolutePath(), transfer);
					}
					catch (Exception e)
					{
						e.printStackTrace();
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

	@Override
	public List<SensorData> getRecentSensorData(int sensorId, long startTimestamp) throws ESException, IOException
	{
		String sensorName = SensorUtils.getSensorName(sensorId);
		ArrayList<SensorData> outputList = new ArrayList<SensorData>();

		JSONFormatter jsonFormatter = JSONFormatter.getJSONFormatter(context, sensorId);

		synchronized (getLock(sensorName))
		{
			String directoryFullPath = DataHandlerConfig.PHONE_STORAGE_DIR + "/" + sensorName;
			File dir = new File(directoryFullPath);
			File[] files = dir.listFiles();
			if (files != null)
			{
				for (File file : files)
				{
					String line;
					BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
					while ((line = br.readLine()) != null)
					{
						// TODO: add support for other formatters
						// convert json string to sensor data object
						long timestamp = jsonFormatter.getTimestamp(line);
						if (timestamp >= startTimestamp)
						{
							SensorData sensorData = jsonFormatter.toSensorData(line);
							if (sensorData.getTimestamp() >= startTimestamp)
							{
								outputList.add(sensorData);
							}
						}
					}
					br.close();
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
			sensorName = UNKNOWN_SENSOR;
		}
		String directoryName = sensorName;
		writeData(directoryName, formatter.toString(data));
	}

	@Override
	public void logError(final String error) throws DataHandlerException
	{
		writeData(ERROR_DIRECTORY_NAME, error);
	}

	@Override
	public void logExtra(final String tag, final String data) throws DataHandlerException
	{
		writeData(tag, data);
	}
}
