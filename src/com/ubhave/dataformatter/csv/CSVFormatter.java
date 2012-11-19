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

package com.ubhave.dataformatter.csv;

import com.ubhave.sensormanager.ESException;
import com.ubhave.sensormanager.config.SensorConfig;
import com.ubhave.sensormanager.data.SensorData;
import com.ubhave.sensormanager.sensors.SensorUtils;

public abstract class CSVFormatter
{
	private final static String UNKNOWN_SENSOR = "unknownSensor";
	
	public String toCSV(final SensorData data)
	{
		StringBuilder builder = new StringBuilder();
		if (data != null)
		{
			SensorConfig config = data.getSensorConfig();
			
			addGenericData(builder, data);
			addSensorSpecificConfig(builder, config);
			addSensorSpecificData(builder, data);
		}
		return builder.toString();
	}
	
	public String getHeaders()
	{
		StringBuilder headers = new StringBuilder();
		headers.append("Sensor");
		headers.append(",SenseTime");
		addSensorSpecificHeaders(headers);
		return headers.toString();
	}
	
	private void addGenericData(StringBuilder builder, SensorData data)
	{
		try
		{
			String name = SensorUtils.getSensorName(data.getSensorType());
			builder.append(name);
		}
		catch (ESException e)
		{
			builder.append(UNKNOWN_SENSOR);
		}
		builder.append(","+data.getTimestamp());
	}
	
	protected abstract void addSensorSpecificData(StringBuilder builder, SensorData data);
	
	protected abstract void addSensorSpecificHeaders(StringBuilder builder);
	
	protected abstract void addSensorSpecificConfig(StringBuilder builder, SensorConfig config);
}
