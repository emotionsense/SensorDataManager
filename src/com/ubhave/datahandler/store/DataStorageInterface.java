package com.ubhave.datahandler.store;

import java.io.IOException;
import java.util.List;

import com.ubhave.dataformatter.DataFormatter;
import com.ubhave.datahandler.except.DataHandlerException;
import com.ubhave.sensormanager.ESException;
import com.ubhave.sensormanager.data.SensorData;

public interface DataStorageInterface
{
	/*
	 * Retrieving stored data
	 */
	public List<SensorData> getRecentSensorData(int sensorId, long startTimestamp) throws ESException, IOException;
	
	/*
	 * Write data
	 */
	public void logSensorData(final SensorData data, final DataFormatter formatter) throws DataHandlerException;
	public void logError(final String error) throws DataHandlerException;
	public void logExtra(final String tag, final String data) throws DataHandlerException;
	
	/*
	 * Initiating an upload
	 */
	public void moveArchivedFilesForUpload();
}
