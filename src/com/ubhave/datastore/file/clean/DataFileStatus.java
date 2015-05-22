package com.ubhave.datastore.file.clean;

import java.io.File;

import com.ubhave.datahandler.config.DataHandlerConfig;
import com.ubhave.datahandler.config.DataStorageConfig;
import com.ubhave.datahandler.config.DataStorageConstants;
import com.ubhave.datahandler.except.DataHandlerException;

public class DataFileStatus
{
	private final DataHandlerConfig config;

	public DataFileStatus()
	{
		this.config = DataHandlerConfig.getInstance();
	}
	
	private long getDurationLimit()
	{
		try
		{
			return (Long) config.get(DataStorageConfig.DATA_LIFE_MILLIS);
		}
		catch (DataHandlerException e)
		{
			e.printStackTrace();
			return DataStorageConfig.DEFAULT_FILE_LIFE_MILLIS;
		}
	}
	
	public boolean isDueForUpload(final File file)
	{
		if (file != null)
		{
			String fileName = file.getName();
			if (fileName.contains(DataStorageConstants.LOG_FILE_SUFFIX))
			{
				String timeStr = fileName.substring(0, fileName.indexOf(DataStorageConstants.LOG_FILE_SUFFIX));
				long fileTimestamp = Long.parseLong(timeStr);
				long currTime = System.currentTimeMillis();
				if ((currTime - fileTimestamp) > getDurationLimit())
				{
					return true;
				}
			}
		}
		return false;
	}
}
