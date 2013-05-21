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

package com.ubhave.dataformatter.json;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import android.content.Context;

import com.ubhave.dataformatter.DataFormatter;
import com.ubhave.sensormanager.ESException;
import com.ubhave.sensormanager.config.SensorConfig;
import com.ubhave.sensormanager.data.SensorData;
import com.ubhave.sensormanager.sensors.SensorUtils;

public abstract class JSONFormatter extends DataFormatter
{
	private final static String SENSOR_TYPE = "sensorType";
	private final static String SENSE_TIME = "senseStartTime";
	private final static String UNKNOWN_SENSOR = "unknownSensor";
	
	protected final Context applicationContext;
	protected final int sensorType;
	
	public JSONFormatter(Context c, int sensorType)
	{
		applicationContext = c;
		this.sensorType = sensorType;
	}

	public JSONObject toJSON(final SensorData data)
	{
		JSONObject json = new JSONObject();
		if (data != null)
		{
			addGenericData(json, data);
			addSensorSpecificData(json, data);

			SensorConfig config = data.getSensorConfig();
			addGenericConfig(json, config);
			addSensorSpecificConfig(json, config);
		}
		return json;
	}

	@Override
	public String toString(final SensorData data)
	{
		return toJSON(data).toJSONString();
	}
	
	public long getTimestamp(String sensorDataJsonString)
	{
		long timestamp = 0;
		JSONObject jsonObject = parseData(sensorDataJsonString);
		if (jsonObject != null)
		{
			timestamp = parseTimeStamp(jsonObject);
		}
		return timestamp;
	}
	
	protected JSONObject parseData(String jsonString)
	{
		try
		{
			JSONParser parser = new JSONParser();
			return (JSONObject) parser.parse(jsonString);
		}
		catch (ParseException e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	protected<T> ArrayList<T> getJSONArray(JSONObject data, String key, Class<T> c) throws NullPointerException
	{
		ArrayList<T> list = new ArrayList<T>();
		JSONArray jsonArray = (JSONArray) data.get(key);
		for (int i = 0; i < jsonArray.size(); i++)
		{
			try
			{
				T member = c.cast(jsonArray.get(i));
				list.add(member);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			
		}
		return list;
	}

	@SuppressWarnings("unchecked")
	protected void addGenericData(JSONObject json, SensorData data)
	{
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(data.getTimestamp());

		SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss:SSS dd MM yyyy Z z");
		json.put(SENSE_TIME, formatter.format(calendar.getTime()));
		try
		{
			json.put(SENSOR_TYPE, SensorUtils.getSensorName(data.getSensorType()));
		}
		catch (ESException e)
		{
			json.put(SENSOR_TYPE, UNKNOWN_SENSOR);
		}
	}

	protected long parseTimeStamp(JSONObject json)
	{
		try
		{
			String dateString = (String) json.get(SENSE_TIME);
			SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss:SSS dd MM yyyy Z z");
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(formatter.parse(dateString));
			return calendar.getTimeInMillis();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return -1;
		}
	}

	public abstract SensorData toSensorData(String jsonString);

	protected abstract void addGenericConfig(JSONObject json, SensorConfig config);

	protected abstract void addSensorSpecificData(JSONObject json, SensorData data);

	protected abstract void addSensorSpecificConfig(JSONObject json, SensorConfig config);
	
	protected abstract SensorConfig getGenericConfig(JSONObject json);
	
	protected Integer getInteger(String key, JSONObject data)
	{
		try
		{
			Integer value = ((Long) data.get(key)).intValue();
			return value;
		}
		catch (Exception e)
		{
			return null;
		}
	}
	
	protected String getString(String key, JSONObject data)
	{
		try
		{
			String value = (String) data.get(key);
			return value;
		}
		catch (Exception e)
		{
			return null;
		}
	}
	
	protected Boolean getBoolean(String key, JSONObject data)
	{
		try
		{
			Boolean value = (Boolean) data.get(key);
			return value;
		}
		catch (Exception e)
		{
			return null;
		}
	}
	
	protected Float getFloat(String key, JSONObject data)
	{
		try
		{
			Double value = (Double) data.get(key);
			if (value != null)
			{
				return value.floatValue();
			}
			else return null;
		}
		catch (Exception e)
		{
			return null;
		}
	}
}
