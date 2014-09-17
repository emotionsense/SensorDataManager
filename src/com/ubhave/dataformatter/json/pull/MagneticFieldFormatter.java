package com.ubhave.dataformatter.json.pull;

import android.content.Context;

import com.ubhave.sensormanager.ESException;
import com.ubhave.sensormanager.process.AbstractProcessor;
import com.ubhave.sensormanager.process.pull.AbstractMotionProcessor;
import com.ubhave.sensormanager.process.pull.MagneticFieldProcessor;
import com.ubhave.sensormanager.sensors.SensorUtils;

public class MagneticFieldFormatter extends AbstractMotionFormatter
{
	public MagneticFieldFormatter(final Context context)
	{
		super(context, SensorUtils.SENSOR_TYPE_MAGNETIC_FIELD);
	}

	@Override
	protected AbstractMotionProcessor getProcessor(boolean setRawData, boolean setProcessedData) throws ESException
	{
		return (MagneticFieldProcessor) AbstractProcessor.getProcessor(applicationContext, SensorUtils.SENSOR_TYPE_MAGNETIC_FIELD, setRawData, setProcessedData);
	}
}
