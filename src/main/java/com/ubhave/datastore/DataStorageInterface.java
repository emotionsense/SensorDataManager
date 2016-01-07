package com.ubhave.datastore;

import com.ubhave.dataformatter.DataFormatter;
import com.ubhave.datahandler.except.DataHandlerException;
import com.ubhave.datahandler.transfer.DataUploadCallback;
import com.ubhave.sensormanager.ESException;
import com.ubhave.sensormanager.data.SensorData;

import java.io.IOException;
import java.util.List;

public interface DataStorageInterface extends DataUploadCallback
{	
	/*
	 * Retrieving stored data
	 */
	List<SensorData> getRecentSensorData(int sensorId, long startTimestamp) throws ESException, DataHandlerException, IOException;
	
	/*
	 * Write data
	 */
	void logSensorData(final SensorData data, final DataFormatter formatter) throws DataHandlerException;
	void logExtra(final String tag, final String data) throws DataHandlerException;
	
	/*
	 * Initiating an upload
	 */
	boolean prepareDataForUpload() throws DataHandlerException;
}
