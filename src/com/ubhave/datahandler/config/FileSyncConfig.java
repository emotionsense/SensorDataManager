package com.ubhave.datahandler.config;



public class FileSyncConfig
{
	/*
	 * Config Default Values
	 */
	public final static String DEFAULT_REQUEST_TYPE_PARAM = "esdmrequest";
	public final static String DEFAULT_REQUEST_DATE_MODIFIED = "date_modified";
	public final static String DEFAULT_REQUEST_GET_FILE = "get_file";
	public final static String DEFAULT_RESPONSE_DATE_MODIFIED = DEFAULT_REQUEST_DATE_MODIFIED;
	
	public final static long DEFAULT_WIFI_SYNC_LIMIT = 1000 * 60 * 60 * 24;
	public final static long DEFAULT_SYNC_FREQUENCY = 1000 * 60 * 12;
}
