package com.ubhave.dataformatter.json.pull;

import android.content.Context;

import com.ubhave.sensormanager.config.SensorConfig;
import com.ubhave.sensormanager.data.pull.AbstractContentReaderEntry;
import com.ubhave.sensormanager.data.pull.AbstractContentReaderListData;
import com.ubhave.sensormanager.data.pull.SMSContentListData;
import com.ubhave.sensormanager.data.pull.SMSContentReaderEntry;
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