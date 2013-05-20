package com.ubhave.dataformatter.json.pull;

import android.content.Context;

import com.ubhave.sensormanager.sensors.SensorUtils;

public class SmsContentReaderFormatter extends AbstractContentReaderFormatter
{
	public SmsContentReaderFormatter(final Context context)
	{
		super(context, SensorUtils.SENSOR_TYPE_SMS_CONTENT_READER);
	}
}