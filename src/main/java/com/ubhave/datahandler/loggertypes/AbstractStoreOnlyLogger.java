package com.ubhave.datahandler.loggertypes;

import android.content.Context;

import com.ubhave.datahandler.config.DataTransferConfig;
import com.ubhave.datahandler.except.DataHandlerException;
import com.ubhave.sensormanager.ESException;

public abstract class AbstractStoreOnlyLogger extends AbstractDataLogger
{
	protected AbstractStoreOnlyLogger(final Context context, final int storageType) throws DataHandlerException, ESException
	{
		super(context, storageType);
	}

	@Override
	protected void configureDataStorage() throws DataHandlerException
	{
		super.configureDataStorage();
		dataManager.setConfig(DataTransferConfig.DATA_TRANSER_POLICY, DataTransferConfig.STORE_ONLY);
	}
}
