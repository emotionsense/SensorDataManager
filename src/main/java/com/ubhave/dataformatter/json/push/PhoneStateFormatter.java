/* **************************************************
 Copyright (c) 2012, University of Cambridge
 Neal Lathia, neal.lathia@cl.cam.ac.uk

This library was developed as part of the EPSRC Ubhave (Ubiquitous and
Social Computing for Positive Behaviour Change) Project. For more
information, please visit http://www.emotionsense.org

Permission to use, copy, modify, and/or distribute this software for any
purpose with or without fee is hereby granted, provided that the above
copyright notice and this permission notice appear in all copies.

THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR
IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 ************************************************** */

package com.ubhave.dataformatter.json.push;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.ubhave.dataformatter.json.PushSensorJSONFormatter;
import com.ubhave.sensormanager.config.SensorConfig;
import com.ubhave.sensormanager.data.SensorData;
import com.ubhave.sensormanager.data.push.PhoneStateData;
import com.ubhave.sensormanager.sensors.SensorUtils;

public class PhoneStateFormatter extends PushSensorJSONFormatter
{
	private final static String EVENT_TYPE = "eventType";
	private final static String DATA = "data";
	private final static String NUMBER = "number";
	
	public PhoneStateFormatter(final Context context)
	{
		super(context, SensorUtils.SENSOR_TYPE_PHONE_STATE);
	}

	@Override
	protected void addSensorSpecificData(JSONObject json, SensorData data) throws JSONException
	{
		PhoneStateData phoneStateData = (PhoneStateData) data;
		json.put(EVENT_TYPE, phoneStateData.getEventType());
		json.put(DATA, phoneStateData.getData());
		json.put(NUMBER, phoneStateData.getNumber());
	}
	
	@Override
	public SensorData toSensorData(String jsonString)
	{
		JSONObject jsonData = super.parseData(jsonString);
		if (jsonData != null)
		{
			long dataReceivedTimestamp = super.parseTimeStamp(jsonData);
			SensorConfig sensorConfig = super.getGenericConfig(jsonData);
			PhoneStateData data = new PhoneStateData(dataReceivedTimestamp, sensorConfig);
			try
			{
				data.setEventType(((Long) jsonData.get(EVENT_TYPE)).intValue());
				data.setData((String) jsonData.get(DATA));
				data.setNumber((String) jsonData.get(NUMBER));
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			
			return data;
		}
		else return null;
	}
}
