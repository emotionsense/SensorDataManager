package com.ubhave.datahandler.logdata;

import org.json.JSONException;
import org.json.JSONObject;

public class ApplicationError extends AbstractLogData
{
	public final static String TAG = "Error";
	private final static String TAG_APP_VERSION = "applicationVersion";
	
	private final int appVersion;
	
	public ApplicationError(final int appVersion, final String action, final String detail)
	{
		super(action, detail);
		this.appVersion = appVersion;
	}
	
	@Override
	public String getDataType()
	{
		return TAG;
	}
	
	@Override
	public JSONObject format(final String userId, final String deviceId) throws NullPointerException, JSONException
	{
		JSONObject json = super.format(userId, deviceId);
		json.put(TAG_APP_VERSION, appVersion);
		return json;
	}
}
