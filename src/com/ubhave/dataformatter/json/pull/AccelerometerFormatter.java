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
import com.ubhave.sensormanager.config.sensors.pull.PullSensorConfig;
import com.ubhave.sensormanager.data.SensorData;
import com.ubhave.sensormanager.data.pullsensor.AccelerometerData;
import com.ubhave.sensormanager.process.AbstractProcessor;
import com.ubhave.sensormanager.process.pull.AccelerometerProcessor;
import com.ubhave.sensormanager.sensors.SensorUtils;

public class AccelerometerFormatter extends PullSensorJSONFormatter
{
	private final static String X_AXIS = "xAxis";
	private final static String Y_AXIS = "yAxis";
	private final static String Z_AXIS = "zAxis";
	private final static String READING_TIMESTAMPS = "sensorTimeStamps";

	private final static String SAMPLE_LENGTH = "sampleLengthMillis";
	
	public AccelerometerFormatter(final Context context)
	{
		super(context, SensorUtils.SENSOR_TYPE_ACCELEROMETER);
	}

	@Override
	protected void addSensorSpecificData(JSONObject json, SensorData data) throws JSONException
	{
		AccelerometerData accelerometerData = (AccelerometerData) data;
		ArrayList<float[]> readings = accelerometerData.getSensorReadings();
		ArrayList<Long> timestamps = accelerometerData.getSensorReadingTimestamps();
		if (readings != null && timestamps != null)
		{
			// Raw data set
			JSONArray xs = new JSONArray();
			JSONArray ys = new JSONArray();
			JSONArray zs = new JSONArray();

			for (int i=0; i<readings.size(); i++)
			{
				float[] sample = readings.get(i);
				xs.put(sample[0]);
				ys.put(sample[1]);
				zs.put(sample[2]);
			}
			
			JSONArray ts = new JSONArray();
			for (int i=0; i<timestamps.size(); i++)
			{
				ts.put(timestamps.get(i));
			}

			json.put(X_AXIS, xs);
			json.put(Y_AXIS, ys);
			json.put(Z_AXIS, zs);
			json.put(READING_TIMESTAMPS, ts);
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
		if (jsonData != null)
		{
			long senseStartTimestamp = super.parseTimeStamp(jsonData);
			SensorConfig sensorConfig = super.getGenericConfig(jsonData);
			ArrayList<float[]> sensorReadings = new ArrayList<float[]>();
			ArrayList<Long> sensorReadingTimestamps = null;
			
			boolean setRawData = true;
			boolean setProcessedData = false;
			try
			{
				ArrayList<Double> xs = getJSONArray(jsonData, X_AXIS, Double.class);
				ArrayList<Double> ys = getJSONArray(jsonData, Y_AXIS, Double.class);
				ArrayList<Double> zs = getJSONArray(jsonData, Z_AXIS, Double.class);
				sensorReadingTimestamps = getJSONArray(jsonData, READING_TIMESTAMPS, Long.class);
			
				for (int i=0; i<xs.size(); i++)
				{
					float[] sample = new float[3];
					sample[0] = ((Double)xs.get(i)).floatValue();
					sample[1] = ((Double)ys.get(i)).floatValue();
					sample[2] = ((Double)zs.get(i)).floatValue();
					sensorReadings.add(sample);
				}
			}
			catch (NullPointerException e)
			{
				setRawData = false;
			}
			
			try
			{
				AccelerometerProcessor processor = (AccelerometerProcessor) AbstractProcessor.getProcessor(applicationContext, sensorType, setRawData, setProcessedData);
				return processor.process(senseStartTimestamp, sensorReadings, sensorReadingTimestamps, sensorConfig);
			}
			catch (ESException e)
			{
				e.printStackTrace();
				return null;
			}
		}
		else
		{
			return null;
		}	
	}
}
