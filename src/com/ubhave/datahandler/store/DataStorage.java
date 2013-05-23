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
import com.ubhave.datahandler.config.DataHandlerConfig;
import com.ubhave.datahandler.config.DataStorageConfig;
import com.ubhave.datahandler.except.DataHandlerException;
import com.ubhave.sensormanager.ESException;
import com.ubhave.sensormanager.data.SensorData;
import com.ubhave.sensormanager.sensors.SensorUtils;

public class DataStorage implements DataStorageInterface
{
	private static final String TAG = "DataStorage";
	private final Object fileTransferLock;
	private static final String UNKNOWN_SENSOR = "Unknown_Sensor";
	private static final String ERROR_DIRECTORY_NAME = "Error_Log";

	private final Context context;
	private final DataHandlerConfig config;
	private static HashMap<String, Object> lockMap = new HashMap<String, Object>();

	public DataStorage(Context context, final Object fileTransferLock)
	{
		this.context = context;
		this.config = DataHandlerConfig.getInstance();
		this.fileTransferLock = fileTransferLock;
	}

	private String checkLastEditedFile(String directoryFullPath) throws DataHandlerException, IOException
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
			if (isFileLimitReached(latestFile))
			{
				moveDirectoryContentsForUpload(directoryFullPath);
				latestFile = new File(directoryFullPath + "/" + System.currentTimeMillis() + ".log");
			}
		}
		else
		{
			latestFile = new File(directoryFullPath + "/" + System.currentTimeMillis() + ".log");
		}
		return latestFile.getAbsolutePath();
	}

	private boolean isFileLimitReached(File file)
	{
		long durationLimit = DataStorageConfig.DEFAULT_FILE_LIFE_MILLIS;
		// long sizeLimit = DataStorageConfig.DEFAULT_FILE_SIZE_BYTES;
		// long fileSize = file.length();
		// if (fileSize > sizeLimit)
		// {
		// return true;
		// }

		try
		{
			durationLimit = (Long) config.get(DataStorageConfig.FILE_LIFE_MILLIS);
			// sizeLimit = (Long) config.get(DataStorageConfig.FILE_MAX_SIZE);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		try
		{
			if (file != null)
			{
				String fileName = file.getName();
				if (fileName != null)
				{
					if (fileName.contains(".log"))
					{
						String timeStr = fileName.substring(0, fileName.indexOf(".log"));
						long fileTimestamp = Long.parseLong(timeStr);
						long currTime = System.currentTimeMillis();
						if ((currTime - fileTimestamp) > durationLimit)
						{
							return true;
						}
					}
				}
			}
			return false;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return true;
		}
	}

	private void moveFileToUploadDir(final File file)
	{
		// start a background thread to move files + transfer log files to the
		// server
		new Thread()
		{
			public void run()
			{
				// move files
				synchronized (fileTransferLock)
				{
					try
					{
						String uploadDir = (String) config.get(DataStorageConfig.LOCAL_STORAGE_UPLOAD_DIRECTORY_PATH);
						File directory = new File(uploadDir);
						if (!directory.exists())
						{
							directory.mkdirs();
						}
						file.renameTo(new File(directory.getAbsolutePath() + "/" + file.getName()));
					}
					catch (DataHandlerException e)
					{
						e.printStackTrace();
					}
				}
			}
		}.start();
	}

	private void moveDirectoryContentsForUpload(String directoryFullPath) throws DataHandlerException, IOException
	{
		Log.d(TAG, "moveFilesForUploadingToServer() " + directoryFullPath);
		File directory = new File(directoryFullPath);
		File[] files = directory.listFiles();
		for (File file : files)
		{
			if (file.getName().endsWith(".gz"))
			{
				moveFileToUploadDir(file);
			}
			else if (isFileLimitReached(file))
			{
				if (file.length() <= 0)
				{
					file.delete();
				}
				else
				{
					Log.d(TAG, "gzip file " + file);
					File gzippedFile = gzipFile(file);
					moveFileToUploadDir(gzippedFile);
					Log.d(TAG, "moved file " + gzippedFile.getAbsolutePath() + " to server upload dir");
					Log.d(TAG, "deleting file: " + file.getAbsolutePath());
					file.delete();
				}
			}
		}
	}

	@Override
	public void moveArchivedFilesForUpload()
	{
		try
		{
			String rootPath = (String) config.get(DataStorageConfig.LOCAL_STORAGE_ROOT_DIRECTORY_NAME);
			File[] rootDirectory = (new File(rootPath)).listFiles();
			for (File directory : rootDirectory)
			{
				String directoryName = directory.getName();
				if (!directoryName.contains((String) config.get(DataStorageConfig.LOCAL_STORAGE_UPLOAD_DIRECTORY_NAME)))
				{
					synchronized (getLock(directoryName))
					{
						try
						{
							moveDirectoryContentsForUpload(directory.getAbsolutePath());
						}
						catch (Exception e)
						{
							e.printStackTrace();
						}
					}
				}
			}
		}
		catch (DataHandlerException e)
		{
			e.printStackTrace();
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
		String uniqueUserId = null;
		try
		{
			uniqueUserId = (String) config.get(DataStorageConfig.UNIQUE_USER_ID);
		}
		catch (DataHandlerException e)
		{
		}

		String imeiPhone = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();

		String uniqueID = "";

		if (imeiPhone != null)
		{
			uniqueID = imeiPhone;
		}

		if (uniqueUserId != null)
		{
			uniqueID = uniqueID + "_" + uniqueUserId;
		}

		return uniqueID;
	}

	@Override
	public List<SensorData> getRecentSensorData(int sensorId, long startTimestamp) throws ESException, IOException
	{
		ArrayList<SensorData> outputList = new ArrayList<SensorData>();
		try
		{
			String sensorName = SensorUtils.getSensorName(sensorId);
			String rootPath = (String) config.get(DataStorageConfig.LOCAL_STORAGE_ROOT_DIRECTORY_NAME);
			JSONFormatter jsonFormatter = JSONFormatter.getJSONFormatter(context, sensorId);
			synchronized (getLock(sensorName))
			{
				String directoryFullPath = rootPath + "/" + sensorName;
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
							try
							{
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
							catch (Exception e)
							{
								e.printStackTrace();
							}
						}
						br.close();
					}
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
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
		String rootPath = (String) config.get(DataStorageConfig.LOCAL_STORAGE_ROOT_DIRECTORY_NAME);
		if (rootPath.contains(DataStorageConfig.DEFAULT_ROOT_DIRECTORY))
		{
			throw new DataHandlerException(DataHandlerException.WRITING_TO_DEFAULT_DIRECTORY);
		}

		synchronized (getLock(directoryName))
		{
			try
			{
				String directoryFullPath = rootPath + "/" + directoryName;
				File file = new File(directoryFullPath);
				if (!file.exists())
				{
					System.err.println("Creating: " + directoryFullPath);
					file.mkdirs();
				}

				String fileFullPath = checkLastEditedFile(directoryFullPath);
				file = new File(fileFullPath);
				if (!file.exists())
				{
					System.err.println("Creating: " + fileFullPath);
					try
					{
						boolean fileCreated = file.createNewFile();
						if (!fileCreated)
						{
							System.err.println("Creating file returned false");
						}
					}
					catch (Exception e)
					{
						System.err.println("Error creating file");
						e.printStackTrace();
					}

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
