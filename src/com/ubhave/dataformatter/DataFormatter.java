package com.ubhave.dataformatter;

import android.content.Context;

import com.ubhave.dataformatter.json.JSONFormatter;
import com.ubhave.sensormanager.data.SensorData;
import com.ubhave.sensormanager.sensors.SensorUtils;

public abstract class DataFormatter
{
	
	public static JSONFormatter getJSONFormatter(final Context c, final int sensorType)
	{
		switch(sensorType)
		{
<<<<<<< HEAD
		case SensorUtils.SENSOR_TYPE_ACCELEROMETER: return new com.ubhave.dataformatter.json.pull.AccelerometerFormatter();
		case SensorUtils.SENSOR_TYPE_BLUETOOTH: return new com.ubhave.dataformatter.json.pull.BluetoothFormatter();
		case SensorUtils.SENSOR_TYPE_LOCATION: return new com.ubhave.dataformatter.json.pull.LocationFormatter();
		case SensorUtils.SENSOR_TYPE_MICROPHONE: return new com.ubhave.dataformatter.json.pull.MicrophoneFormatter();
		case SensorUtils.SENSOR_TYPE_WIFI: return new com.ubhave.dataformatter.json.pull.WifiFormatter();
		case SensorUtils.SENSOR_TYPE_APPLICATION: return new com.ubhave.dataformatter.json.pull.ApplicationFormatter();
		case SensorUtils.SENSOR_TYPE_SMS_CONTENT_READER: return new com.ubhave.dataformatter.json.pull.ContentReaderFormatter();
		case SensorUtils.SENSOR_TYPE_CALL_CONTENT_READER: return new com.ubhave.dataformatter.json.pull.ContentReaderFormatter();
=======
		case SensorUtils.SENSOR_TYPE_ACCELEROMETER: return new com.ubhave.dataformatter.json.pull.AccelerometerFormatter(c);
		case SensorUtils.SENSOR_TYPE_BLUETOOTH: return new com.ubhave.dataformatter.json.pull.BluetoothFormatter(c);
		case SensorUtils.SENSOR_TYPE_LOCATION: return new com.ubhave.dataformatter.json.pull.LocationFormatter(c);
		case SensorUtils.SENSOR_TYPE_MICROPHONE: return new com.ubhave.dataformatter.json.pull.MicrophoneFormatter(c);
		case SensorUtils.SENSOR_TYPE_WIFI: return new com.ubhave.dataformatter.json.pull.WifiFormatter(c);
		case SensorUtils.SENSOR_TYPE_APPLICATION: return new com.ubhave.dataformatter.json.pull.ApplicationFormatter(c);
>>>>>>> dev
		
		case SensorUtils.SENSOR_TYPE_BATTERY: return new com.ubhave.dataformatter.json.push.BatteryFormatter(c);
		case SensorUtils.SENSOR_TYPE_SCREEN: return new com.ubhave.dataformatter.json.push.ScreenFormatter(c);
		case SensorUtils.SENSOR_TYPE_CONNECTION_STATE: return new com.ubhave.dataformatter.json.push.ConnectionStateFormatter(c);
		case SensorUtils.SENSOR_TYPE_PHONE_STATE: return new com.ubhave.dataformatter.json.push.PhoneStateFormatter(c);
		case SensorUtils.SENSOR_TYPE_PROXIMITY: return new com.ubhave.dataformatter.json.push.ProximityFormatter(c);
		case SensorUtils.SENSOR_TYPE_SMS: return new com.ubhave.dataformatter.json.push.SmsFormatter(c);
		default: return null;
		}
	}
	
	public abstract String toString(final SensorData data);
}
