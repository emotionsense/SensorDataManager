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
	private final static String TAG = "UnencryptedDataTable";
	
	public UnencryptedDataTable(final String tableName)
	{
		super(tableName);
	}
	
	public void createTable(final SQLiteDatabase database)
	{
		database.execSQL(getCreateTableQuery());
	}
	
	public void add(final SQLiteDatabase database, final long entryTime, final String data) throws Exception
	{
		ContentValues content = super.getContentValues(entryTime, data);
		long rowId = database.insert(this.tableName, null, content);
		if (rowId == -1)
		{
			throw new Exception("Data Not Inserted");
		}
		if (DataHandlerConfig.shouldLog())
		{
			Log.d(TAG, tableName+ " inserted into row: "+rowId);
		}
	}
	
	public List<JSONObject> getUnsyncedData(final SQLiteDatabase database, final long timeLimit)
	{
		Cursor cursor = database.query(tableName, new String[]{dataKey}, syncedWithServer+" == ? AND "+timeStampKey+" > ?", new String[]{UNSYNCED, ""+timeLimit}, null, null, null);
		return super.getUnsyncedData(cursor);
	}
	
	public void setSynced(final SQLiteDatabase database)
	{
		ContentValues content = super.getSyncedContentValues();
		database.update(tableName, content, null, null);
		int numRows = database.delete(tableName, syncedWithServer+" == ?", new String[]{SYNCED});
		if (DataHandlerConfig.shouldLog())
		{
			Log.d(TAG, "Deleted "+numRows+" synced rows from "+tableName);
		}
	}
	
	public List<SensorData> getRecentData(final SQLiteDatabase database, final JSONFormatter formatter, final long timeLimit)
	{
		Cursor cursor = database.query(tableName, new String[]{dataKey}, timeStampKey+" > ?", new String[]{""+timeLimit}, null, null, null);
		return super.getRecentData(formatter, cursor);
	}
}
