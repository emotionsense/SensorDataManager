package com.ubhave.dataformatter.json.pull;

import android.content.Context;

import com.ubhave.sensormanager.config.SensorConfig;
import com.ubhave.sensormanager.data.pull.AbstractContentReaderEntry;
import com.ubhave.sensormanager.data.pull.AbstractContentReaderListData;
import com.ubhave.sensormanager.data.pull.CallContentListData;
import com.ubhave.sensormanager.data.pull.CallContentReaderEntry;
import com.ubhave.sensormanager.sensors.SensorUtils;

public class CallContentReaderFormatter extends AbstractContentReaderFormatter
{
	public CallContentReaderFormatter(final Context context)
	{
		super(context, SensorUtils.SENSOR_TYPE_CALL_CONTENT_READER);
	}
	
	@Override
	protected AbstractContentReaderListData getData(long senseStartTime, SensorConfig config)
	{
		return new CallContentListData(senseStartTime, config);
	}
	
	@Override
	protected AbstractContentReaderEntry getNewEntry()
	{
		return new CallContentReaderEntry();
	}
}