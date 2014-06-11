package com.ubhave.dataformatter.json.push;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.ubhave.dataformatter.json.PushSensorJSONFormatter;
import com.ubhave.sensormanager.config.SensorConfig;
import com.ubhave.sensormanager.data.SensorData;
import com.ubhave.sensormanager.data.pushsensor.LightData;
import com.ubhave.sensormanager.sensors.SensorUtils;

public class LightFormatter extends PushSensorJSONFormatter
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
        LightData lightData = (LightData) data;
		json.put(LIGHT, lightData.getLight());
		json.put(MAX_RANGE, lightData.getMaxRange());
	}
	
	@Override
	public SensorData toSensorData(String jsonString)
	{
		JSONObject jsonData = super.parseData(jsonString);
		if (jsonData != null)
		{
			long recvTimestamp = super.parseTimeStamp(jsonData);
			SensorConfig sensorConfig = super.getGenericConfig(jsonData);
			LightData data = new LightData(recvTimestamp, sensorConfig);
			try
			{
				data.setLight(((Double) jsonData.get(LIGHT)).floatValue());
				data.setMaxRange(((Double) jsonData.get(MAX_RANGE)).floatValue());
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			return data;
		}
		else return null;
	}
}
