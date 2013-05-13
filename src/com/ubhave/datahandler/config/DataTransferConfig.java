package com.ubhave.datahandler.config;

import java.util.HashMap;
import java.util.HashSet;

public class DataTransferConfig
{
	/*
	 * Config Keys
	 */
	public final static String POST_DATA_URL = "dataTargetURL";
	public final static String POST_DATA_URL_PASSWD = "dataTargetURLPasswd";
	public final static String DATA_TRANSER_POLICY = "transferPolicy";

	/*
	 * Config Values
	 * Note: not implemented
	 */
//	public final static int NO_TRANSFER = -1; // No transfer (store only)
	public final static int TRANSFER_IMMEDIATE = 0; // Post immediately (error on no connection)
//	public final static int TRANFER_BULK_ON_INTERVAL = 1; // Store and post on interval
	public final static int TRANSFER_ON_CONNECTION = 2; // Store and post as soon as phone is connected
	public final static int TRANSFER_ON_WIFI = 3; // Store and post as soon as phone has wifi connection
	
	/*
	 * Default values
	 */
	public final static int DEFAULT_TRANFER_POLICY = TRANSFER_ON_CONNECTION;
	

	public static HashSet<String> validKeys()
	{
		HashSet<String> validKeys = new HashSet<String>();
		validKeys.add(POST_DATA_URL);
		validKeys.add(POST_DATA_URL_PASSWD);
		validKeys.add(DATA_TRANSER_POLICY);
		return validKeys;
	}
	
	public static HashMap<String, Object> defaultValues()
	{
		HashMap<String, Object> defaults = new HashMap<String, Object>();
		defaults.put(DATA_TRANSER_POLICY, DEFAULT_TRANFER_POLICY);
		return defaults;
	}
}
