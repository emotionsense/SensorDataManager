package com.ubhave.datastore.none;

import android.content.Context;

import com.ubhave.datahandler.ESDataManager;
import com.ubhave.datahandler.except.DataHandlerException;
import com.ubhave.datastore.DataStorageInterface;
import com.ubhave.sensormanager.ESException;

public class NoStorageManager extends ESDataManager
{
	public NoStorageManager(final Context context) throws ESException, DataHandlerException
	{
		super(context, null);
	}
	
	@Override
	protected DataStorageInterface getStorage(final String dataPassword)
	{
		return null;
	}
}
