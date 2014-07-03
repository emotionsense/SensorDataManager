package com.ubhave.datahandler.loggertypes;

import android.content.Context;

import com.ubhave.datahandler.config.DataTransferConfig;
import com.ubhave.datahandler.except.DataHandlerException;
import com.ubhave.sensormanager.ESException;

public abstract class AbstractImmediateTransferLogger extends AbstractTransferLogger
{

	protected AbstractImmediateTransferLogger(Context context) throws DataHandlerException, ESException
	{
		super(context);
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
	
	protected abstract String getPostDataParamKey();
}
