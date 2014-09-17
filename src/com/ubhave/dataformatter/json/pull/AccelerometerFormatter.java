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

import android.content.Context;

import com.ubhave.sensormanager.ESException;
import com.ubhave.sensormanager.process.AbstractProcessor;
import com.ubhave.sensormanager.process.pull.AbstractMotionProcessor;
import com.ubhave.sensormanager.process.pull.AccelerometerProcessor;
import com.ubhave.sensormanager.sensors.SensorUtils;

public class AccelerometerFormatter extends AbstractMotionFormatter
{
	public AccelerometerFormatter(final Context context)
	{
		super(context, SensorUtils.SENSOR_TYPE_ACCELEROMETER);
	}

	@Override
	protected AbstractMotionProcessor getProcessor(boolean setRawData, boolean setProcessedData) throws ESException
	{
		return (AccelerometerProcessor) AbstractProcessor.getProcessor(applicationContext, SensorUtils.SENSOR_TYPE_ACCELEROMETER, setRawData, setProcessedData);
	}
}
