package com.ubhave.datahandler.logdata;

import org.json.JSONException;
import org.json.JSONObject;

public class LogExtra extends AbstractLogData
{
	public final static String TAG = "Error";
	private final static String TAG_LOGGER = "logger";
	private final JSONObject json;
	
	public LogExtra(final JSONObject json)
	{
		super(null, null);
		this.json = json;
	}
	
	@Override
	public String getDataType()
	{
		return TAG;
	}
	
	@Override
	public JSONObject format(final String userId, final String deviceId) throws NullPointerException, JSONException
	{
		JSONObject loggerDetails = super.format(userId, deviceId);
		json.put(TAG_LOGGER, loggerDetails);
		return json;
	}
}
