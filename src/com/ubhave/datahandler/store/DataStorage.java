package com.ubhave.datahandler.store;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import com.ubhave.dataformatter.DataFormatter;
import com.ubhave.datahandler.DataHandlerConfig;
import com.ubhave.datahandler.DataHandlerException;
import com.ubhave.sensormanager.ESException;
import com.ubhave.sensormanager.data.SensorData;
import com.ubhave.sensormanager.sensors.SensorUtils;

public class DataStorage
{
	private final static String UNKNOWN_SENSOR = "Unknown_Sensor";
	private final static String ERROR_DIRECTORY_NAME = "Error_Log";

	public DataStorage()
	{
	}

	// private File getDirectory(String directory) throws DataHandlerException
	// {
	// File dir = context.getDir(directory, Context.MODE_PRIVATE);
	// DataHandlerConfig config = DataHandlerConfig.getInstance();
	// if (config.containsConfig(DataHandlerConfig.FILE_STORAGE_QUOTA))
	// {
	// long quota = (Long) config.get(DataHandlerConfig.FILE_STORAGE_QUOTA);
	// if (dir.length() > quota)
	// {
	// throw new DataHandlerException(DataHandlerException.STORAGE_OVER_QUOTA);
	// }
	// }
	//
	// if (dir != null)
	// {
	// return dir;
	// }
	// else
	// {
	// throw new
	// DataHandlerException(DataHandlerException.STORAGE_CREATE_ERROR);
	// }
	// }

	private String getFileName(String directoryFullPath) throws DataHandlerException
	{
		File directory = new File(directoryFullPath);
		File[] files = directory.listFiles();
		long latestUpdate = Long.MIN_VALUE;
		File latestFile = null;
		for (File file : files)
		{
			if (file.isFile())
			{
				long update = file.lastModified();
				if (update > latestUpdate)
				{
					latestUpdate = update;
					latestFile = file;
				}
			}
		}

		long fileQuota = (Long) DataHandlerConfig.getInstance().get(DataHandlerConfig.FILE_MAX_SIZE);
		if (latestFile == null || latestFile.length() > fileQuota)
		{
			latestFile = new File(directoryFullPath + "/" + System.currentTimeMillis() + ".log");
		}
		return latestFile.getAbsolutePath();
	}

	private void writeData(String directoryName, String data) throws DataHandlerException
	{
		try
		{
			String directoryFullPath = DataHandlerConfig.PHONE_STORAGE_DIR + "/" + directoryName;
			File file = new File(directoryFullPath);
			if (!file.exists())
			{
				file.mkdirs();
			}

			String fileFullPath = getFileName(directoryFullPath);

			file = new File(fileFullPath);
			if (!file.exists())
			{
				file.createNewFile();
			}
			// append mode
			FileOutputStream fos = new FileOutputStream(file, true);
			fos.write(data.getBytes());
			fos.write("\n".getBytes());
			fos.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
			throw new DataHandlerException(DataHandlerException.IO_EXCEPTION);
		}
	}

	public void logSensorData(final SensorData data, final DataFormatter formatter) throws DataHandlerException
	{
		String sensorName;
		try
		{
			sensorName = SensorUtils.getSensorName(data.getSensorType());
		}
		catch (ESException e)
		{
			sensorName = UNKNOWN_SENSOR;
		}
		String directoryName = sensorName;

		writeData(directoryName, formatter.toString(data));
	}

	public void logError(final String error) throws DataHandlerException
	{
		writeData(ERROR_DIRECTORY_NAME, error);
	}

	public void logExtra(final String tag, final String data) throws DataHandlerException
	{
		writeData(tag, data);
	}

	// public static String zipFiles(String directory, String fileExtension,
	// File[] files)
	// {
	// String fileType = fileExtension.substring(fileExtension.lastIndexOf(".")
	// + 1, fileExtension.length());
	// fileType = fileType.toUpperCase();
	//
	// // Create the ZIP file
	// String outFilename = directory + "/" + getImei() + "_" + fileType + "_" +
	// System.currentTimeMillis() + ".zip";
	// try
	// {
	// ZipOutputStream out = new ZipOutputStream(new
	// FileOutputStream(outFilename));
	//
	// // Compress the files
	// for (File logFile : files)
	// {
	// FileInputStream in = new FileInputStream(logFile);
	//
	// // Add ZIP entry to output stream.
	// out.putNextEntry(new ZipEntry(logFile.getName()));
	//
	// // Transfer bytes from the file to the ZIP file
	// int len;
	// byte[] buf = new byte[1024];
	// while ((len = in.read(buf)) > 0)
	// {
	// out.write(buf, 0, len);
	// }
	//
	// // Complete the entry
	// out.closeEntry();
	// in.close();
	// }
	//
	// // Complete the ZIP file
	// out.close();
	// }
	// catch (Exception exp)
	// {
	// exp.printStackTrace();
	// }
	//
	// return outFilename;
	// }

	// public static final String ROOT_DIR = "MobileSurveyData";
	// public static final String APP_DIR_FULL_PATH =
	// Environment.getExternalStorageDirectory().getAbsolutePath() + "/" +
	// ROOT_DIR;
	// public static final String SOUNDS_DIR = APP_DIR_FULL_PATH + "/sounds";
	// public static final String DATA_LOGS_DIR = APP_DIR_FULL_PATH +
	// "/data_logs";
	// public static final String CONFIG_DIR = APP_DIR_FULL_PATH + "/config";
	// public static final String TO_BE_UPLOADED_LOGS_DIR = APP_DIR_FULL_PATH +
	// "/to_be_uploaded";
}
