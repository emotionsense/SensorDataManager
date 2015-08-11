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

package com.ubhave.dataformatter.json.push;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.BatteryManager;

import com.ubhave.dataformatter.json.PushSensorJSONFormatter;
import com.ubhave.sensormanager.config.SensorConfig;
import com.ubhave.sensormanager.data.SensorData;
import com.ubhave.sensormanager.data.push.BatteryData;
import com.ubhave.sensormanager.sensors.SensorUtils;

public class BatteryFormatter extends PushSensorJSONFormatter
{
	private final static String LEVEL = "level";
	private final static String SCALE = "scale";
	private final static String TEMPERATURE = "temperature";
	private final static String VOLTAGE = "voltage";
	private final static String PLUGGED = "plugged";
	private final static String STATUS = "status";
	private final static String HEALTH = "health";
	
	public BatteryFormatter(final Context context)
	{
		super(context, SensorUtils.SENSOR_TYPE_BATTERY);
	}

	private static String getHealthString(int healthValue)
	{
		switch (healthValue)
		{
		case BatteryManager.BATTERY_HEALTH_DEAD:
			return "BATTERY_HEALTH_DEAD";
		case BatteryManager.BATTERY_HEALTH_GOOD:
			return "BATTERY_HEALTH_GOOD";
		case BatteryManager.BATTERY_HEALTH_OVERHEAT:
			return "BATTERY_HEALTH_OVERHEAT";
		case BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE:
			return "BATTERY_HEALTH_OVER_VOLTAGE";
		case BatteryManager.BATTERY_HEALTH_UNKNOWN:
			return "BATTERY_HEALTH_UNKNOWN";
		case BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE:
			return "BATTERY_HEALTH_UNSPECIFIED_FAILURE";
		default:
			return "UNKNOWN";
		}
	}

	private static int getHealthId(String healthString)
	{
		if (healthString.equals("BATTERY_HEALTH_DEAD"))
		{
			return BatteryManager.BATTERY_HEALTH_DEAD;
		}
		else if (healthString.equals("BATTERY_HEALTH_GOOD"))
		{
			return BatteryManager.BATTERY_HEALTH_GOOD;
		}
		else if (healthString.equals("BATTERY_HEALTH_OVERHEAT"))
		{
			return BatteryManager.BATTERY_HEALTH_OVERHEAT;
		}
		else if (healthString.equals("BATTERY_HEALTH_OVER_VOLTAGE"))
		{
			return BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE;
		}
		else if (healthString.equals("BATTERY_HEALTH_UNKNOWN"))
		{
			return BatteryManager.BATTERY_HEALTH_UNKNOWN;
		}
		else if (healthString.equals("BATTERY_HEALTH_UNSPECIFIED_FAILURE"))
		{
			return BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE;
		}
		else
		{
			return -1;
		}
	}

	private static String getStatusString(int statusValue)
	{
		switch (statusValue)
		{
		case BatteryManager.BATTERY_STATUS_CHARGING:
			return "BATTERY_STATUS_CHARGING";
		case BatteryManager.BATTERY_STATUS_DISCHARGING:
			return "BATTERY_STATUS_DISCHARGING";
		case BatteryManager.BATTERY_STATUS_FULL:
			return "BATTERY_STATUS_FULL";
		case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
			return "BATTERY_STATUS_NOT_CHARGING";
		case BatteryManager.BATTERY_STATUS_UNKNOWN:
			return "BATTERY_STATUS_UNKNOWN";
		default:
			return "UNKNOWN";
		}
	}

	private static int getStatusId(String status)
	{
		if (status.equals("BATTERY_STATUS_CHARGING"))
		{
			return BatteryManager.BATTERY_STATUS_CHARGING;
		}
		else if (status.equals("BATTERY_STATUS_DISCHARGING"))
		{
			return BatteryManager.BATTERY_STATUS_DISCHARGING;
		}
		else if (status.equals("BATTERY_STATUS_FULL"))
		{
			return BatteryManager.BATTERY_STATUS_FULL;
		}
		else if (status.equals("BATTERY_STATUS_NOT_CHARGING"))
		{
			return BatteryManager.BATTERY_STATUS_NOT_CHARGING;
		}
		else if (status.equals("BATTERY_STATUS_UNKNOWN"))
		{
			return BatteryManager.BATTERY_STATUS_UNKNOWN;
		}
		else
		{
			return -1;
		}
	}

	@Override
	protected void addSensorSpecificData(JSONObject json, SensorData data) throws JSONException
	{
		BatteryData batteryData = (BatteryData) data;
		json.put(LEVEL, batteryData.getBatteryLevel());
		json.put(SCALE, batteryData.getScale());
		json.put(TEMPERATURE, batteryData.getTemperature());
		json.put(VOLTAGE, batteryData.getVoltage());
		json.put(PLUGGED, batteryData.getPlugged());
		json.put(STATUS, getStatusString(batteryData.getStatus()));
		json.put(HEALTH, getHealthString(batteryData.getHealth()));
	}

	@Override
	public SensorData toSensorData(String jsonString)
	{
		JSONObject jsonData = parseData(jsonString);
		if (jsonData != null)
		{
			long timestamp = parseTimeStamp(jsonData);
			SensorConfig sensorConfig = super.getGenericConfig(jsonData);
			BatteryData data = new BatteryData(timestamp, sensorConfig);
			try
			{
				data.setBatteryLevel(((Long) jsonData.get(LEVEL)).intValue());
				data.setScale(((Long) jsonData.get(SCALE)).intValue());
				data.setTemperature(((Long) jsonData.get(TEMPERATURE)).intValue());
				data.setVoltage(((Long) jsonData.get(VOLTAGE)).intValue());
				data.setPlugged(((Long) jsonData.get(PLUGGED)).intValue());
				data.setStatus(getStatusId((String) jsonData.get(STATUS)));
				data.setHealth(getHealthId((String) jsonData.get(HEALTH)));
				return data;
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
