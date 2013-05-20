package com.ubhave.datahandler.config;

import java.util.HashMap;
import java.util.HashSet;

public class FileSyncConfig
{
	/*
	 * Config Keys
	 */
	public final static String WAIT_FOR_WIFI_INTERVAL_MILLIS = "FileWaitForWifiInterval";
	public final static String TRANSFER_ALARM_INTERVAL = "FileTransferAlarmInterval";
	
	/*
	 * Config Default Values
	 */
	public final static String DEFAULT_REQUEST_TYPE_PARAM = "esdmrequest";
	public final static String DEFAULT_REQUEST_DATE_MODIFIED = "date_modified";
	public final static String DEFAULT_REQUEST_GET_FILE = "get_file";
	public final static String DEFAULT_RESPONSE_DATE_MODIFIED = DEFAULT_REQUEST_DATE_MODIFIED;
	
	public final static long DEFAULT_WIFI_SYNC_LIMIT = 1000 * 60 * 60 * 24;
	public final static long DEFAULT_SYNC_FREQUENCY = 1000 * 60 * 60 * 12;
	
	public static HashSet<String> validKeys()
	{
		HashSet<String> validKeys = new HashSet<String>();
		validKeys.add(WAIT_FOR_WIFI_INTERVAL_MILLIS);
		validKeys.add(TRANSFER_ALARM_INTERVAL);
		return validKeys;
	}

	public static HashMap<String, Object> defaultValues()
	{
		HashMap<String, Object> defaults = new HashMap<String, Object>();
		defaults.put(WAIT_FOR_WIFI_INTERVAL_MILLIS, DEFAULT_WIFI_SYNC_LIMIT);
		defaults.put(TRANSFER_ALARM_INTERVAL, DEFAULT_SYNC_FREQUENCY);
		return defaults;
	}
}
