package com.ubhave.dataformatter.json.env;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.ubhave.dataformatter.json.PushSensorJSONFormatter;
import com.ubhave.sensormanager.config.SensorConfig;
import com.ubhave.sensormanager.data.SensorData;
import com.ubhave.sensormanager.data.env.AbstractEnvironmentData;

public abstract class AbstractEnvironmentFormatter extends PushSensorJSONFormatter
{

	protected AbstractEnvironmentFormatter(final Context context, final int type)
	{
		super(context, type);
	}

	@Override
	protected void addSensorSpecificData(JSONObject json, SensorData data) throws JSONException
	{
		AbstractEnvironmentData pData = (AbstractEnvironmentData) data;
		json.put(getMetric(), pData.getValue());
	}
	
	protected abstract String getMetric();

	@Override
	public SensorData toSensorData(String jsonString)
	{
		JSONObject jsonData = super.parseData(jsonString);
		if (jsonData != null)
		{
			long recvTimestamp = super.parseTimeStamp(jsonData);
			SensorConfig sensorConfig = super.getGenericConfig(jsonData);
			AbstractEnvironmentData data = getInstance(recvTimestamp, sensorConfig);
			try
			{
				data.setValue(((Double) jsonData.get(getMetric())).floatValue());
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			return data;
		}
		else
		{
			return null;
		}
	}
	
	protected abstract AbstractEnvironmentData getInstance(long timestamp, SensorConfig config);
}
