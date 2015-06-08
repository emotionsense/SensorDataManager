package com.ubhave.datahandler.loggertypes;

import android.content.Context;
import android.util.Log;

import com.ubhave.datahandler.config.DataHandlerConfig;
import com.ubhave.datahandler.config.DataStorageConfig;
import com.ubhave.datahandler.config.DataTransferConfig;
import com.ubhave.datahandler.except.DataHandlerException;
import com.ubhave.sensormanager.ESException;

public abstract class AbstractStoreOnlyLogger extends AbstractDataLogger
{
	protected AbstractStoreOnlyLogger(final Context context, final int storageType) throws DataHandlerException, ESException
	{
		super(context, storageType);
		if (storageType == DataStorageConfig.STORAGE_TYPE_NONE)
		{
			if (DataHandlerConfig.shouldLog())
			{
				Log.d(LOG_TAG, "Store only logger cannot have STORAGE_TYPE_NONE");
			}
			throw new DataHandlerException(DataHandlerException.CONFIG_CONFLICT);
		}
	}

	@Override
	protected void configureDataStorage()
	{
		super.configureDataStorage();
		try
		{
			dataManager.setConfig(DataTransferConfig.DATA_TRANSER_POLICY, DataTransferConfig.STORE_ONLY);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
