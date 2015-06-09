package com.ubhave.datastore.db;

import net.sqlcipher.database.SQLiteDatabase;
import android.content.Context;

import com.ubhave.datahandler.ESDataManager;
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
	protected DataStorageInterface getStorage(final String dataPassword)
	{
		return new DBDataStorage(context, dataPassword);
	}
}
