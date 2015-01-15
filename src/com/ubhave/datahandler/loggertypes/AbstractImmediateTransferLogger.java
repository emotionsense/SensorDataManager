package com.ubhave.datahandler.loggertypes;

import android.content.Context;
import android.util.Log;

import com.ubhave.datahandler.config.DataHandlerConfig;
import com.ubhave.datahandler.config.DataStorageConfig;
import com.ubhave.datahandler.config.DataTransferConfig;
import com.ubhave.datahandler.except.DataHandlerException;
import com.ubhave.sensormanager.ESException;

public abstract class AbstractImmediateTransferLogger extends AbstractTransferLogger
{
	protected AbstractImmediateTransferLogger(final Context context, final int storageType) throws DataHandlerException, ESException
	{
		super(context, storageType);
		if (storageType != DataStorageConfig.STORAGE_TYPE_NONE)
		{
			if (DataHandlerConfig.shouldLog())
			{
				Log.d("AbstractImmediateTransferLogger", "Warning: unused storage type in immediate transfer logger.");
			}
		}
	}

	@Override
	protected void configureDataStorage()
	{
		super.configureDataStorage();
		try
		{
			dataManager.setConfig(DataTransferConfig.DATA_TRANSER_POLICY, DataTransferConfig.TRANSFER_IMMEDIATE);
			dataManager.setConfig(DataTransferConfig.POST_RAW_DATA_KEY, getPostDataParamKey());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	@Override
	protected String getStorageName()
	{
		return null; // No storage
	}
	
	protected abstract String getPostDataParamKey();
}
