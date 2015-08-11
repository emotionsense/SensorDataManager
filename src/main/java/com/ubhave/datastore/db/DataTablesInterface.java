package com.ubhave.datastore.db;

import java.util.List;
import java.util.Set;

import org.json.JSONObject;

import com.ubhave.dataformatter.json.JSONFormatter;
import com.ubhave.sensormanager.data.SensorData;

public interface DataTablesInterface
{
	public Set<String> getTableNames();

	public void writeData(final String tableName, final String data);

	public List<SensorData> getRecentSensorData(final String tableName, final JSONFormatter formatter, final long timeLimit);

	public List<JSONObject> getUnsyncedData(final String tableName, final long maxAge);

	public void setSynced(final String tableName, final long syncTime);
}
