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
import com.ubhave.sensormanager.data.push.SmsData;
import com.ubhave.sensormanager.sensors.SensorUtils;

public class SmsFormatter extends PushSensorJSONFormatter
{
	private final static String CONTENT_LENGTH = "contentLength";
	private final static String WORD_COUNT = "wordCount";
	private final static String MESSAGE_TYPE = "messageType";
	private final static String EVENT_TYPE = "eventType";
	private final static String ADDRESS = "address";
	
	public SmsFormatter(final Context context)
	{
		super(context, SensorUtils.SENSOR_TYPE_SMS);
	}
	
	@Override
	protected void addSensorSpecificData(JSONObject json, SensorData data) throws JSONException
	{
		SmsData smsData = (SmsData) data;
		json.put(CONTENT_LENGTH, smsData.getContentLength());
		json.put(WORD_COUNT, smsData.getNoOfWords());
		json.put(MESSAGE_TYPE, smsData.getMessageType());
		json.put(EVENT_TYPE, smsData.getEventType());
		json.put(ADDRESS, smsData.getAddress());
		// Future: set features
	}
	
	@Override
	public SensorData toSensorData(String jsonString)
	{
		JSONObject jsonData = super.parseData(jsonString);
		if (jsonData != null)
		{
			long recvTimestamp = super.parseTimeStamp(jsonData);
			SensorConfig sensorConfig = super.getGenericConfig(jsonData);
			SmsData data = new SmsData(recvTimestamp, sensorConfig);
			try
			{
				// Construct Raw Data
				int smsLength = ((Long) jsonData.get(CONTENT_LENGTH)).intValue();
				int noOfWords = ((Long) jsonData.get(WORD_COUNT)).intValue();
				String addr = (String) jsonData.get(ADDRESS);
				String messageType = (String) jsonData.get(MESSAGE_TYPE);
				String eventType = (String) jsonData.get(EVENT_TYPE);
				
				data.setContentLength(smsLength);
				data.setAddress(addr);
				data.setMessageType(messageType);
				data.setEventType(eventType);
				data.setNumberOfWords(noOfWords);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			
			// Future: construct feature data
			return data;
		}
		else return null;
	}
}
