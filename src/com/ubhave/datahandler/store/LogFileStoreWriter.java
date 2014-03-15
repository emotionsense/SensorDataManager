package com.ubhave.datahandler.store;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

import android.util.Log;

import com.ubhave.datahandler.config.DataHandlerConfig;
import com.ubhave.datahandler.config.DataStorageConfig;
import com.ubhave.datahandler.config.DataStorageConstants;
import com.ubhave.datahandler.except.DataHandlerException;

public class LogFileStoreWriter
{
	private static final String TAG = "LogFileDataStorage";
	
	private final FileStoreCleaner fileStoreCleaner;
	private final DataHandlerConfig config;
	private final HashMap<String, Object> lockMap;

	public LogFileStoreWriter(final FileStoreCleaner fileStoreCleaner, final HashMap<String, Object> lockMap)
	{
		this.config = DataHandlerConfig.getInstance();
		this.fileStoreCleaner = fileStoreCleaner;
		this.lockMap = lockMap;
	}
	
	public void writeData(String directoryName, String data) throws DataHandlerException
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
				File directory = getDirectory(rootPath, directoryName);
				File dataFile = getLastEditedFile(directory);
				if (dataFile == null || !dataFile.exists())
				{
					dataFile = new File(directory.getAbsolutePath() + "/" + System.currentTimeMillis() + DataStorageConstants.LOG_FILE_SUFFIX);
					if (DataHandlerConfig.shouldLog())
					{
						Log.d(TAG, "Creating: " + dataFile.getAbsolutePath());
					}
					
					boolean fileCreated = dataFile.createNewFile();
					if (!fileCreated && DataHandlerConfig.shouldLog())
					{
						Log.d(TAG, "Creating file returned false");
					}
				}

				// Append data to file
				FileOutputStream fos = new FileOutputStream(dataFile, true);
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
	
	private Object getLock(final String key)
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
	
	private File getDirectory(final String rootPath, final String directoryName)
	{
		File directory = new File(rootPath, directoryName);
		if (!directory.exists())
		{
			if (DataHandlerConfig.shouldLog())
			{
				Log.d(TAG, "Creating: " + directory.getAbsolutePath());
			}
			directory.mkdirs();
		}
		return directory;
	}
	
	private File getLastEditedFile(final File directory) throws DataHandlerException, IOException
	{
		long latestUpdate = Long.MIN_VALUE;
		File latestFile = null;
		File[] files = directory.listFiles();
		if (files != null)
		{
			for (File file : files)
			{
				if (file.isFile() && file.getName().endsWith(DataStorageConstants.LOG_FILE_SUFFIX))
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
				fileStoreCleaner.moveDirectoryContentsForUpload(directory.getAbsolutePath());
				latestFile = null;
			}
		}
		return latestFile;
	}
	
	private long getDurationLimit()
	{
		try
		{
			return (Long) config.get(DataStorageConfig.FILE_LIFE_MILLIS);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return DataStorageConfig.DEFAULT_FILE_LIFE_MILLIS;
		}
	}

	private boolean isFileLimitReached(File file)
	{
		try
		{
			long durationLimit = getDurationLimit();
			if (file != null)
			{
				String fileName = file.getName();
				if (fileName != null)
				{
					if (fileName.contains(DataStorageConstants.LOG_FILE_SUFFIX))
					{
						String timeStr = fileName.substring(0, fileName.indexOf(DataStorageConstants.LOG_FILE_SUFFIX));
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
			return false;
		}
	}
}
