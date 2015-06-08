package com.ubhave.datahandler.loggertypes;

import java.util.ArrayList;

import android.Manifest;
import android.content.Context;
import android.util.Log;

import com.ubhave.datahandler.config.DataHandlerConfig;
import com.ubhave.datahandler.config.DataStorageConfig;
import com.ubhave.datahandler.config.DataTransferConfig;
import com.ubhave.datahandler.except.DataHandlerException;
import com.ubhave.sensormanager.ESException;

public abstract class AbstractAsyncTransferLogger extends AbstractTransferLogger
{
	protected AbstractAsyncTransferLogger(final Context context, final int storageType) throws DataHandlerException, ESException
	{
		super(context, storageType);
		if (storageType == DataStorageConfig.STORAGE_TYPE_NONE)
		{
			if (DataHandlerConfig.shouldLog())
			{
				Log.d(LOG_TAG, "Async transfer store cannot have STORAGE_TYPE_NONE");
			}
			throw new DataHandlerException(DataHandlerException.CONFIG_CONFLICT);
		}
	}
	
	@Override
	protected ArrayList<String> getPermissions(final int storageType)
	{
		ArrayList<String> permissions = super.getPermissions(storageType);
		permissions.add(Manifest.permission.ACCESS_NETWORK_STATE);
		return permissions;
	}

	@Override
	protected void configureDataStorage()
	{
		super.configureDataStorage();
		try
		{
			dataManager.setConfig(DataTransferConfig.DATA_TRANSER_POLICY, DataTransferConfig.TRANSFER_PERIODICALLY);
			dataManager.setConfig(DataStorageConfig.DATA_LIFE_MILLIS, getDataLifeMillis());
			dataManager.setConfig(DataTransferConfig.TRANSFER_ALARM_INTERVAL, getTransferAlarmLengthMillis());
			dataManager.setConfig(DataTransferConfig.POST_KEY, getPostKey());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	protected abstract String getPostKey();

	protected abstract long getDataLifeMillis();

	protected abstract long getTransferAlarmLengthMillis();
	
	public void flush() throws DataHandlerException
	{
		dataManager.postAllStoredData();
	}
}
