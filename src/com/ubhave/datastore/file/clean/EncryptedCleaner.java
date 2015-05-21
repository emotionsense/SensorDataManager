package com.ubhave.datastore.file.clean;

import java.io.File;
import java.io.IOException;

import android.util.Log;

import com.ubhave.datahandler.config.DataHandlerConfig;
import com.ubhave.datahandler.config.DataStorageConfig;
import com.ubhave.datahandler.config.DataStorageConstants;
import com.ubhave.datahandler.except.DataHandlerException;
import com.ubhave.datastore.file.FileStoreAbstractReader;
import com.ubhave.datastore.file.FileVault;

public class EncryptedCleaner extends FileStoreAbstractReader
{
	private final static String TAG = "LogFileDataStorage";

	public EncryptedCleaner(final FileVault vault)
	{
		super(vault);
	}
	
	public void moveDirectoryContentsForUpload(final String directoryPath) throws DataHandlerException, IOException
	{
		if (DataHandlerConfig.shouldLog())
		{
			Log.d(TAG, "moveDirectoryContentsForUpload() " + directoryPath);
		}
		
		File directory = new File(directoryPath);
		if (directory != null && directory.exists())
		{
			File[] fileList = directory.listFiles();
			if (fileList != null)
			{
				for (File file : fileList)
				{
					if (isLogFileDueForUpload(file))
					{
						if (file.length() <= 0)
						{
							file.delete();
						}
						else
						{
							// TODO read contents
						}	
					}
				}
				// TODO move contents to upload vault
			}
		}
	}
	
	private long getDurationLimit()
	{
		try
		{
			DataHandlerConfig config = DataHandlerConfig.getInstance();
			return (Long) config.get(DataStorageConfig.DATA_LIFE_MILLIS);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return DataStorageConfig.DEFAULT_FILE_LIFE_MILLIS;
		}
	}
	
	private boolean isLogFileDueForUpload(File file)
	{
		try
		{
			long durationLimit = getDurationLimit();
			if (file != null)
			{
				String fileName = file.getName();
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
			return false;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return false;
		}
	}
}
