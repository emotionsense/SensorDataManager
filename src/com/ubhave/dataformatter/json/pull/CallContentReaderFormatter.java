package com.ubhave.dataformatter.json.pull;

import android.content.Context;

import com.ubhave.sensormanager.sensors.SensorUtils;

public class CallContentReaderFormatter extends AbstractContentReaderFormatter
{
	public CallContentReaderFormatter(final Context context)
	{
		super(context, SensorUtils.SENSOR_TYPE_CALL_CONTENT_READER);
	}
}