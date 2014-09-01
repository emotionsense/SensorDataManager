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

import com.ubhave.dataformatter.json.PushSensorJSONFormatter;
import com.ubhave.sensormanager.config.SensorConfig;
import com.ubhave.sensormanager.data.SensorData;
import com.ubhave.sensormanager.data.push.ConnectionStateData;
import com.ubhave.sensormanager.sensors.SensorUtils;

public class ConnectionStateFormatter extends PushSensorJSONFormatter
{
	public final static int NO_CONNECTION = 0;
	public final static int MOBILE_CONNECTION = 1;
	public final static int WIFI_CONNECTION = 2;
	public final static int OTHER_CONNECTION = 3;

	private final static String CONNECTED = "connected";
	private final static String CONNECTING = "connecting";
	private final static String AVAILABLE = "available";
	private final static String NETWORK_TYPE = "networkType";
	private final static String ROAMING = "roaming";
	private final static String SSID = "ssid";

	public ConnectionStateFormatter(final Context context)
	{
		super(context, SensorUtils.SENSOR_TYPE_CONNECTION_STATE);
	}

	private String getConnectionTypeString(int type)
	{
		switch (type)
		{
		case 0:
			return "NONE";
		case 1:
			return "MOBILE";
		case 2:
			return "WIFI";
		case 3:
			return "OTHER";
		default:
			return "UNKNOWN";
		}
	}

	private int getConnectionTypeId(String type)
	{
		if (type.equals("NONE"))
		{
			return 0;
		}
		else if (type.equals("MOBILE"))
		{
			return 1;
		}
		else if (type.equals("WIFI"))
		{
			return 2;
		}
		else if (type.equals("OTHER"))
		{
			return 3;
		}
		else
		{
			return -1;
		}
	}

	private String getRoamingString(int status)
	{
		switch (status)
		{
		case 0:
			return "ROAMING";
		case 1:
			return "NOT_ROAMING";
		default:
			return "UNKNOWN";
		}
	}

	private int getRoamingId(String status)
	{
		if (status.equals("ROAMING"))
		{
			return 0;
		}
		else if (status.equals("NOT_ROAMING"))
		{
			return 1;
		}
		else
		{
			return -1;
		}
	}

	@Override
	protected void addSensorSpecificData(JSONObject json, SensorData data)
			throws JSONException
	{
		ConnectionStateData connectionData = (ConnectionStateData) data;
		json.put(CONNECTED, connectionData.isConnected());
		json.put(CONNECTING, connectionData.isConnectedOrConnecting());
		json.put(AVAILABLE, connectionData.isAvailable());
		json.put(NETWORK_TYPE,
				getConnectionTypeString(connectionData.getNetworkType()));
		json.put(ROAMING, getRoamingString(connectionData.getRoamingStatus()));

		String ssid = connectionData.getSSID();
		if (ssid != null)
		{
			json.put(SSID, ssid);
		}
	}

	@Override
	public SensorData toSensorData(String jsonString)
	{
		JSONObject jsonData = super.parseData(jsonString);
		if (jsonData != null)
		{
			long dataReceivedTimestamp = super.parseTimeStamp(jsonData);
			SensorConfig sensorConfig = super.getGenericConfig(jsonData);
			ConnectionStateData data = new ConnectionStateData(
					dataReceivedTimestamp, sensorConfig);
			try
			{
				data.setConnectedOrConnecting((Boolean) jsonData
						.get(CONNECTING));
				data.setAvailable((Boolean) jsonData.get(AVAILABLE));
				data.setConnected((Boolean) jsonData.get(CONNECTED));
				data.setNetworkType(getConnectionTypeId((String) jsonData
						.get(NETWORK_TYPE)));
				data.setRoamingStatus(getRoamingId((String) jsonData
						.get(ROAMING)));
			} catch (Exception e)
			{
				e.printStackTrace();
			}
			return data;
		}
		return null;
	}
}
