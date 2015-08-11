package com.ubhave.dataformatter.json.pull;

import android.content.Context;

import com.ubhave.sensormanager.ESException;
import com.ubhave.sensormanager.process.AbstractProcessor;
import com.ubhave.sensormanager.process.pull.AbstractMotionProcessor;
import com.ubhave.sensormanager.process.pull.GyroscopeProcessor;
import com.ubhave.sensormanager.sensors.SensorUtils;

public class GyroscopeFormatter extends AbstractMotionFormatter
{
	public GyroscopeFormatter(final Context context)
	{
		super(context, SensorUtils.SENSOR_TYPE_GYROSCOPE);
	}

	@Override
	protected AbstractMotionProcessor getProcessor(boolean setRawData, boolean setProcessedData) throws ESException
	{
		return (GyroscopeProcessor) AbstractProcessor.getProcessor(applicationContext, SensorUtils.SENSOR_TYPE_GYROSCOPE, setRawData, setProcessedData);
	}
}
