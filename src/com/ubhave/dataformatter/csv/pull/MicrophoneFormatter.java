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

import com.ubhave.dataformatter.csv.CSVFormatter;
import com.ubhave.sensormanager.config.SensorConfig;
import com.ubhave.sensormanager.data.SensorData;
import com.ubhave.sensormanager.data.pullsensor.MicrophoneData;

public class MicrophoneFormatter extends CSVFormatter
{	
	
	private final static String SAMPLE_LENGTH = "sampleLengthMillis";
	private final static String SLEEP_LENGTH = "postSenseSleepMillis";
	private final static String AMPLITUDE = "amplitude";
	
	@Override
	protected void addSensorSpecificData(StringBuilder builder, SensorData data)
	{
		MicrophoneData micData = (MicrophoneData) data;
		builder.append(micData.getAmplitudeString());
	}

	@Override
	protected void addSensorSpecificConfig(StringBuilder builder, SensorConfig config)
	{
		builder.append(","+config.getParameter(SensorConfig.SENSE_WINDOW_LENGTH_MILLIS));
		builder.append(","+config.getParameter(SensorConfig.POST_SENSE_SLEEP_LENGTH_MILLIS));
	}

	@Override
	protected void addSensorSpecificHeaders(StringBuilder builder)
	{
		builder.append(","+SAMPLE_LENGTH);
		builder.append(","+SLEEP_LENGTH);
		builder.append(","+AMPLITUDE);
	}
}
