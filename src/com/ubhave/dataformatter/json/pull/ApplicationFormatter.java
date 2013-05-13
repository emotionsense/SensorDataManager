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
import com.ubhave.sensormanager.data.pullsensor.ApplicationData;

public class ApplicationFormatter extends PullSensorJSONFormatter
{
	private final static String APPLICATION = "application";
	private final static String APPLICATION_RESULT = "applicationResult";

	private final static String UNAVAILABLE = "unavailable";
	private final static String SENSE_CYCLES = "senseCycles";

	@SuppressWarnings("unchecked")
	@Override
	protected void addSensorSpecificData(JSONObject json, SensorData data)
	{
		ApplicationData appData = (ApplicationData) data;
		ArrayList<String> results =appData.getApplications();
		JSONArray resultJSON = new JSONArray();
		if (results != null)
		{
			for (String result : results)
			{
				JSONObject appJSON = new JSONObject();
				appJSON.put(APPLICATION, result);
				resultJSON.add(appJSON);
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
		json.put(SENSE_CYCLES, config.getParameter(SensorConfig.NUMBER_OF_SENSE_CYCLES));
	}

	@Override
	public SensorData toSensorData(String jsonString)
	{
		JSONObject jsonData = super.parseData(jsonString);
		long senseStartTimestamp = super.parseTimeStamp(jsonData);
		SensorConfig sensorConfig = super.getGenericConfig(jsonData);
		
		JSONArray jsonArray = (JSONArray)jsonData.get(APPLICATION_RESULT);
		
		ArrayList<String> appList = new ArrayList<String>(); 
		
		for (int i = 0; i < jsonArray.size(); i++)
		{
			JSONObject jsonObject = (JSONObject)jsonArray.get(i);
			String application = (String)jsonObject.get(APPLICATION);

			appList.add(application);
		}
		
		ApplicationData applicationData = new ApplicationData(senseStartTimestamp, sensorConfig);
		applicationData.setApplications(appList);

		return applicationData;
	}

}
