package com.ubhave.datahandler.loggertypes;

import java.util.ArrayList;

import android.Manifest;
import android.content.Context;

import com.ubhave.datahandler.config.DataStorageConfig;
import com.ubhave.datahandler.config.DataTransferConfig;
import com.ubhave.datahandler.except.DataHandlerException;
import com.ubhave.datahandler.transfer.DataUploadCallback;
import com.ubhave.sensormanager.ESException;

public abstract class AbstractAsyncTransferLogger extends AbstractTransferLogger
{
	protected AbstractAsyncTransferLogger(final Context context, final int storageType) throws DataHandlerException, ESException
	{
		super(context, storageType);
	}
	
	@Override
	protected ArrayList<String> getPermissions(final int storageType)
	{
		ArrayList<String> permissions = super.getPermissions(storageType);
		permissions.add(Manifest.permission.ACCESS_NETWORK_STATE);
		return permissions;
	}

	@Override
	protected void configureDataStorage() throws DataHandlerException
	{
		super.configureDataStorage();
		dataManager.setConfig(DataTransferConfig.DATA_TRANSER_POLICY, DataTransferConfig.TRANSFER_PERIODICALLY);
		dataManager.setConfig(DataStorageConfig.DATA_LIFE_MILLIS, getDataLifeMillis());
		dataManager.setConfig(DataTransferConfig.TRANSFER_ALARM_INTERVAL, getTransferAlarmLengthMillis());
		dataManager.setConfig(DataTransferConfig.POST_KEY, getPostKey());
		dataManager.setConfig(DataTransferConfig.WAIT_FOR_WIFI_INTERVAL_MILLIS, getWaitForWiFiMillis());
	}
	
	protected abstract String getPostKey();

	protected abstract long getDataLifeMillis();

	protected abstract long getTransferAlarmLengthMillis();
	
	protected abstract long getWaitForWiFiMillis();
	
	public void flush(final DataUploadCallback callback) throws DataHandlerException
	{
		dataManager.postAllStoredData(callback);
	}
}
