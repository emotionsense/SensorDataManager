package com.ubhave.datahandler.logdata;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import com.ubhave.datahandler.except.DataHandlerException;

public abstract class AbstractLogData
{
	public final static String TAG_INTERACTION = "Interaction";
	public final static String TAG_ERROR = "Error";
	
	private final static String TAG_USER_ID = "userid";
	private final static String TAG_DEVICE_ID = "deviceid";
	
	private final static String TAG_TIMESTAMP = "timestamp";
	private final static String TAG_LOCAL_TIME = "localTime";
	private final static String TAG_DATA_TYPE = "dataType";
	
	private final long logTime;
	
	public AbstractLogData()
	{
		this.logTime = System.currentTimeMillis();
	}
	
	private String localTime()
	{
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(logTime);
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss zZ", Locale.ENGLISH);
		return dateFormat.format(calendar.getTime());
	}
	
	public abstract String getDataType();
	
	public JSONObject format(final String userId, final String deviceId) throws DataHandlerException, JSONException
	{
		JSONObject json = new JSONObject();
		if (userId == null && deviceId == null)
		{
			throw new DataHandlerException(DataHandlerException.MISSING_REQUIRED_DATA);
		}
		else
		{
			json.put(TAG_TIMESTAMP, logTime);
			json.put(TAG_LOCAL_TIME, localTime());
			json.put(TAG_DATA_TYPE, getDataType());
			if (userId != null)
			{
				json.put(TAG_USER_ID, userId);
			}
			if (deviceId != null)
			{
				json.put(TAG_DEVICE_ID, deviceId);
			}
		}
		return json;
	}
}
