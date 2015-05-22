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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.ubhave.dataformatter.json.PullSensorJSONFormatter;
import com.ubhave.sensormanager.ESException;
import com.ubhave.sensormanager.config.SensorConfig;
import com.ubhave.sensormanager.config.pull.PullSensorConfig;
import com.ubhave.sensormanager.data.SensorData;
import com.ubhave.sensormanager.data.pull.WifiData;
import com.ubhave.sensormanager.data.pull.WifiScanResult;
import com.ubhave.sensormanager.process.AbstractProcessor;
import com.ubhave.sensormanager.process.pull.WifiProcessor;
import com.ubhave.sensormanager.sensors.SensorUtils;

public class WifiFormatter extends PullSensorJSONFormatter
{
	private final static String SCAN_RESULT = "scanResult";
	private final static String SSID = "ssid";
	private final static String BSSID = "bssid";
	private final static String CAPABILITIES = "capabilities";
	private final static String LEVEL = "level";
	private final static String FREQUENCY = "frequency";

	private final static String UNAVAILABLE = "unavailable";
	private final static String SENSE_CYCLES = "senseCycles";
	
	public WifiFormatter(final Context context)
	{
		super(context, SensorUtils.SENSOR_TYPE_WIFI);
	}

	@Override
	protected void addSensorSpecificData(JSONObject json, SensorData data) throws JSONException
	{
		WifiData wifiData = (WifiData) data;
		ArrayList<WifiScanResult> results = wifiData.getWifiScanData();
		JSONArray resultJSON = new JSONArray();
		if (results != null)
		{
			json.put(UNAVAILABLE, false);
			for (WifiScanResult result : results)
			{
				JSONObject scanJSON = new JSONObject();
				scanJSON.put(SSID, result.getSsid());
				scanJSON.put(BSSID, result.getBssid());
				scanJSON.put(CAPABILITIES, result.getCapabilities());
				scanJSON.put(LEVEL, result.getLevel());
				scanJSON.put(FREQUENCY, result.getFrequency());
				resultJSON.put(scanJSON);
			}
		}
		else
		{
			json.put(UNAVAILABLE, true);
		}
		json.put(SCAN_RESULT, resultJSON);
	}

	@Override
	protected void addSensorSpecificConfig(JSONObject json, SensorConfig config) throws JSONException
	{
		if (config != null)
		{
			json.put(SENSE_CYCLES, config.getParameter(PullSensorConfig.NUMBER_OF_SENSE_CYCLES));
		}
	}

	@Override
	public SensorData toSensorData(String jsonString)
	{
		JSONObject jsonData = super.parseData(jsonString);
		long senseStartTimestamp = super.parseTimeStamp(jsonData);
		SensorConfig sensorConfig = super.getGenericConfig(jsonData);
		
		boolean setRawData = true;
		boolean setProcessedData = false;
		
		ArrayList<WifiScanResult> wifiList = new ArrayList<WifiScanResult>(); 
		try
		{
			boolean isUnavailable = jsonData.getBoolean(UNAVAILABLE);
			if (!isUnavailable)
			{
				JSONArray jsonArray = (JSONArray)jsonData.get(SCAN_RESULT);
				for (int i = 0; i < jsonArray.length(); i++)
				{
					JSONObject jsonObject = (JSONObject)jsonArray.get(i);
					String ssid = (String)jsonObject.get(SSID);
					String bssid = (String)jsonObject.get(BSSID);
					String capabilities = (String)jsonObject.get(CAPABILITIES);
					int level = ((Long)jsonObject.get(LEVEL)).intValue();
					int frequency = ((Long)jsonObject.get(FREQUENCY)).intValue();
					
					WifiScanResult scanResult = new WifiScanResult(ssid, bssid, capabilities, level, frequency);
					wifiList.add(scanResult);
				}
			}
		}
		catch (Exception e)
		{
			setRawData = false;
		}
		try
		{
			WifiProcessor processor = (WifiProcessor) AbstractProcessor.getProcessor(applicationContext, sensorType, setRawData, setProcessedData);
			return processor.process(senseStartTimestamp, wifiList, sensorConfig);
		}
		catch (ESException e)
		{
			e.printStackTrace();
			return null;
		}
	}
}
