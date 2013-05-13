package com.ubhave.datahandler;

import java.io.IOException;
import java.util.List;

import com.ubhave.datahandler.sync.FileUpdatedListener;
import com.ubhave.datahandler.sync.SyncRequest;
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
	public int subscribeToRemoteFileUpdate(final String url, final String targetFile, FileUpdatedListener listener) throws DataHandlerException;
	public int subscribeToRemoteFileUpdate(final SyncRequest request, FileUpdatedListener listener) throws DataHandlerException;
	public void unsubscribeFromRemoteFileUpdate(final int key) throws DataHandlerException;
	public void attemptFileSync();
	
}
