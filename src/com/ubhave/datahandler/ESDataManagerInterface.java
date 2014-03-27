package com.ubhave.datahandler;

import java.io.IOException;
import java.util.List;

import com.ubhave.dataformatter.DataFormatter;
import com.ubhave.datahandler.except.DataHandlerException;
import com.ubhave.sensormanager.ESException;
import com.ubhave.sensormanager.data.SensorData;

public interface ESDataManagerInterface
{
	/*
	 * Updating Data Manager config
	 */
	public void setConfig(final String key, final Object value) throws DataHandlerException;
	public Object getConfig(final String key)  throws DataHandlerException;
	
	/*
	 * Logging/storing data
	 */
	public void logSensorData(final SensorData data) throws DataHandlerException;
	public void logSensorData(final SensorData data, DataFormatter formatter) throws DataHandlerException;
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
	public void postAllStoredData() throws DataHandlerException;
	
}
