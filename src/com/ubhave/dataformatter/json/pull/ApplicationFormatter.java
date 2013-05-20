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

import android.content.Context;

import com.ubhave.dataformatter.json.PullSensorJSONFormatter;
import com.ubhave.sensormanager.ESException;
import com.ubhave.sensormanager.config.SensorConfig;
import com.ubhave.sensormanager.config.sensors.pull.PullSensorConfig;
import com.ubhave.sensormanager.data.SensorData;
import com.ubhave.sensormanager.data.pullsensor.ApplicationData;
import com.ubhave.sensormanager.process.AbstractProcessor;
import com.ubhave.sensormanager.process.pull.ApplicationProcessor;
import com.ubhave.sensormanager.sensors.SensorUtils;

public class ApplicationFormatter extends PullSensorJSONFormatter
{
	private final static String APPLICATION_RESULT = "applicationResult";

	private final static String UNAVAILABLE = "unavailable";
	private final static String SENSE_CYCLES = "senseCycles";
	
	public ApplicationFormatter(final Context context)
	{
		super(context, SensorUtils.SENSOR_TYPE_APPLICATION);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void addSensorSpecificData(JSONObject json, SensorData data)
	{
		ApplicationData appData = (ApplicationData) data;
		ArrayList<String> results = appData.getApplications();
		JSONArray resultJSON = new JSONArray();
		if (results != null)
		{
			for (String result : results)
			{
				resultJSON.add(result);
			}
		}
		else
		{
			resultJSON.add(UNAVAILABLE);
		}
		json.put(APPLICATION_RESULT, resultJSON);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void addSensorSpecificConfig(JSONObject json, SensorConfig config)
	{
		json.put(SENSE_CYCLES, config.getParameter(PullSensorConfig.NUMBER_OF_SENSE_CYCLES));
	}

	@Override
	public SensorData toSensorData(String jsonString)
	{
		JSONObject jsonData = super.parseData(jsonString);
		long senseStartTimestamp = super.parseTimeStamp(jsonData);
		SensorConfig sensorConfig = super.getGenericConfig(jsonData);
		
		ArrayList<String> appList = null; 
		
		boolean setRawData = true;
		boolean setProcessedData = false;
		try
		{
			appList = getJSONArray(jsonData, APPLICATION_RESULT, String.class);
		}
		catch (Exception e)
		{
			setRawData = false;
		}
		try
		{
			ApplicationProcessor processor = (ApplicationProcessor) AbstractProcessor.getProcessor(applicationContext, sensorType, setRawData, setProcessedData);
			return processor.process(senseStartTimestamp, appList, sensorConfig);
		}
		catch (ESException e)
		{
			e.printStackTrace();
			return null;
		}
	}
}
