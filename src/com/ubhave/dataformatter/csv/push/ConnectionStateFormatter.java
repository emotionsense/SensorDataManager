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

package com.ubhave.dataformatter.csv.push;

import com.ubhave.dataformatter.csv.PushSensorCSVFormatter;
import com.ubhave.sensormanager.data.SensorData;
import com.ubhave.sensormanager.data.pushsensor.ConnectionStateData;

public class ConnectionStateFormatter extends PushSensorCSVFormatter
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

	private String getConnectionTypeString(int type)
	{
		if (type == 0)
		{
			return "NONE";
		}
		else if (type == 1)
		{
			return "MOBILE";
		}
		else if (type == 2)
		{
			return "WIFI";
		}
		else if (type == 3)
		{
			return "OTHER";
		}
		else
		{
			return "UNKNOWN";
		}
	}

	private String getRoamingString(int status)
	{
		if (status == 0)
		{
			return "ROAMING";
		}
		else if (status == 1)
		{
			return "NOT_ROAMING";
		}
		else
		{
			return "UNKNOWN";
		}
	}

	@Override
	protected void addSensorSpecificData(StringBuilder builder, SensorData data)
	{
		ConnectionStateData connectionData = (ConnectionStateData) data;
		builder.append(","+connectionData.isConnected());
		builder.append(","+connectionData.isConnectedOrConnecting());
		builder.append(","+connectionData.isAvailable());
		builder.append(","+getConnectionTypeString(connectionData.getNetworkType()));
		builder.append(","+getRoamingString(connectionData.getRoamingStatus()));

		String ssid = connectionData.getSSID();
		if (ssid != null)
		{
			builder.append(","+ssid);
		}
		else builder.append(",NONE");
	}

	@Override
	protected void addSensorSpecificHeaders(StringBuilder builder)
	{
		builder.append(","+CONNECTED);
		builder.append(","+CONNECTING);
		builder.append(","+AVAILABLE);
		builder.append(","+NETWORK_TYPE);
		builder.append(","+ROAMING);
		builder.append(","+SSID);
	}
}
