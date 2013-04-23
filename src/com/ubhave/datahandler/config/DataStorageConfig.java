package com.ubhave.datahandler.config;

import java.util.HashMap;
import java.util.HashSet;

public class DataStorageConfig
{
	/*
	 * Config Keys
	 */
	public final static String LOCAL_STORAGE_ROOT_DIRECTORY_NAME = "localDir";
	public final static String LOCAL_STORAGE_UPLOAD_DIRECTORY_PATH = "uploadDirPath";
	public final static String LOCAL_STORAGE_UPLOAD_DIRECTORY_NAME = "uploadDirName";
	public final static String LOCAL_STORAGE_DATA_FORMAT = "dataFormat";
	public final static String FILE_MAX_SIZE = "fileSize";
	public final static String FILE_LIFE_MILLIS = "fileDuration";

	/*
	 * Config Values
	 */
//	public final static int JSON_FORMAT = 0;
//	public final static int CSV_FORMAT = 1;
	
	/*
	 * Default values
	 */
	public final static String DEFAULT_UPLOAD_DIRECTORY_NAME = "to_be_uploaded";
	//public final static int DEFAULT_DATA_FORMAT = JSON_FORMAT;
	public final static long DEFAULT_FILE_SIZE = 1024 * 1024; // 1 MB
	public final static long DEFAULT_FILE_LIFE_MILLIS = 30 * 60 * 60 * 1000; // 30 hours

	/*
	 * Unimplemented
	 */
//	public final static String FILE_DELETION_POLICY = "deletion";
//	public final static int NEVER_DELETE = 0;
//	public final static int DELETE_OLDEST_FIRST = 1;
//	public final static int DELETE_NEWEST_FIRST = 2;
//	public final static String FILE_STORAGE_QUOTA = "quota";

	public static HashSet<String> validKeys()
	{
		HashSet<String> validKeys = new HashSet<String>();
		validKeys.add(LOCAL_STORAGE_ROOT_DIRECTORY_NAME);
		validKeys.add(LOCAL_STORAGE_UPLOAD_DIRECTORY_PATH);
		validKeys.add(LOCAL_STORAGE_UPLOAD_DIRECTORY_NAME);
		return validKeys;
	}
	
	public static HashMap<String, Object> defaultValues()
	{
		HashMap<String, Object> defaults = new HashMap<String, Object>();
		defaults.put(LOCAL_STORAGE_UPLOAD_DIRECTORY_NAME, DEFAULT_UPLOAD_DIRECTORY_NAME);
		//defaults.put(LOCAL_STORAGE_DATA_FORMAT, DEFAULT_DATA_FORMAT);
		defaults.put(FILE_MAX_SIZE, DEFAULT_FILE_SIZE);
		defaults.put(FILE_LIFE_MILLIS, DEFAULT_FILE_LIFE_MILLIS);
		return defaults;
	}
}
