package com.ubhave.datahandler.config;

import java.util.HashMap;
import java.util.HashSet;

public class DataStorageConfig
{
	/*
	 * Config Keys
	 */
	public final static String LOCAL_STORAGE_ROOT_DIRECTORY_NAME = "localDir";
	public final static String LOCAL_STORAGE_UPLOAD_DIRECTORY_NAME = "uploadDirName";
	public final static String LOCAL_STORAGE_UPLOAD_DIRECTORY_PATH = "uploadDirPath"; // Note: not user defined
	public final static String LOCAL_STORAGE_DATA_FORMAT = "dataFormat";
	public final static String FILE_MAX_SIZE = "fileSize";
	public final static String FILE_LIFE_MILLIS = "fileDuration";

	/*
	 * Default values
	 */
	public final static String DEFAULT_UPLOAD_DIRECTORY_NAME = "to_be_uploaded";
	public final static long DEFAULT_FILE_SIZE_BYTES = 5 * 1024; // 5 MB
	public final static long DEFAULT_FILE_LIFE_MILLIS = 30 * 60 * 60 * 1000; // 30 hours
	public final static String DEFAULT_ROOT_DIRECTORY = "esdm_root";

	public static HashSet<String> validKeys()
	{
		HashSet<String> validKeys = new HashSet<String>();
		validKeys.add(LOCAL_STORAGE_ROOT_DIRECTORY_NAME);
		validKeys.add(LOCAL_STORAGE_UPLOAD_DIRECTORY_NAME);
		validKeys.add(LOCAL_STORAGE_UPLOAD_DIRECTORY_PATH);
		validKeys.add(FILE_LIFE_MILLIS);
		validKeys.add(FILE_MAX_SIZE);
		return validKeys;
	}

	public static HashMap<String, Object> defaultValues()
	{
		HashMap<String, Object> defaults = new HashMap<String, Object>();
		defaults.put(LOCAL_STORAGE_ROOT_DIRECTORY_NAME, DEFAULT_ROOT_DIRECTORY);
		defaults.put(LOCAL_STORAGE_UPLOAD_DIRECTORY_NAME, DEFAULT_UPLOAD_DIRECTORY_NAME);
		
		String absoluteDir = (String) defaults.get(DataStorageConfig.LOCAL_STORAGE_ROOT_DIRECTORY_NAME);
		String uploadDir = absoluteDir +"/"+ defaults.get(DataStorageConfig.LOCAL_STORAGE_UPLOAD_DIRECTORY_NAME);
		defaults.put(LOCAL_STORAGE_UPLOAD_DIRECTORY_PATH, uploadDir);
		
		defaults.put(FILE_MAX_SIZE, DEFAULT_FILE_SIZE_BYTES);
		defaults.put(FILE_LIFE_MILLIS, DEFAULT_FILE_LIFE_MILLIS);
		
		return defaults;
	}
}
