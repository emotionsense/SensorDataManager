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

package com.ubhave.dataformatter.csv.pull;

import android.location.Location;

import com.ubhave.dataformatter.csv.CSVFormatter;
import com.ubhave.sensormanager.config.SensorConfig;
import com.ubhave.sensormanager.data.SensorData;
import com.ubhave.sensormanager.data.pullsensor.LocationData;

public class LocationFormatter extends CSVFormatter
{	
	
	private final static String LATITUDE = "latitude";
	private final static String LONGITUDE = "longitude";
	private final static String ACCURACY = "accuracy";
	private final static String SPEED = "speed";
	private final static String BEARING = "bearing";
	private final static String PROVIDER = "provider";
	private final static String TIME = "time";
	
	private final static String LOCATION_ACCURACY = "configAccuracy";
	
	@Override
	protected void addSensorSpecificData(StringBuilder builder, SensorData data)
	{
		LocationData locationData = (LocationData) data;
		Location location = locationData.getLocation();
		if (location != null)
		{
			builder.append(location.getLatitude());
			builder.append(","+location.getLongitude());
			builder.append(","+location.getAccuracy());
			builder.append(","+location.getSpeed());
			builder.append(","+location.getBearing());
			builder.append(","+location.getProvider());
			builder.append(","+location.getTime());
		}
	}

	@Override
	protected void addSensorSpecificConfig(StringBuilder builder, SensorConfig config)
	{
		builder.append(","+config.getParameter(SensorConfig.LOCATION_ACCURACY));
	}

	@Override
	protected void addSensorSpecificHeaders(StringBuilder builder)
	{
		builder.append(","+LOCATION_ACCURACY);
		builder.append(","+LATITUDE);
		builder.append(","+LONGITUDE);
		builder.append(","+ACCURACY);
		builder.append(","+SPEED);
		builder.append(","+BEARING);
		builder.append(","+PROVIDER);
		builder.append(","+TIME);
	}

}
