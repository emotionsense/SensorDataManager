package com.ubhave.datastore;

import java.io.IOException;
import java.util.List;

import com.ubhave.dataformatter.DataFormatter;
import com.ubhave.datahandler.except.DataHandlerException;
import com.ubhave.datahandler.transfer.DataUploadCallback;
import com.ubhave.sensormanager.ESException;
import com.ubhave.sensormanager.data.SensorData;

public interface DataStorageInterface extends DataUploadCallback
{	
	/*
	 * Retrieving stored data
	 */
	public List<SensorData> getRecentSensorData(int sensorId, long startTimestamp) throws ESException, DataHandlerException, IOException;
	
	/*
	 * Write data
	 */
	public void logSensorData(final SensorData data, final DataFormatter formatter) throws DataHandlerException;
	public void logExtra(final String tag, final String data) throws DataHandlerException;
	
	/*
	 * Initiating an upload
	 */
	public boolean prepareDataForUpload() throws DataHandlerException;
}
