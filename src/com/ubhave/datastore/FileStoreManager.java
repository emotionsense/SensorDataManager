package com.ubhave.datastore;

import android.content.Context;

import com.ubhave.datahandler.ESDataManager;
import com.ubhave.datahandler.config.DataStorageConfig;
import com.ubhave.datahandler.config.DataTransferConfig;
import com.ubhave.datahandler.except.DataHandlerException;
import com.ubhave.datastore.file.FileDataStorage;
import com.ubhave.sensormanager.ESException;

public class FileStoreManager extends ESDataManager
{
	private static final Object fileTransferLock = new Object();
	
	public FileStoreManager(final Context context) throws ESException, DataHandlerException
	{
		super(context);
	}
	
	@Override
	protected DataStorageInterface getStorage()
	{
		return new FileDataStorage(context, fileTransferLock);
	}

	@Override
	public void transferStoredData()
	{
		storage.moveArchivedFilesForUpload();
		synchronized (fileTransferLock)
		{
			transfer.attemptDataUpload();
		}
	}
	
	@Override
	public void postAllStoredData() throws DataHandlerException
	{
		if ((Integer) config.get(DataTransferConfig.DATA_TRANSER_POLICY) != DataTransferConfig.STORE_ONLY)
		{
			long currentFileLife = (Long) config.get(DataStorageConfig.DATA_LIFE_MILLIS);
			config.setConfig(DataStorageConfig.DATA_LIFE_MILLIS, -1L);
			storage.moveArchivedFilesForUpload();
			synchronized (fileTransferLock)
			{
				transfer.uploadData();
			}
			config.setConfig(DataStorageConfig.DATA_LIFE_MILLIS, currentFileLife);
		}
	}
}
