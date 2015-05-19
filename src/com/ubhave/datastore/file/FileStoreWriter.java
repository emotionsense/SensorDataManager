package com.ubhave.datastore.file;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;

import android.content.Context;
import android.util.Log;

import com.ubhave.datahandler.config.DataHandlerConfig;
import com.ubhave.datahandler.config.DataStorageConfig;
import com.ubhave.datahandler.config.DataStorageConstants;
import com.ubhave.datahandler.except.DataHandlerException;

public class FileStoreWriter
{
	private static final String TAG = "LogFileDataStorage";

	private final DataHandlerConfig config;
	private HashMap<String, Object> lockMap;
	private FileVault fileWriter;

	public FileStoreWriter(final Context context)
	{
		this.config = DataHandlerConfig.getInstance();
		this.lockMap = new HashMap<String, Object>();
		this.fileWriter = new FileVault();
	}
	
	public FileStoreWriter(final Context context, final HashMap<String, Object> lockMap)
	{
		this(context);
		this.lockMap = lockMap;
	}
	
	public void writeData(final String directoryName, String data) throws DataHandlerException
	{
		String rootPath = (String) config.get(DataStorageConfig.LOCAL_STORAGE_ROOT_NAME);
		if (rootPath.contains(DataStorageConfig.DEFAULT_ROOT_DIRECTORY))
		{
			 // TODO Deal with this exception
			throw new DataHandlerException(DataHandlerException.WRITING_TO_DEFAULT_DIRECTORY);
		}

		synchronized (getLock(directoryName))
		{
			final File directory = getDirectory(rootPath, directoryName);
			try
			{
				File dataFile;
				if (!fileWriter.isEncrypted())
				{
					dataFile = getLastestFile(directory);
					data += "\n";
				}
				else
				{
					dataFile = createNewFile(directory);
				}
				
				OutputStream cos = fileWriter.openForWriting(dataFile);
				cos.write(data.getBytes());
				cos.flush();
				cos.close();
			}
			catch (Exception e)
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

	private File getLastestFile(final File directory) throws DataHandlerException, IOException
	{
		File latestFile = null;
		final File[] files = directory.listFiles();
		if (files != null)
		{
			long latestUpdate = Long.MIN_VALUE;
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
		if (latestFile == null)
		{
			return createNewFile(directory);
		}
		else
		{
			// TODO re-enable
//			if (isFileLimitReached(latestFile))
//			{
//				fileStoreCleaner.moveDirectoryContentsForUpload(directory.getAbsolutePath());
//				latestFile = null;
//			}
		}
		return latestFile;
	}
	
	private File createNewFile(final File directory) throws IOException
	{
		File file = new File(directory.getAbsolutePath() + "/" + System.currentTimeMillis() + DataStorageConstants.LOG_FILE_SUFFIX);
//		while (file.exists())
//		{
			// TODO Deal with file exists case
//		}
		if (DataHandlerConfig.shouldLog())
		{
			Log.d(TAG, "Creating: " + file.getAbsolutePath());
		}
		
		boolean fileCreated = file.createNewFile();
		if (!fileCreated && DataHandlerConfig.shouldLog())
		{
			Log.d(TAG, "Creating file returned false.");
		}
		return file;
	}

	
//	private long getDurationLimit()
//	{
//		try
//		{
//			return (Long) config.get(DataStorageConfig.DATA_LIFE_MILLIS);
//		}
//		catch (Exception e)
//		{
//			e.printStackTrace();
//			return DataStorageConfig.DEFAULT_FILE_LIFE_MILLIS;
//		}
//	}
//
//	private boolean isFileLimitReached(File file)
//	{
//		try
//		{
//			long durationLimit = getDurationLimit();
//			if (file != null)
//			{
//				String fileName = file.getName();
//				if (fileName != null)
//				{
//					if (fileName.contains(DataStorageConstants.LOG_FILE_SUFFIX))
//					{
//						String timeStr = fileName.substring(0, fileName.indexOf(DataStorageConstants.LOG_FILE_SUFFIX));
//						long fileTimestamp = Long.parseLong(timeStr);
//						long currTime = System.currentTimeMillis();
//						if ((currTime - fileTimestamp) > durationLimit)
//						{
//							return true;
//						}
//					}
//				}
//			}
//			return false;
//		}
//		catch (Exception e)
//		{
//			e.printStackTrace();
//			return false;
//		}
//	}
}
