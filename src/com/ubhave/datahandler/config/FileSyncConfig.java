package com.ubhave.datahandler.config;

import java.util.HashMap;
import java.util.HashSet;


public class FileSyncConfig
{
	/*
	 * Config Keys
	 */
	public final static String REQUEST_TYPE_PARAM_NAME = "requestType";
	public final static String REQUEST_DATE_MODIFIED_VALUE = "requestDateModified";
	public final static String REQUEST_GET_FILE_VALUE = "requestGetFile";
	
	public final static String RESPONSE_DATE_MODIFIED_KEY = "responseDateModified";
	public final static String SYNC_FREQUENCY = "syncFrequency";
	
	// Sync Policy Unimplemented
//	public final static String SYNC_CONNECTION_TYPE = "syncConnectionType";

	/*
	 * Config Values
	 */
	public final static String DEFAULT_REQUEST_TYPE_PARAM = "esdmrequest";
	public final static String DEFAULT_REQUEST_DATE_MODIFIED = "date_modified";
	public final static String DEFAULT_REQUEST_GET_FILE = "get_file";
	public final static String DEFAULT_RESPONSE_DATE_MODIFIED = DEFAULT_REQUEST_DATE_MODIFIED;
	public final static long DEFAULT_SYNC_FREQUENCY = 1000 * 60 * 60 * 24; // daily
	
	public static HashSet<String> validKeys()
	{
		HashSet<String> validKeys = new HashSet<String>();
		validKeys.add(REQUEST_TYPE_PARAM_NAME);
		validKeys.add(REQUEST_DATE_MODIFIED_VALUE);
		validKeys.add(REQUEST_GET_FILE_VALUE);
		validKeys.add(RESPONSE_DATE_MODIFIED_KEY);
		validKeys.add(SYNC_FREQUENCY);
		return validKeys;
	}
	
	public static HashMap<String, Object> defaultValues()
	{
		HashMap<String, Object> defaults = new HashMap<String, Object>();
		defaults.put(REQUEST_TYPE_PARAM_NAME, DEFAULT_REQUEST_TYPE_PARAM);
		defaults.put(REQUEST_DATE_MODIFIED_VALUE, DEFAULT_REQUEST_DATE_MODIFIED);
		defaults.put(REQUEST_GET_FILE_VALUE, DEFAULT_REQUEST_GET_FILE);
		defaults.put(RESPONSE_DATE_MODIFIED_KEY, DEFAULT_RESPONSE_DATE_MODIFIED);
		defaults.put(SYNC_FREQUENCY, DEFAULT_SYNC_FREQUENCY);
		return defaults;
	}
	
}
