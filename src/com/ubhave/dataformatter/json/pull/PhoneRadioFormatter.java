/* **************************************************
 Copyright (c) 2014, Idiap
 Olivier Bornet, olivier.bornet@idiap.ch

This file was developed to add phone radio sensor to the SensorManager library
from https://github.com/nlathia/SensorManager.

The SensorManager library was developed as part of the EPSRC Ubhave (Ubiquitous
and Social Computing for Positive Behaviour Change) Project. For more
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
import com.ubhave.sensormanager.config.SensorConfig;
import com.ubhave.sensormanager.data.SensorData;
import com.ubhave.sensormanager.data.pull.PhoneRadioData;
import com.ubhave.sensormanager.data.pull.PhoneRadioDataList;
import com.ubhave.sensormanager.process.AbstractProcessor;
import com.ubhave.sensormanager.process.pull.PhoneRadioProcessor;
import com.ubhave.sensormanager.sensors.SensorUtils;

public class PhoneRadioFormatter extends PullSensorJSONFormatter
{
	private final static String PHONE_RADIO_RESULT = "phoneRadioResult";
	private final static String MCC = "mcc";
	private final static String MNC = "mnc";
	private final static String LAC = "lac";
	private final static String CID = "cid";

	private final static String UNAVAILABLE = "unavailable";

	public PhoneRadioFormatter(final Context context)
	{
		super(context, SensorUtils.SENSOR_TYPE_PHONE_RADIO);
	}

	@Override
	protected void addSensorSpecificData(JSONObject json, SensorData data) throws JSONException
	{
		PhoneRadioDataList phoneRadioDataList = (PhoneRadioDataList) data;
		ArrayList<PhoneRadioData> results = phoneRadioDataList.getPhoneRadios();
		JSONArray resultJSON = new JSONArray();
		if (results != null)
		{
			for (PhoneRadioData result : results)
			{
				JSONObject cellInfo = new JSONObject();
				cellInfo.put(MCC, result.getMcc());
				cellInfo.put(MNC, result.getMnc());
				cellInfo.put(LAC, result.getLac());
				cellInfo.put(CID, result.getCid());
				resultJSON.put(cellInfo);
			}
		}
		else
		{
			resultJSON.put(UNAVAILABLE);
		}
		json.put(PHONE_RADIO_RESULT, resultJSON);
	}

	@Override
	protected void addSensorSpecificConfig(JSONObject json, SensorConfig config) throws JSONException
	{
	}

	@Override
	public SensorData toSensorData(String jsonString)
	{
		JSONObject jsonData = super.parseData(jsonString);
		if (jsonData != null)
		{
			long senseStartTimestamp = super.parseTimeStamp(jsonData);
			SensorConfig sensorConfig = super.getGenericConfig(jsonData);

			boolean setRawData = true;
			boolean setProcessedData = false;

			try
			{
				ArrayList<PhoneRadioData> phoneRadioData = new ArrayList<PhoneRadioData>();
				JSONArray jsonArray = (JSONArray) jsonData.get(PHONE_RADIO_RESULT);
				for (int i = 0; i < jsonArray.length(); i++)
				{
					JSONObject entry = (JSONObject) jsonArray.get(i);
					String mcc = (String) entry.get(MCC);
					String mnc = (String) entry.get(MNC);
					int lac = ((Long) entry.get(LAC)).intValue();
					int cid = ((Long) entry.get(CID)).intValue();
					phoneRadioData.add(new PhoneRadioData(mcc, mnc, lac, cid));
				}

				PhoneRadioProcessor processor = (PhoneRadioProcessor) AbstractProcessor.getProcessor(applicationContext, sensorType, setRawData, setProcessedData);
				return processor.process(senseStartTimestamp, phoneRadioData, sensorConfig);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		return null;
	}
}
