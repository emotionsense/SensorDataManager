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

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.location.Location;

import com.ubhave.dataformatter.json.PullSensorJSONFormatter;
import com.ubhave.sensormanager.ESException;
import com.ubhave.sensormanager.config.SensorConfig;
import com.ubhave.sensormanager.config.sensors.pull.LocationConfig;
import com.ubhave.sensormanager.data.SensorData;
import com.ubhave.sensormanager.data.pullsensor.LocationData;
import com.ubhave.sensormanager.process.AbstractProcessor;
import com.ubhave.sensormanager.process.pull.LocationProcessor;
import com.ubhave.sensormanager.sensors.SensorUtils;

public class LocationFormatter extends PullSensorJSONFormatter
{
	private final static String LATITUDE = "latitude";
	private final static String LONGITUDE = "longitude";
	private final static String ACCURACY = "accuracy";
	private final static String SPEED = "speed";
	private final static String BEARING = "bearing";
	private final static String PROVIDER = "provider";
	private final static String TIME = "time";

	private final static String LOCATION_ACCURACY = "configAccuracy";
	private final static String UNKNOWN_STRING = "unknown";
	private final static double UNKNOWN_DOUBLE = 0.0;
	private final static long UNKNOWN_LONG = 0;
	
	public LocationFormatter(final Context context)
	{
		super(context, SensorUtils.SENSOR_TYPE_LOCATION);
	}

	@Override
	protected void addSensorSpecificData(JSONObject json, SensorData data) throws JSONException
	{
		LocationData locationData = (LocationData) data;
		Location location = locationData.getLocation();
		if (location != null)
		{
			json.put(LATITUDE, location.getLatitude());
			json.put(LONGITUDE, location.getLongitude());
			json.put(ACCURACY, location.getAccuracy());
			json.put(SPEED, location.getSpeed());
			json.put(BEARING, location.getBearing());
			json.put(PROVIDER, location.getProvider());
			json.put(TIME, location.getTime());
		}
		else
		{
			json.put(LATITUDE, UNKNOWN_DOUBLE);
			json.put(LONGITUDE, UNKNOWN_DOUBLE);
			json.put(ACCURACY, UNKNOWN_DOUBLE);
			json.put(SPEED, UNKNOWN_DOUBLE);
			json.put(BEARING, UNKNOWN_DOUBLE);
			json.put(PROVIDER, UNKNOWN_STRING);
			json.put(TIME, UNKNOWN_LONG);
		}
	}

	@Override
	protected void addSensorSpecificConfig(JSONObject json, SensorConfig config) throws JSONException
	{
		json.put(LOCATION_ACCURACY, config.getParameter(LocationConfig.ACCURACY_TYPE));
	}

	@Override
	public SensorData toSensorData(String jsonString)
	{
		JSONObject jsonData = super.parseData(jsonString);
		long senseStartTimestamp = super.parseTimeStamp(jsonData);
		SensorConfig sensorConfig = super.getGenericConfig(jsonData);
		
		boolean setRawData = true;
		boolean setProcessedData = false;
		Location location = null;
		
		try
		{
			double latitude = (Double) jsonData.get(LATITUDE);
			double longitude = (Double) jsonData.get(LONGITUDE);
			float accuracy = ((Double) jsonData.get(ACCURACY)).floatValue();
			float speed = ((Double)  jsonData.get(SPEED)).floatValue();
			float bearing = ((Double)  jsonData.get(BEARING)).floatValue();
			String provider = (String) jsonData.get(PROVIDER);
			long timestamp = (Long) jsonData.get(TIME);

			location = new Location(provider);
			location.setLatitude(latitude);
			location.setLongitude(longitude);
			location.setAccuracy(accuracy);
			location.setSpeed(speed);
			location.setBearing(bearing);
			location.setTime(timestamp);
		}
		catch (Exception e)
		{
			setRawData = false;
		}

		try
		{
			LocationProcessor processor = (LocationProcessor) AbstractProcessor.getProcessor(applicationContext, sensorType, setRawData, setProcessedData);
			return processor.process(senseStartTimestamp, location, sensorConfig);
		}
		catch (ESException e)
		{
			e.printStackTrace();
			return null;
		}
	}

}
