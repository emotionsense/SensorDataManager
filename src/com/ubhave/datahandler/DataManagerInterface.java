package com.ubhave.datahandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import com.ubhave.sensormanager.ESException;
import com.ubhave.sensormanager.data.SensorData;

public interface DataManagerInterface
{
	/*
	 * Updating Data Manager config
	 */
	public void setConfig(final String key, final Object value) throws DataHandlerException;
	
	/*
	 * Logging/storing data
	 */
	public void logSensorData(final SensorData data) throws DataHandlerException;
	public void logError(final String error) throws DataHandlerException;
	public void logExtra(final String tag, final String data) throws DataHandlerException;
	
	/*
	 * Retrieving logged data
	 */
	public List<SensorData> getRecentSensorData(int sensorId, long startTimestamp) throws ESException, IOException;
	
	/*
	 * Uploading stored data
	 */
	public void transferStoredData();
	
	/*
	 * Downloading a file
	 */
	public int addRemoteToSyncList(final String url, final HashMap<String, String> queryParameters, final String filePath) throws DataHandlerException;
	public void removeFromSyncList(int id) throws DataHandlerException;
	public void syncUpdatedFiles();
}
