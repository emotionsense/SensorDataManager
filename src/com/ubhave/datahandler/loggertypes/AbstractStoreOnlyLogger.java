package com.ubhave.datahandler.loggertypes;

import android.content.Context;

import com.ubhave.datahandler.config.DataTransferConfig;
import com.ubhave.datahandler.except.DataHandlerException;
import com.ubhave.sensormanager.ESException;

public abstract class AbstractStoreOnlyLogger extends AbstractDataLogger
{

	protected AbstractStoreOnlyLogger(Context context) throws DataHandlerException, ESException
	{
		super(context);
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
