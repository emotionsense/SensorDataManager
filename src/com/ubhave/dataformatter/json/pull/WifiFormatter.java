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

import android.net.wifi.ScanResult;

import com.ubhave.dataformatter.json.PullSensorJSONFormatter;
import com.ubhave.sensormanager.config.SensorConfig;
import com.ubhave.sensormanager.data.SensorData;
import com.ubhave.sensormanager.data.pullsensor.WifiData;

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

	@SuppressWarnings("unchecked")
	@Override
	protected void addSensorSpecificData(JSONObject json, SensorData data)
	{
		WifiData wifiData = (WifiData) data;
		ArrayList<ScanResult> results = wifiData.getWifiScanData();
		JSONArray resultJSON = new JSONArray();
		if (results != null)
		{
			for (ScanResult result : results)
			{
				JSONObject scanJSON = new JSONObject();
				scanJSON.put(SSID, result.SSID);
				scanJSON.put(BSSID, result.BSSID);
				scanJSON.put(CAPABILITIES, result.capabilities);
				scanJSON.put(LEVEL, result.level);
				scanJSON.put(FREQUENCY, result.frequency);
				resultJSON.add(scanJSON);
			}
		}
		else
		{
			resultJSON.add(UNAVAILABLE);
		}
		json.put(SCAN_RESULT, resultJSON);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void addSensorSpecificConfig(JSONObject json, SensorConfig config)
	{
		json.put(SENSE_CYCLES, config.getParameter(SensorConfig.NUMBER_OF_SENSE_CYCLES));
	}

}
