package com.ubhave.datahandler.logdata;

import org.json.JSONException;
import org.json.JSONObject;

public class UserInteraction extends AbstractLogData
{
	public final static String TAG = "Interaction";
	private final String tag;
	
	public UserInteraction(final String tag, final String action, final String detail)
	{
		super(action, detail);
		this.tag = tag;
	}
	
	@Override
	public String getDataType()
	{
		return tag;
	}
	
	@Override
	public JSONObject format(final String userId, final String deviceId) throws NullPointerException, JSONException
	{
		JSONObject json = super.format(userId, deviceId);
		
		return json;
	}
}
