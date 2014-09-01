package com.ubhave.dataformatter.json.env;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.ubhave.sensormanager.config.SensorConfig;
import com.ubhave.sensormanager.data.SensorData;
import com.ubhave.sensormanager.data.env.AbstractEnvironmentData;
import com.ubhave.sensormanager.data.env.LightData;
import com.ubhave.sensormanager.sensors.SensorUtils;

public class LightFormatter extends AbstractEnvironmentFormatter
{
	private final static String LIGHT = "light";
	private final static String MAX_RANGE = "maxRange";

	public LightFormatter(final Context context)
	{
		super(context, SensorUtils.SENSOR_TYPE_LIGHT);
	}

	@Override
	protected void addSensorSpecificData(JSONObject json, SensorData data) throws JSONException
	{
		super.addSensorSpecificData(json, data);
		LightData lightData = (LightData) data;
		json.put(MAX_RANGE, lightData.getMaxRange());
	}

	@Override
	protected String getMetric()
	{
		return LIGHT;
	}

	@Override
	protected AbstractEnvironmentData getInstance(long timestamp, SensorConfig config)
	{
		return new LightData(timestamp, config);
	}

	@Override
	public SensorData toSensorData(String jsonString)
	{
		LightData data = (LightData) super.toSensorData(jsonString);
		try
		{
			JSONObject jsonData = super.parseData(jsonString);
			data.setMaxRange(((Double) jsonData.get(MAX_RANGE)).floatValue());
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
		return data;
	}
}
