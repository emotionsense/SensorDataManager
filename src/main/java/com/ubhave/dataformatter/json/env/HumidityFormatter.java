package com.ubhave.dataformatter.json.env;

import android.content.Context;

import com.ubhave.sensormanager.config.SensorConfig;
import com.ubhave.sensormanager.data.env.AbstractEnvironmentData;
import com.ubhave.sensormanager.data.env.HumidityData;
import com.ubhave.sensormanager.sensors.SensorUtils;

public class HumidityFormatter extends AbstractEnvironmentFormatter
{
	private final static String HUMIDITY = "humidity";

	public HumidityFormatter(final Context context)
	{
		super(context, SensorUtils.SENSOR_TYPE_HUMIDITY);
	}

	@Override
	protected String getMetric()
	{
		return HUMIDITY;
	}

	@Override
	protected AbstractEnvironmentData getInstance(long timestamp, SensorConfig config)
	{
		return new HumidityData(timestamp, config);
	}
}
