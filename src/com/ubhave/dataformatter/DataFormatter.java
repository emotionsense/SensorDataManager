package com.ubhave.dataformatter;

import android.content.Context;

import com.ubhave.dataformatter.json.JSONFormatter;
import com.ubhave.dataformatter.json.env.AmbientTemperatureFormatter;
import com.ubhave.dataformatter.json.env.HumidityFormatter;
import com.ubhave.dataformatter.json.env.LightFormatter;
import com.ubhave.dataformatter.json.env.PressureFormatter;
import com.ubhave.dataformatter.json.log.InteractionFormatter;
import com.ubhave.dataformatter.json.pull.AccelerometerFormatter;
import com.ubhave.dataformatter.json.pull.BluetoothFormatter;
import com.ubhave.dataformatter.json.pull.CallContentReaderFormatter;
import com.ubhave.dataformatter.json.pull.GyroscopeFormatter;
import com.ubhave.dataformatter.json.pull.LocationFormatter;
import com.ubhave.dataformatter.json.pull.MagneticFieldFormatter;
import com.ubhave.dataformatter.json.pull.MicrophoneFormatter;
import com.ubhave.dataformatter.json.pull.PhoneRadioFormatter;
import com.ubhave.dataformatter.json.pull.SmsContentReaderFormatter;
import com.ubhave.dataformatter.json.pull.StepCounterFormatter;
import com.ubhave.dataformatter.json.pull.WifiFormatter;
import com.ubhave.dataformatter.json.push.BatteryFormatter;
import com.ubhave.dataformatter.json.push.ConnectionStateFormatter;
import com.ubhave.dataformatter.json.push.ConnectionStrengthFormatter;
import com.ubhave.dataformatter.json.push.PassiveLocationFormatter;
import com.ubhave.dataformatter.json.push.PhoneStateFormatter;
import com.ubhave.dataformatter.json.push.ProximityFormatter;
import com.ubhave.dataformatter.json.push.ScreenFormatter;
import com.ubhave.dataformatter.json.push.SmsFormatter;
import com.ubhave.datahandler.except.DataHandlerException;
import com.ubhave.sensormanager.data.SensorData;
import com.ubhave.sensormanager.sensors.SensorUtils;

public abstract class DataFormatter
{

	public static JSONFormatter getJSONFormatter(final Context c, final int sensorType)
	{
		switch (sensorType)
		{
		case SensorUtils.SENSOR_TYPE_ACCELEROMETER:
			return new AccelerometerFormatter(c);
		case SensorUtils.SENSOR_TYPE_BLUETOOTH:
			return new BluetoothFormatter(c);
		case SensorUtils.SENSOR_TYPE_LOCATION:
			return new LocationFormatter(c);
		case SensorUtils.SENSOR_TYPE_MICROPHONE:
			return new MicrophoneFormatter(c);
		case SensorUtils.SENSOR_TYPE_WIFI:
			return new WifiFormatter(c);
		case SensorUtils.SENSOR_TYPE_SMS_CONTENT_READER:
			return new CallContentReaderFormatter(c);
		case SensorUtils.SENSOR_TYPE_CALL_CONTENT_READER:
			return new SmsContentReaderFormatter(c);
		case SensorUtils.SENSOR_TYPE_BATTERY:
			return new BatteryFormatter(c);
		case SensorUtils.SENSOR_TYPE_SCREEN:
			return new ScreenFormatter(c);
		case SensorUtils.SENSOR_TYPE_CONNECTION_STATE:
			return new ConnectionStateFormatter(c);
		case SensorUtils.SENSOR_TYPE_PROXIMITY:
			return new ProximityFormatter(c);
		case SensorUtils.SENSOR_TYPE_SMS:
			return new SmsFormatter(c);
		case SensorUtils.SENSOR_TYPE_GYROSCOPE:
			return new GyroscopeFormatter(c);
		case SensorUtils.SENSOR_TYPE_LIGHT:
			return new LightFormatter(c);
		case SensorUtils.SENSOR_TYPE_PHONE_STATE:
			return new PhoneStateFormatter(c);
		case SensorUtils.SENSOR_TYPE_CONNECTION_STRENGTH:
			return new ConnectionStrengthFormatter(c);
		case SensorUtils.SENSOR_TYPE_PASSIVE_LOCATION:
			return new PassiveLocationFormatter(c);
		case SensorUtils.SENSOR_TYPE_AMBIENT_TEMPERATURE:
			return new AmbientTemperatureFormatter(c);
		case SensorUtils.SENSOR_TYPE_HUMIDITY:
			return new HumidityFormatter(c);
		case SensorUtils.SENSOR_TYPE_PRESSURE:
			return new PressureFormatter(c);
		case SensorUtils.SENSOR_TYPE_MAGNETIC_FIELD:
			return new MagneticFieldFormatter(c);
		case SensorUtils.SENSOR_TYPE_PHONE_RADIO:
            return new PhoneRadioFormatter(c);
		case SensorUtils.SENSOR_TYPE_STEP_COUNTER:
			return new StepCounterFormatter(c);
		case SensorUtils.SENSOR_TYPE_INTERACTION:
			return new InteractionFormatter(c);
		default:
			return null;
		}
	}

	public abstract String toString(final SensorData data) throws DataHandlerException;
	
	public abstract SensorData toSensorData(final String dataString);
}
