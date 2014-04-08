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
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.ubhave.dataformatter.json.PullSensorJSONFormatter;
import com.ubhave.sensormanager.config.SensorConfig;
import com.ubhave.sensormanager.config.sensors.pull.PullSensorConfig;
import com.ubhave.sensormanager.data.SensorData;
import com.ubhave.sensormanager.data.pullsensor.ApplicationData;
import com.ubhave.sensormanager.data.pullsensor.ApplicationDataList;
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

	@Override
	protected void addSensorSpecificData(JSONObject json, SensorData data) throws JSONException
	{
		ApplicationDataList appData = (ApplicationDataList) data;
		ArrayList<ApplicationData> results = appData.getApplications();
		JSONArray resultJSON = new JSONArray();
		if (results != null)
		{
			for (ApplicationData result : results)
			{
				JSONObject map = new JSONObject();
				for (String key : result.keySet())
				{
					map.put(key, result.get(key));
				}
				resultJSON.put(map);
			}
		}
		else
		{
			resultJSON.put(UNAVAILABLE);
		}
		json.put(APPLICATION_RESULT, resultJSON);
	}

	@Override
	protected void addSensorSpecificConfig(JSONObject json, SensorConfig config) throws JSONException
	{
		json.put(SENSE_CYCLES, config.getParameter(PullSensorConfig.NUMBER_OF_SENSE_CYCLES));
	}

	@SuppressWarnings("unchecked")
	@Override
	public SensorData toSensorData(String jsonString)
	{
		JSONObject jsonData = super.parseData(jsonString);
		if (jsonData != null)
		{
			long senseStartTimestamp = super.parseTimeStamp(jsonData);
			SensorConfig sensorConfig = super.getGenericConfig(jsonData);
			
			boolean setRawData = true;
			boolean setProcessedData = false;
			
			try
			{
				ArrayList<ApplicationData> appList = new ArrayList<ApplicationData>();
				JSONArray jsonArray = (JSONArray) jsonData.get(APPLICATION_RESULT);
				for (int i=0; i<jsonArray.length(); i++)
				{
					JSONObject entry = (JSONObject) jsonArray.get(i);
					ApplicationData data = new ApplicationData();
					Iterator<String> keyIterator = entry.keys();
					while (keyIterator.hasNext())
					{
						String key = keyIterator.next();
						entry.put(key, (String) entry.get(key));
					}
					appList.add(data);
				}
				
				ApplicationProcessor processor = (ApplicationProcessor) AbstractProcessor.getProcessor(applicationContext, sensorType, setRawData, setProcessedData);
				return processor.process(senseStartTimestamp, appList, sensorConfig);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		return null;
	}
}
