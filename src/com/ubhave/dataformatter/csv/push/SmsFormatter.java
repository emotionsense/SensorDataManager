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
import com.ubhave.sensormanager.data.pushsensor.SmsData;

public class SmsFormatter extends PushSensorCSVFormatter
{
	private final static String CONTENT_LENGTH = "contentLength";
	private final static String WORD_COUNT = "wordCount";
	private final static String EVENT_TYPE = "eventType";
	private final static String ADDRESS = "address";
	
	@Override
	protected void addSensorSpecificData(StringBuilder builder, SensorData data)
	{
		SmsData smsData = (SmsData) data;
		builder.append(","+smsData.getContentLength());
		builder.append(","+smsData.getNoOfWords());
		builder.append(","+smsData.getEventType());
		builder.append(","+smsData.getAddress());
	}

	@Override
	protected void addSensorSpecificHeaders(StringBuilder builder)
	{
		builder.append(","+CONTENT_LENGTH);
		builder.append(","+WORD_COUNT);
		builder.append(","+EVENT_TYPE);
		builder.append(","+ADDRESS);
	}
}
