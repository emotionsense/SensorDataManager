package com.ubhave.datastore.file.clean;

import java.io.File;

import com.ubhave.datahandler.config.DataHandlerConfig;
import com.ubhave.datahandler.config.DataStorageConfig;
import com.ubhave.datahandler.config.DataStorageConstants;

public class DataFileStatus
{
	private final DataHandlerConfig config;

	public DataFileStatus()
	{
		this.config = DataHandlerConfig.getInstance();
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
				long durationLimit = (Long) config.get(DataStorageConfig.DATA_LIFE_MILLIS, DataStorageConfig.DEFAULT_FILE_LIFE_MILLIS);
				if ((currTime - fileTimestamp) > durationLimit)
				{
					return true;
				}
			}
		}
		return false;
	}
}
