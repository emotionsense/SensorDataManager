package com.ubhave.dataformatter.json.env;

import android.content.Context;

import com.ubhave.sensormanager.config.SensorConfig;
import com.ubhave.sensormanager.data.env.AbstractEnvironmentData;
import com.ubhave.sensormanager.data.env.AmbientTemperatureData;
import com.ubhave.sensormanager.sensors.SensorUtils;

public class AmbientTemperatureFormatter extends AbstractEnvironmentFormatter
{
	private final static String TEMPERATURE = "temperature";

	public AmbientTemperatureFormatter(final Context context)
	{
		super(context, SensorUtils.SENSOR_TYPE_AMBIENT_TEMPERATURE);
	}

	@Override
	protected String getMetric()
	{
		return TEMPERATURE;
	}

	@Override
	protected AbstractEnvironmentData getInstance(long timestamp, SensorConfig config)
	{
		return new AmbientTemperatureData(timestamp, config);
	}
}
