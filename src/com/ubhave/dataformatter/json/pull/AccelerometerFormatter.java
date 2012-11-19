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
	
	private final static String SAMPLE_LENGTH = "sampleLengthMillis";
	
	@SuppressWarnings("unchecked")
	@Override
	protected void addSensorSpecificData(JSONObject json, SensorData data)
	{
		AccelerometerData accelerometerData = (AccelerometerData) data;
		ArrayList<float[]> readings = accelerometerData.getSensorReadings();
		
		JSONArray xs = new JSONArray();
		JSONArray ys = new JSONArray();
		JSONArray zs = new JSONArray();
		
		for (float[] sample : readings)
		{
			xs.add(sample[0]);
			ys.add(sample[1]);
			zs.add(sample[2]);
		}
		
		json.put(X_AXIS, xs);
		json.put(Y_AXIS, ys);
		json.put(Z_AXIS, zs);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void addSensorSpecificConfig(JSONObject json, SensorConfig config)
	{
		json.put(SAMPLE_LENGTH, config.getParameter(SensorConfig.SENSE_WINDOW_LENGTH_MILLIS));
	}

}
