package com.ubhave.datahandler.loggertypes;

import android.content.Context;

import com.ubhave.datahandler.config.DataTransferConfig;

public abstract class AbstractStoreOnlyLogger extends AbstractDataLogger
{

	protected AbstractStoreOnlyLogger(Context context)
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
