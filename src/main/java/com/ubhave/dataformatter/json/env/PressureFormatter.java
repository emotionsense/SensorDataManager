package com.ubhave.dataformatter.json.env;

import android.content.Context;

import com.ubhave.sensormanager.config.SensorConfig;
import com.ubhave.sensormanager.data.env.AbstractEnvironmentData;
import com.ubhave.sensormanager.data.env.PressureData;
import com.ubhave.sensormanager.sensors.SensorUtils;

public class PressureFormatter extends AbstractEnvironmentFormatter
{
	private final static String PRESSURE = "pressure";

	public PressureFormatter(final Context context)
	{
		super(context, SensorUtils.SENSOR_TYPE_PRESSURE);
	}

	@Override
	protected String getMetric()
	{
		return PRESSURE;
	}

	@Override
	protected AbstractEnvironmentData getInstance(long timestamp, SensorConfig config)
	{
		return new PressureData(timestamp, config);
	}
}
