package com.ubhave.dataformatter.json.pull;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import android.content.Context;

import com.ubhave.dataformatter.json.PullSensorJSONFormatter;
import com.ubhave.sensormanager.config.SensorConfig;
import com.ubhave.sensormanager.data.SensorData;
import com.ubhave.sensormanager.data.pullsensor.ContentReaderData;
import com.ubhave.sensormanager.data.pullsensor.ContentReaderResult;

public abstract class AbstractContentReaderFormatter extends PullSensorJSONFormatter
{
	private static final String CONTENT_LIST = "contentList";
	private static final String SENSOR_TYPE = "sensorType";
	
	public AbstractContentReaderFormatter(final Context context, int type)
	{
		super(context, type);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void addSensorSpecificData(JSONObject json, SensorData data)
	{
		ContentReaderData crData = (ContentReaderData) data;
		ArrayList<ContentReaderResult> contentList = crData.getContentList();

		JSONArray jsonArray = new JSONArray();
		for (ContentReaderResult contentMap : contentList)
		{
			JSONObject jsonMapObject = new JSONObject();
			for (String key : contentMap.getKeys())
			{
				jsonMapObject.put(key, contentMap.get(key));
			}
			jsonArray.add(jsonMapObject);
		}
		json.put(CONTENT_LIST, jsonArray);
		json.put(SENSOR_TYPE, crData.getSensorType());
	}

	@Override
	protected void addSensorSpecificConfig(JSONObject json, SensorConfig config)
	{
		// nothing to add
	}

	@Override
	public SensorData toSensorData(String jsonString)
	{
		JSONObject jsonData = super.parseData(jsonString);
		if (jsonData != null)
		{
			long senseStartTimestamp = super.parseTimeStamp(jsonData);
			SensorConfig sensorConfig = super.getGenericConfig(jsonData);
			
			ContentReaderData data = new ContentReaderData(senseStartTimestamp, sensorConfig);
			try
			{
				data.setSensorType(((Long)jsonData.get(SENSOR_TYPE)).intValue());
				JSONArray jsonArray = (JSONArray) jsonData.get(CONTENT_LIST);
				for (int i = 0; i < jsonArray.size(); i++)
				{
					HashMap<String, String> contentMap = new HashMap<String, String>();
					JSONObject jsonObject = (JSONObject) jsonArray.get(i);
					for (Object keyObject : jsonObject.keySet())
					{
						String key = (String) keyObject;
						contentMap.put(key, (String) jsonObject.get(key));
					}
					
					ContentReaderResult entry = new ContentReaderResult();
					entry.setContentMap(contentMap);
					data.addContent(entry);
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			return data;
		}
		else
		{
			return null;
		}
	}

}
