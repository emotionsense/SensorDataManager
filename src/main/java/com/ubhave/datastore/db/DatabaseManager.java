package com.ubhave.datastore.db;

import net.sqlcipher.database.SQLiteDatabase;
import android.content.Context;
import android.util.Log;

import com.ubhave.datahandler.ESDataManager;
import com.ubhave.datahandler.config.DataStorageConfig;
import com.ubhave.datahandler.except.DataHandlerException;
import com.ubhave.datastore.DataStorageInterface;
import com.ubhave.sensormanager.ESException;

public class DatabaseManager extends ESDataManager
{	
	public DatabaseManager(final Context context, final String dataPassword) throws ESException, DataHandlerException
	{
		super(context, dataPassword);
		SQLiteDatabase.loadLibs(context);
	}
	
	@Override
	protected DataStorageInterface getStorage(final String dataPassword) throws DataHandlerException
	{
		Log.d(DatabaseStorage.TAG, "Creating database storage in manager");
		return new DatabaseStorage(context, dataPassword);
	}
	
	@Override
	protected final int getStorageType()
	{
		return DataStorageConfig.STORAGE_TYPE_DB;
	}
}
