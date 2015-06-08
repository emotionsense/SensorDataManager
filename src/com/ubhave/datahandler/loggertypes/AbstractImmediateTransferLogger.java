package com.ubhave.datahandler.loggertypes;

import android.content.Context;

import com.ubhave.datahandler.config.DataStorageConfig;
import com.ubhave.datahandler.config.DataTransferConfig;
import com.ubhave.datahandler.except.DataHandlerException;
import com.ubhave.sensormanager.ESException;

public abstract class AbstractImmediateTransferLogger extends AbstractTransferLogger
{
	protected AbstractImmediateTransferLogger(final Context context) throws DataHandlerException, ESException
	{
		super(context, DataStorageConfig.STORAGE_TYPE_NONE);
	}

	@Override
	protected void configureDataStorage()
	{
		super.configureDataStorage();
		try
		{
			dataManager.setConfig(DataTransferConfig.DATA_TRANSER_POLICY, DataTransferConfig.TRANSFER_IMMEDIATE);
			dataManager.setConfig(DataTransferConfig.POST_KEY, getPostKey());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	@Override
	protected final String getStorageName()
	{
		// No storage
		return null; 
	}
	
	@Override
	protected final String getEncryptionPassword()
	{
		// No encryption in current version
		return null;
	}
	
	protected abstract String getPostKey();
}
