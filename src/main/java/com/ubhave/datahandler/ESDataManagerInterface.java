package com.ubhave.datahandler;

import com.ubhave.dataformatter.DataFormatter;
import com.ubhave.datahandler.except.DataHandlerException;
import com.ubhave.datahandler.transfer.DataUploadCallback;
import com.ubhave.sensormanager.ESException;
import com.ubhave.sensormanager.data.SensorData;

import java.io.IOException;
import java.util.List;

public interface ESDataManagerInterface
{
	/*
	 * Updating Data Manager config
	 */
	void setConfig(final String key, final Object value) throws DataHandlerException;
	Object getConfig(final String key)  throws DataHandlerException;
	
	/*
	 * Logging/storing data
	 */
	void logSensorData(final SensorData data) throws DataHandlerException;
    void logSensorData(final SensorData data, DataFormatter formatter) throws DataHandlerException;
	void logExtra(final String tag, final String data) throws DataHandlerException;
	
	/*
	 * Retrieving logged data
	 */
	List<SensorData> getRecentSensorData(int sensorId, long startTimestamp) throws DataHandlerException, ESException, IOException;
	
	/*
	 * Uploading stored data
	 */
	void postAllStoredData(final DataUploadCallback callback) throws DataHandlerException;
	
}
