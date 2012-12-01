package com.ubhave.dataformatter;

import com.ubhave.dataformatter.csv.CSVFormatter;
import com.ubhave.dataformatter.json.JSONFormatter;
import com.ubhave.sensormanager.data.SensorData;
import com.ubhave.sensormanager.sensors.SensorUtils;

public abstract class DataFormatter
{
	
	public static CSVFormatter getCSVFormatter(int sensorType)
	{
		switch(sensorType)
		{
		case SensorUtils.SENSOR_TYPE_ACCELEROMETER: return new com.ubhave.dataformatter.csv.pull.AccelerometerFormatter();
		case SensorUtils.SENSOR_TYPE_BLUETOOTH: return new com.ubhave.dataformatter.csv.pull.BluetoothFormatter();
		case SensorUtils.SENSOR_TYPE_LOCATION: return new com.ubhave.dataformatter.csv.pull.LocationFormatter();
		case SensorUtils.SENSOR_TYPE_MICROPHONE: return new com.ubhave.dataformatter.csv.pull.MicrophoneFormatter();
		case SensorUtils.SENSOR_TYPE_WIFI: return new com.ubhave.dataformatter.csv.pull.WifiFormatter();
		
		case SensorUtils.SENSOR_TYPE_BATTERY: return new com.ubhave.dataformatter.csv.push.BatteryFormatter();
		case SensorUtils.SENSOR_TYPE_CONNECTION_STATE: return new com.ubhave.dataformatter.csv.push.ConnectionStateFormatter();
		case SensorUtils.SENSOR_TYPE_PHONE_STATE: return new com.ubhave.dataformatter.csv.push.PhoneStateFormatter();
		case SensorUtils.SENSOR_TYPE_PROXIMITY: return new com.ubhave.dataformatter.csv.push.ProximityFormatter();
		case SensorUtils.SENSOR_TYPE_SMS: return new com.ubhave.dataformatter.csv.push.SmsFormatter();
		default: return null;
		}
	}
	
	public static JSONFormatter getJSONFormatter(int sensorType)
	{
		switch(sensorType)
		{
		case SensorUtils.SENSOR_TYPE_ACCELEROMETER: return new com.ubhave.dataformatter.json.pull.AccelerometerFormatter();
		case SensorUtils.SENSOR_TYPE_BLUETOOTH: return new com.ubhave.dataformatter.json.pull.BluetoothFormatter();
		case SensorUtils.SENSOR_TYPE_LOCATION: return new com.ubhave.dataformatter.json.pull.LocationFormatter();
		case SensorUtils.SENSOR_TYPE_MICROPHONE: return new com.ubhave.dataformatter.json.pull.MicrophoneFormatter();
		case SensorUtils.SENSOR_TYPE_WIFI: return new com.ubhave.dataformatter.json.pull.WifiFormatter();
		
		case SensorUtils.SENSOR_TYPE_BATTERY: return new com.ubhave.dataformatter.json.push.BatteryFormatter();
		case SensorUtils.SENSOR_TYPE_CONNECTION_STATE: return new com.ubhave.dataformatter.json.push.ConnectionStateFormatter();
		case SensorUtils.SENSOR_TYPE_PHONE_STATE: return new com.ubhave.dataformatter.json.push.PhoneStateFormatter();
		case SensorUtils.SENSOR_TYPE_PROXIMITY: return new com.ubhave.dataformatter.json.push.ProximityFormatter();
		case SensorUtils.SENSOR_TYPE_SMS: return new com.ubhave.dataformatter.json.push.SmsFormatter();
		default: return null;
		}
	}
	
	public abstract String toString(final SensorData data);
}
