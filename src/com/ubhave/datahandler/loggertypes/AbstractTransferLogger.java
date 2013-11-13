package com.ubhave.datahandler.loggertypes;

import android.content.Context;

import com.ubhave.datahandler.config.DataTransferConfig;

public abstract class AbstractTransferLogger extends AbstractDataLogger
{

	protected AbstractTransferLogger(Context context)
	{
		super(context);
	}

	@Override
	protected void configureDataStorage()
	{
		super.configureDataStorage();
		try
		{
			dataManager.setConfig(DataTransferConfig.POST_DATA_URL, getDataPostURL());
			dataManager.setConfig(DataTransferConfig.POST_DATA_URL_PASSWD, getPostPassword());
			dataManager.setConfig(DataTransferConfig.POST_RESPONSE_ON_SUCCESS, getSuccessfulPostResponse());
		}
		catch (Exception e)
		{
			dataManager = null;
			e.printStackTrace();
		}
	}

	protected abstract String getDataPostURL();

	protected abstract String getPostPassword();
	
	protected abstract String getSuccessfulPostResponse();

}
