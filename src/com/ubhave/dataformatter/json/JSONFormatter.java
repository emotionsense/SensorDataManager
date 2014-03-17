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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;

import com.ubhave.dataformatter.DataFormatter;
import com.ubhave.datahandler.config.DataHandlerConfig;
import com.ubhave.datahandler.config.DataStorageConfig;
import com.ubhave.sensormanager.ESException;
import com.ubhave.sensormanager.config.SensorConfig;
import com.ubhave.sensormanager.data.SensorData;
import com.ubhave.sensormanager.sensors.SensorUtils;

public abstract class JSONFormatter extends DataFormatter
{
	private final static String SENSOR_TYPE = "sensorType";
	private final static String SENSE_TIME = "senseStartTime";
	private final static String SENSE_TIME_MILLIS = "senseStartTimeMillis";
	private final static String UNKNOWN_SENSOR = "unknownSensor";
	private final static String USER_ID = "userid";
	private final static String DEVICE_ID = "deviceid";
	
	protected final Context applicationContext;
	protected final int sensorType;
	protected final DataHandlerConfig config;
	
	public JSONFormatter(Context c, int sensorType)
	{
		applicationContext = c;
		this.sensorType = sensorType;
		this.config = DataHandlerConfig.getInstance();
	}

	public JSONObject toJSON(final SensorData data)
	{
		JSONObject json = new JSONObject();
		if (data != null)
		{
			try
			{
				addGenericData(json, data);
				addSensorSpecificData(json, data);

				SensorConfig config = data.getSensorConfig();
				addGenericConfig(json, config);
				addSensorSpecificConfig(json, config);
			}
			catch (JSONException e)
			{
				e.printStackTrace();
			}
		}
		return json;
	}

	@Override
	public String toString(final SensorData data)
	{
		JSONObject jsonData = toJSON(data);
		if (jsonData != null)
		{
			return jsonData.toString();
		}
		else
		{
			return null;
		}
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
			return new JSONObject(jsonString);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	protected<T> ArrayList<T> getJSONArray(JSONObject data, String key, Class<T> c) throws NullPointerException
	{
		try
		{
			ArrayList<T> list = new ArrayList<T>();
			JSONArray jsonArray = (JSONArray) data.get(key);
			for (int i=0; i<jsonArray.length(); i++)
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
		catch (JSONException e)
		{
			e.printStackTrace();
			return null;
		}
	}

	@SuppressLint("SimpleDateFormat")
	protected void addGenericData(JSONObject json, SensorData data)
	{
		try
		{
			String userId = (String) config.get(DataStorageConfig.UNIQUE_USER_ID);
			if (userId != null && userId.length() > 0)
			{
				json.put(USER_ID, userId);
			}
			
			String deviceId = (String) config.get(DataStorageConfig.UNIQUE_DEVICE_ID);
			if (deviceId != null && deviceId.length() > 0)
			{
				json.put(DEVICE_ID, deviceId);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(data.getTimestamp());

		SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss:SSS dd MM yyyy Z z");
		try
		{
			json.put(SENSE_TIME, formatter.format(calendar.getTime()));
			json.put(SENSE_TIME_MILLIS, data.getTimestamp());
			try
			{
				String sensorName = SensorUtils.getSensorName(data.getSensorType());
				json.put(SENSOR_TYPE, sensorName);
			}
			catch (ESException e)
			{
				e.printStackTrace();
				json.put(SENSOR_TYPE, UNKNOWN_SENSOR);
			}
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
	}

	@SuppressLint("SimpleDateFormat")
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

	protected abstract void addGenericConfig(JSONObject json, SensorConfig config) throws JSONException;

	protected abstract void addSensorSpecificData(JSONObject json, SensorData data) throws JSONException;

	protected abstract void addSensorSpecificConfig(JSONObject json, SensorConfig config) throws JSONException;
	
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
