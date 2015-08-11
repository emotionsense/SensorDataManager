package com.ubhave.dataformatter.json.log;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.ubhave.dataformatter.json.JSONFormatter;
import com.ubhave.datahandler.except.DataHandlerException;
import com.ubhave.sensormanager.config.SensorConfig;
import com.ubhave.sensormanager.data.SensorData;
import com.ubhave.sensormanager.data.log.InteractionData;
import com.ubhave.sensormanager.sensors.SensorUtils;

public class InteractionFormatter extends JSONFormatter
{
	private HashSet<String> ignoredFields;

	public InteractionFormatter(final Context c)
	{
		super(c, SensorUtils.SENSOR_TYPE_INTERACTION);
		ignoredFields = new HashSet<String>();
		ignoredFields.addAll(Arrays.asList(new String[] { USER_ID, DEVICE_ID, SENSOR_TYPE, SENSE_TIME, SENSE_TIME_MILLIS }));
	}

	@Override
	protected void addSensorSpecificData(final JSONObject json, final SensorData data) throws JSONException, DataHandlerException
	{
		InteractionData userInteraction = (InteractionData) data;
		HashMap<String, String> values = userInteraction.getValues();
		for (String key : values.keySet())
		{
			json.put(key, values.get(key));
		}
	}

	@Override
	protected void addGenericConfig(JSONObject json, SensorConfig config) throws JSONException
	{
	}

	@Override
	protected void addSensorSpecificConfig(JSONObject json, SensorConfig config) throws JSONException
	{
	}

	@Override
	protected SensorConfig getGenericConfig(final JSONObject json)
	{
		return null;
	}

	@Override
	public SensorData toSensorData(final String dataString)
	{
		JSONObject jsonData = parseData(dataString);
		if (jsonData != null)
		{
			try
			{
				long timestamp = jsonData.getLong(SENSE_TIME_MILLIS);
				HashMap<String, String> values = new HashMap<String, String>();
				Iterator<?> iterator = jsonData.keys();
				while (iterator.hasNext())
				{
					String key = (String) iterator.next();
					if (!ignoredFields.contains(key))
					{
						values.put(key, getString(key, jsonData));
					}
				}
				return new InteractionData(timestamp, values);
			}
			catch (JSONException e)
			{
				e.printStackTrace();
			}
		}
		return null;
	}
}
