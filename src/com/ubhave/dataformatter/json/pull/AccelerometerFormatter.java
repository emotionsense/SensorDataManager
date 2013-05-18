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

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.ubhave.dataformatter.json.PullSensorJSONFormatter;
import com.ubhave.sensormanager.config.SensorConfig;
import com.ubhave.sensormanager.data.SensorData;
import com.ubhave.sensormanager.data.pullsensor.AccelerometerData;

public class AccelerometerFormatter extends PullSensorJSONFormatter
{

	private final static String X_AXIS = "xAxis";
	private final static String Y_AXIS = "yAxis";
	private final static String Z_AXIS = "zAxis";
	private final static String READING_TIMESTAMPS = "sensorTimeStamps";

	private final static String SAMPLE_LENGTH = "sampleLengthMillis";

	@SuppressWarnings("unchecked")
	@Override
	protected void addSensorSpecificData(JSONObject json, SensorData data)
	{
		AccelerometerData accelerometerData = (AccelerometerData) data;
		ArrayList<float[]> readings = accelerometerData.getSensorReadings();
		ArrayList<Long> timestamps = accelerometerData.getSensorReadingTimestamps();

		JSONArray xs = new JSONArray();
		JSONArray ys = new JSONArray();
		JSONArray zs = new JSONArray();

		for (int i=0; i<readings.size(); i++)
		{
			float[] sample = readings.get(i);
			xs.add(sample[0]);
			ys.add(sample[1]);
			zs.add(sample[2]);
		}
		
		JSONArray ts = new JSONArray();
		for (int i=0; i<timestamps.size(); i++)
		{
			ts.add(timestamps.get(i));
		}

		json.put(X_AXIS, xs);
		json.put(Y_AXIS, ys);
		json.put(Z_AXIS, zs);
		json.put(READING_TIMESTAMPS, ts);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void addSensorSpecificConfig(JSONObject json, SensorConfig config)
	{
		json.put(SAMPLE_LENGTH, config.getParameter(SensorConfig.SENSE_WINDOW_LENGTH_MILLIS));
	}

	@Override
	public SensorData toSensorData(String jsonString)
	{
		JSONObject jsonData = super.parseData(jsonString);
		if (jsonData != null)
		{
			long senseStartTimestamp = super.parseTimeStamp(jsonData);
			SensorConfig sensorConfig = super.getGenericConfig(jsonData);
			try
			{
				JSONArray xs = (JSONArray) jsonData.get(X_AXIS);
				JSONArray ys = (JSONArray) jsonData.get(Y_AXIS);
				JSONArray zs = (JSONArray) jsonData.get(Z_AXIS);
				
				ArrayList<float[]> sensorReadings = new ArrayList<float[]>();
				for (int i=0; i<xs.size(); i++)
				{
					float[] sample = new float[3];
					sample[0] = ((Double)xs.get(i)).floatValue();
					sample[1] = ((Double)ys.get(i)).floatValue();
					sample[2] = ((Double)zs.get(i)).floatValue();
					sensorReadings.add(sample);
				}
				
				JSONArray ts = (JSONArray) jsonData.get(READING_TIMESTAMPS);
				ArrayList<Long> sensorReadingTimestamps = new ArrayList<Long>();
				for (int i=0; i<ts.size(); i++)
				{
					sensorReadingTimestamps.add((Long) ts.get(i));
				}
				
				AccelerometerData accData = new AccelerometerData(senseStartTimestamp, sensorReadings, sensorReadingTimestamps, sensorConfig);
				
				return accData;
			}
			catch (NullPointerException e)
			{
				e.printStackTrace();
				return null;
			}
		}
		else
			return null;
	}

}
