package com.ubhave.datastore.file;

import android.content.Context;

import com.ubhave.datahandler.ESDataManager;
import com.ubhave.datahandler.config.DataStorageConfig;
import com.ubhave.datahandler.except.DataHandlerException;
import com.ubhave.datastore.DataStorageInterface;
import com.ubhave.sensormanager.ESException;

public class FileStoreManager extends ESDataManager
{	
	public FileStoreManager(final Context context, final String dataPassword) throws ESException, DataHandlerException
	{
		super(context, dataPassword);
	}
	
	@Override
	protected DataStorageInterface getStorage(final String dataPassword)
	{
		return new FileStorage(context, dataPassword, fileTransferLock);
	}
	
	@Override
	protected final int getStorageType()
	{
		return DataStorageConfig.STORAGE_TYPE_FILES;
	}
}
