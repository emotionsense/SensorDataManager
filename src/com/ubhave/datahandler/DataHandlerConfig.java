package com.ubhave.datahandler;

import java.util.HashMap;
import java.util.HashSet;

import android.os.Environment;

public class DataHandlerConfig
{
	// Config Keys
	public final static String DATA_POST_TARGET_URL = "dataTargetURL";
	public final static String ERROR_POST_TARGET_URL = "errorTargetURL";
	public final static String EXTRA_POST_TARGET_URL = "extraTargetURL";

	// Local storage dir
	public final static String PHONE_STORAGE_DIR = Environment.getExternalStorageDirectory().getAbsolutePath()
			+ "/ESDataStorage";
	public final static String SERVER_UPLOAD_DIR = PHONE_STORAGE_DIR + "/" + "to_be_uploaded";

	public final static String DATA_POLICY = "policy";
	public final static int STORE_ONLY = -1; // No transfer (store only)
	public final static int TRANSFER_IMMEDIATE = 0; // Post immediately (error
													// on no connection)
	public final static int TRANFER_BULK_ON_INTERVAL = 1; // Store and post on
															// interval
	public final static int TRANSFER_ON_CONNECTION = 2; // Store and post as
														// soon as phone is
														// connected
	public final static int TRANSFER_ON_WIFI = 3; // Store and post as soon as
													// phone has wifi connection

	public final static String DATA_FORMAT = "dataFormat";
	public final static int JSON_FORMAT = 0;
	public final static int CSV_FORMAT = 1;

	public final static String FILE_MAX_SIZE = "fileSize";
	public final static long DEFAULT_FILE_SIZE = 1024 * 1024; // 1 MB
	
	public final static long DEFAULT_FILE_DURATION = 5 * 60 * 60 * 1000; // 5 hours

	// TODO
	public final static String FILE_DELETION_POLICY = "deletion";
	public final static int NEVER_DELETE = 0;
	public final static int DELETE_OLDEST_FIRST = 1;
	public final static int DELETE_NEWEST_FIRST = 2;

	public final static String FILE_STORAGE_QUOTA = "quota";

	private static DataHandlerConfig instance;

	public static DataHandlerConfig getInstance()
	{
		if (instance == null)
		{
			instance = new DataHandlerConfig();
		}
		return instance;
	}

	private final HashSet<String> validKeys;
	private final HashMap<String, Object> config;

	public DataHandlerConfig()
	{
		config = new HashMap<String, Object>();

		validKeys = new HashSet<String>();
		validKeys.add(DATA_POST_TARGET_URL);
		validKeys.add(ERROR_POST_TARGET_URL);
		validKeys.add(EXTRA_POST_TARGET_URL);
		validKeys.add(DATA_FORMAT);
		validKeys.add(DATA_POLICY);

		validKeys.add(FILE_DELETION_POLICY);
		validKeys.add(FILE_MAX_SIZE);
		validKeys.add(FILE_STORAGE_QUOTA);

		// Set up default config
		config.put(DATA_POLICY, TRANFER_BULK_ON_INTERVAL);
		config.put(DATA_FORMAT, JSON_FORMAT);
		config.put(FILE_DELETION_POLICY, NEVER_DELETE);
		config.put(FILE_MAX_SIZE, DEFAULT_FILE_SIZE);
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

	public boolean containsConfig(final String key)
	{
		return config.containsKey(key);
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
