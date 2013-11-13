package com.ubhave.dataformatter.json.pull;

import android.content.Context;

import com.ubhave.sensormanager.config.SensorConfig;
import com.ubhave.sensormanager.data.pullsensor.AbstractContentReaderEntry;
import com.ubhave.sensormanager.data.pullsensor.AbstractContentReaderListData;
import com.ubhave.sensormanager.data.pullsensor.SMSContentListData;
import com.ubhave.sensormanager.data.pullsensor.SMSContentReaderEntry;
import com.ubhave.sensormanager.sensors.SensorUtils;

public class SmsContentReaderFormatter extends AbstractContentReaderFormatter
{
	public SmsContentReaderFormatter(final Context context)
	{
		super(context, SensorUtils.SENSOR_TYPE_SMS_CONTENT_READER);
	}
	
	@Override
	protected AbstractContentReaderListData getData(long senseStartTime, SensorConfig config)
	{
		return new SMSContentListData(senseStartTime, config);
	}
	
	@Override
	protected AbstractContentReaderEntry getNewEntry()
	{
		return new SMSContentReaderEntry();
	}
}