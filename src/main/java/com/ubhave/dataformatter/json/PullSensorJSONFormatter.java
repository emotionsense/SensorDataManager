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

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.ubhave.sensormanager.config.SensorConfig;
import com.ubhave.sensormanager.config.pull.PullSensorConfig;

public abstract class PullSensorJSONFormatter extends JSONFormatter
{
	private final static String IS_ADAPTIVELY_SENSED = "isAdaptivelySensed";

	private final static String SLEEP_LENGTH = "postSenseSleepMillis";

	public PullSensorJSONFormatter(final Context context, final int sensorType)
	{
		super(context, sensorType);
	}

	@Override
	protected void addGenericConfig(JSONObject json, SensorConfig config) throws JSONException
	{
		if (config != null)
		{
			Long sleepLength = (Long) config.getParameter(SLEEP_LENGTH);
			if (sleepLength != null)
			{
				json.put(SLEEP_LENGTH, sleepLength);
				json.put(IS_ADAPTIVELY_SENSED, (Boolean) config.getParameter(PullSensorConfig.ADAPTIVE_SENSING_ENABLED));
			}
		}
	}

	protected SensorConfig getGenericConfig(final JSONObject json)
	{
		SensorConfig config = new SensorConfig();
		try
		{
			Long sleepLength = (Long) json.get(SLEEP_LENGTH);
			config.setParameter(SLEEP_LENGTH, sleepLength);

			Boolean isAdaptivelySensed = (Boolean) json.get(IS_ADAPTIVELY_SENSED);
			config.setParameter(PullSensorConfig.ADAPTIVE_SENSING_ENABLED, isAdaptivelySensed);
		}
		catch (Exception e)
		{
		}
		return config;
	}
}
