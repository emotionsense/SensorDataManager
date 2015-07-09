package com.ubhave.datastore.db;

import java.util.List;

import org.json.JSONObject;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.ubhave.dataformatter.json.JSONFormatter;
import com.ubhave.datahandler.config.DataHandlerConfig;
import com.ubhave.sensormanager.data.SensorData;

public class UnencryptedDataTable extends AbstractDataTable
{	
	public UnencryptedDataTable(final String tableName)
	{
		super(tableName);
	}
	
	public void createTable(final SQLiteDatabase database)
	{
		Log.d(DatabaseStorage.TAG, tableName+ ": execSQL to create table.");
		database.execSQL(getCreateTableQuery());
	}
	
	public void add(final SQLiteDatabase database, final long entryTime, final String data) throws Exception
	{
		ContentValues content = super.getContentValues(entryTime, data);
		long rowId = database.insert(tableName, null, content);
		if (rowId == -1)
		{
			throw new Exception("Data Not Inserted");
		}
		if (DataHandlerConfig.shouldLog())
		{
			Log.d(DatabaseStorage.TAG, tableName+ " inserted into row: "+rowId+" at: "+entryTime);
		}
	}
	
	public List<JSONObject> getUnsyncedData(final SQLiteDatabase database, final long timeLimit)
	{
		Cursor cursor = database.query(tableName, new String[]{dataKey}, UNSYNCED_AND_OLDER_THAN, new String[]{""+UNSYNCED, ""+timeLimit}, null, null, null);
		return formatCursorToJSON(cursor);
	}
	
	public void setSynced(final SQLiteDatabase database, final long timeLimit)
	{
		if (DataHandlerConfig.shouldLog())
		{
			Log.d(DatabaseStorage.TAG, "Setting "+tableName+" to synced.");
		}
		
		ContentValues content = super.getSyncedContentValues();
		database.update(tableName, content, UNSYNCED_AND_OLDER_THAN, new String[]{""+UNSYNCED, ""+timeLimit});
		int numRows = database.delete(tableName, UNSYNCED_WHERE, new String[]{""+SYNCED});
		if (DataHandlerConfig.shouldLog())
		{
			Log.d(DatabaseStorage.TAG, "Deleted "+numRows+" synced rows from "+tableName);
		}
	}
	
	public List<SensorData> getRecentData(final SQLiteDatabase database, final JSONFormatter formatter, final long timeLimit)
	{
		Cursor cursor = database.query(tableName, new String[]{dataKey}, TIME_GREATER_THAN, new String[]{""+timeLimit}, null, null, null);
		return formatToSensorData(formatter, cursor);
	}
}
