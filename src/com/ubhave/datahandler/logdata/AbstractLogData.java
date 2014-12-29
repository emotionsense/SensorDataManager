package com.ubhave.datahandler.logdata;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

public abstract class AbstractLogData
{
	private final static String TAG_USER_ID = "userId";
	private final static String TAG_DEVICE_ID = "deviceId";
	private final static String TAG_TIMESTAMP = "timestamp";
	private final static String TAG_LOCAL_TIME = "localTime";
	private final static String TAG_DATA_TYPE = "dataType";
	
	protected final static String TAG_DATA_TITLE = "dataTitle";
	protected final static String TAG_DATA_MESSAGE = "dataMessage";
	
	private final long logTime;
	private final String title;
	private final String message;
	
	public AbstractLogData()
	{
		this.logTime = System.currentTimeMillis();
		this.title = null;
		this.message = null;
	}
	
	public AbstractLogData(final String title, final String message)
	{
		this.logTime = System.currentTimeMillis();
		this.title = title;
		this.message = message;
	}
	
	private String localTime()
	{
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(logTime);
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss zZ", Locale.ENGLISH);
		return dateFormat.format(calendar.getTime());
	}
	
	public abstract String getDataType();
	
	public JSONObject format(final String userId, final String deviceId) throws NullPointerException, JSONException
	{
		JSONObject json = new JSONObject();
		if (userId == null && deviceId == null)
		{
			throw new NullPointerException("No user ids set.");
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
			if (title != null)
			{
				json.put(TAG_DATA_TITLE, title);
			}
			if (message != null)
			{
				json.put(TAG_DATA_MESSAGE, message);
			}
		}
		return json;
	}
}
