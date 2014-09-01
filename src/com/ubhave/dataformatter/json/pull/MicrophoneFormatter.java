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

package com.ubhave.dataformatter.json.pull;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.ubhave.dataformatter.json.PullSensorJSONFormatter;
import com.ubhave.sensormanager.ESException;
import com.ubhave.sensormanager.config.SensorConfig;
import com.ubhave.sensormanager.config.pull.PullSensorConfig;
import com.ubhave.sensormanager.data.SensorData;
import com.ubhave.sensormanager.data.pull.MicrophoneData;
import com.ubhave.sensormanager.process.AbstractProcessor;
import com.ubhave.sensormanager.process.pull.MicrophoneProcessor;
import com.ubhave.sensormanager.sensors.SensorUtils;

public class MicrophoneFormatter extends PullSensorJSONFormatter
{	
	private final static String SAMPLE_LENGTH = "sampleLengthMillis";
	private final static String AMPLITUDE = "amplitude";
	private final static String MEDIA_FILE_PATH = "media_file";
	private final static String READING_TIMESTAMPS = "sensorTimeStamps";
	
	public MicrophoneFormatter(final Context context)
	{
		super(context, SensorUtils.SENSOR_TYPE_MICROPHONE);
	}
	
	@Override
	protected void addSensorSpecificData(JSONObject json, SensorData data) throws JSONException
	{
		MicrophoneData micData = (MicrophoneData) data;
		int[] values = micData.getAmplitudeArray();
		JSONArray valueArray = new JSONArray();
		for (int i=0; i<values.length; i++)
		{
			valueArray.put(values[i]);
		}
		json.put(AMPLITUDE, valueArray);
		
		long[] tsValues = micData.getTimestampArray();
		JSONArray tsArray = new JSONArray();
		for (int i=0; i<values.length; i++)
		{
			tsArray.put(tsValues[i]);
		}
		json.put(READING_TIMESTAMPS, tsArray);
		
		String mediaFile = micData.getMediaFilePath();
		if (mediaFile != null)
		{
			json.put(MEDIA_FILE_PATH, mediaFile);
		}
	}

	@Override
	protected void addSensorSpecificConfig(JSONObject json, SensorConfig config) throws JSONException
	{
		json.put(SAMPLE_LENGTH, config.getParameter(PullSensorConfig.SENSE_WINDOW_LENGTH_MILLIS));
	}
	
	@Override
	public SensorData toSensorData(String jsonString)
	{
		JSONObject jsonData = super.parseData(jsonString);
		long senseStartTimestamp = super.parseTimeStamp(jsonData);
		SensorConfig sensorConfig = super.getGenericConfig(jsonData);
		
		boolean setRawData = true;
		boolean setProcessedData = false;
		
		int[] ampValues = null;
		long[] tsValues = null;
		String mediaFilePath = null;
		
		try
		{
			ArrayList<Integer> amplitudes = getJSONArray(jsonData, AMPLITUDE, Integer.class);
			ArrayList<Long> timestamps = getJSONArray(jsonData, READING_TIMESTAMPS, Long.class);
			
			ampValues = new int[amplitudes.size()];
			tsValues = new long[timestamps.size()];
			
			for (int i=0; i<amplitudes.size(); i++)
			{
				ampValues[i] = Long.valueOf(amplitudes.get(i)).intValue();
				tsValues[i] = timestamps.get(i);
			}
			
			if (jsonData.has(MEDIA_FILE_PATH))
			{
				mediaFilePath = jsonData.getString(MEDIA_FILE_PATH);
			}
		}
		catch (Exception e)
		{
			setRawData = false;
		}
		
		try
		{
			MicrophoneProcessor processor = (MicrophoneProcessor) AbstractProcessor.getProcessor(applicationContext, sensorType, setRawData, setProcessedData);
			return processor.process(senseStartTimestamp, ampValues, tsValues, mediaFilePath, sensorConfig);
		}
		catch (ESException e)
		{
			e.printStackTrace();
			return null;
		}
	}

}
