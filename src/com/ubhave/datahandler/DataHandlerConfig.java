package com.ubhave.datahandler;

import java.util.HashMap;
import java.util.HashSet;

public class DataHandlerConfig
{
	// Config Keys
	public final static String FILE_STORAGE_QUOTA = "quota";
	public final static String DATA_POST_TARGET_URL = "dataTargetURL";
	public final static String ERROR_POST_TARGET_URL = "errorTargetURL";
	public final static String EXTRA_POST_TARGET_URL = "extraTargetURL";
	public final static String DATA_FORMAT = "dataFormat";
	public final static String DATA_POLICY = "policy";
	public final static String FILE_DELETION_POLICY = "deletion";
	private final HashSet<String> validKeys;

	// Config Values
	public final static int TRANSFER_IMMEDIATE = 0;
	public final static int TRANSFER_ON_CONNECTION = 1;
	public final static int TRANSFER_ON_WIFI = 2;
	public final static int TRANFER_BULK_ON_INTERVAL = 3;
	public final static int STORE_ONLY = 4;

	public final static int NEVER_DELETE = 0;
	public final static int DELETE_OLDEST_FIRST = 1;
	public final static int DELETE_NEWEST_FIRST = 2;

	public final static int JSON_FORMAT = 0;
	public final static int CSV_FORMAT = 1;

	private static DataHandlerConfig instance;
	private final HashMap<String, Object> config;

	public static DataHandlerConfig getInstance()
	{
		if (instance == null)
		{
			instance = new DataHandlerConfig();
		}
		return instance;
	}

	public DataHandlerConfig()
	{
		config = new HashMap<String, Object>();

		validKeys = new HashSet<String>();
		validKeys.add(FILE_STORAGE_QUOTA);
		validKeys.add(DATA_POST_TARGET_URL);
		validKeys.add(ERROR_POST_TARGET_URL);
		validKeys.add(EXTRA_POST_TARGET_URL);
		validKeys.add(DATA_FORMAT);
		validKeys.add(DATA_POLICY);
		validKeys.add(FILE_DELETION_POLICY);

		// Set up default config
		config.put(DATA_POLICY, TRANSFER_IMMEDIATE);
		config.put(DATA_FORMAT, JSON_FORMAT);
		config.put(FILE_DELETION_POLICY, NEVER_DELETE);
	}

	public void setConfig(final String key, final Object value) throws DataHandlerException
	{
		if (validKeys.contains(key))
		{
			config.put(key, value);
		}
		else
		{
			throw new DataHandlerException(DataHandlerException.UNKNOWN_CONFIG);
		}
	}
	
	public Object get(final String key) throws DataHandlerException
	{
		if (validKeys.contains(key))
		{
			return config.get(key);
		}
		else
		{
			throw new DataHandlerException(DataHandlerException.UNKNOWN_CONFIG);
		}
	}
}
