package com.ubhave.datahandler.loggertypes;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.Manifest;
import android.content.Context;

import com.ubhave.datahandler.config.DataTransferConfig;

public abstract class AbstractTransferLogger extends AbstractDataLogger
{
	protected AbstractTransferLogger(Context context)
	{
		super(context);
	}
	
	@Override
	protected ArrayList<String> getPermissions()
	{
		ArrayList<String> permissions = super.getPermissions();
		permissions.add(Manifest.permission.INTERNET);
		return permissions;
	}

	@Override
	protected void configureDataStorage()
	{
		super.configureDataStorage();
		try
		{
			dataManager.setConfig(DataTransferConfig.POST_DATA_URL, getDataPostURL());
			dataManager.setConfig(DataTransferConfig.POST_RESPONSE_ON_SUCCESS, getSuccessfulPostResponse());
			HashMap<String, String> params = getPostParameters();
			if (params != null)
			{
				dataManager.setConfig(DataTransferConfig.POST_PARAMETERS, toJSON(params));
			}
		}
		catch (Exception e)
		{
			dataManager = null;
			e.printStackTrace();
		}
	}
	
	private JSONObject toJSON(HashMap<String, String> map) throws JSONException
	{
		JSONObject json = new JSONObject();
		for (String key : map.keySet())
		{
			String value = map.get(key);
			if (value != null)
			{
				json.put(key, value);
			}
		}
		return json;
	}

	protected abstract String getDataPostURL();
	
	protected abstract String getSuccessfulPostResponse();
	
	protected abstract HashMap<String, String> getPostParameters();

}
