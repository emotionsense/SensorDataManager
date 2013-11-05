package com.ubhave.dataformatter.json.pull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.ubhave.dataformatter.json.PullSensorJSONFormatter;
import com.ubhave.sensormanager.config.SensorConfig;
import com.ubhave.sensormanager.data.SensorData;
import com.ubhave.sensormanager.data.pullsensor.ContentReaderData;
import com.ubhave.sensormanager.data.pullsensor.ContentReaderResult;

public abstract class AbstractContentReaderFormatter extends PullSensorJSONFormatter
{
	private static final String CONTENT_LIST = "contentList";
	
	public AbstractContentReaderFormatter(final Context context, int type)
	{
		super(context, type);
	}

	@Override
	protected void addSensorSpecificData(JSONObject json, SensorData data) throws JSONException
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
			jsonArray.put(jsonMapObject);
		}
		json.put(CONTENT_LIST, jsonArray);
	}

	@Override
	protected void addSensorSpecificConfig(JSONObject json, SensorConfig config)
	{
		// nothing to add
	}
	
	protected abstract ContentReaderData getData(long senseStartTime, SensorConfig config);

	@SuppressWarnings("unchecked")
	@Override
	public SensorData toSensorData(String jsonString)
	{
		JSONObject jsonData = super.parseData(jsonString);
		if (jsonData != null)
		{
			long senseStartTimestamp = super.parseTimeStamp(jsonData);
			SensorConfig sensorConfig = super.getGenericConfig(jsonData);
			
			ContentReaderData data = getData(senseStartTimestamp, sensorConfig);
			try
			{
				JSONArray jsonArray = (JSONArray) jsonData.get(CONTENT_LIST);
				for (int i = 0; i < jsonArray.length(); i++)
				{
					HashMap<String, String> contentMap = new HashMap<String, String>();
					JSONObject jsonObject = (JSONObject) jsonArray.get(i);
					Iterator<String> keyIterator = jsonObject.keys();
					while (keyIterator.hasNext())
					{
						String key = keyIterator.next();
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
