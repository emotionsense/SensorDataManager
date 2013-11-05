package com.ubhave.dataformatter.json.pull;

import android.content.Context;

import com.ubhave.sensormanager.config.SensorConfig;
import com.ubhave.sensormanager.data.pullsensor.CallContentData;
import com.ubhave.sensormanager.data.pullsensor.ContentReaderData;
import com.ubhave.sensormanager.sensors.SensorUtils;

public class CallContentReaderFormatter extends AbstractContentReaderFormatter
{
	public CallContentReaderFormatter(final Context context)
	{
		super(context, SensorUtils.SENSOR_TYPE_CALL_CONTENT_READER);
	}
	
	@Override
	protected ContentReaderData getData(long senseStartTime, SensorConfig config)
	{
		return new CallContentData(senseStartTime, config);
	}
}