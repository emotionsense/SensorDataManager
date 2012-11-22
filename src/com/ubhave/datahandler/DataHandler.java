package com.ubhave.datahandler;

import java.util.HashMap;

import android.content.Context;

import com.ubhave.datahandler.store.DataStorage;
import com.ubhave.datahandler.transfer.DataTransfer;
import com.ubhave.sensormanager.ESException;
import com.ubhave.sensormanager.data.SensorData;

public class DataHandler
{
	public final static String DATA_POLICY = "policy";
	public final static int TRANSFER_IMMEDIATE = 0;
	public final static int TRANSFER_ON_WIFI = 1;
	public final static int TRANFER_BULK_ON_INTERVAL = 2;
	public final static int STORE_ONLY = 3;
	
	public final static String FILE_STORAGE_QUOTA = "quota";
	
	public final static String FILE_DELETION_POLICY = "deletion";
	public final static int NEVER_DELETE = 0;
	public final static int DELETE_OLDEST_FIRST = 1;
	public final static int DELETE_NEWEST_FIRST = 2;
	
	
	private static DataHandler instance;
	
	private final HashMap<String, Integer> config;
	private final DataStorage storage;
	private final DataTransfer transfer;
	
	public static DataHandler getInstance(final Context context) throws ESException
	{
		if (instance == null)
		{
			instance = new DataHandler(context);
		}
		return instance;
	}
	
	private DataHandler(final Context context) throws ESException
	{
		config = new HashMap<String, Integer>();
		storage = new DataStorage();
		transfer = new DataTransfer(context);
		
		// Set up default config
		config.put(DATA_POLICY, TRANSFER_IMMEDIATE);
		config.put(FILE_STORAGE_QUOTA, 0); // TODO
		config.put(FILE_DELETION_POLICY, NEVER_DELETE);
	}
	
	
	public void setConfig(final String key, final Integer value) throws DataHandlerException
	{
		if (key.equals(DATA_POLICY) || key.equals(FILE_STORAGE_QUOTA) || key.equals(FILE_DELETION_POLICY))
		{
			config.put(key, value);
		}
		else throw new DataHandlerException(DataHandlerException.UNKNOWN_CONFIG);
	}
	
	public void logSensorData(final SensorData data)
	{
		
	}
	
	public void logError(final String error)
	{
		
	}
	
	public void logExtra(final String tag, final String data)
	{
		
	}
}
