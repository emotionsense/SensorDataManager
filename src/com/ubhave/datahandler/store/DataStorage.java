package com.ubhave.datahandler.store;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

import android.util.Log;

import com.ubhave.dataformatter.DataFormatter;
import com.ubhave.datahandler.DataHandlerConfig;
import com.ubhave.datahandler.DataHandlerException;
import com.ubhave.sensormanager.ESException;
import com.ubhave.sensormanager.data.SensorData;
import com.ubhave.sensormanager.sensors.SensorUtils;

public class DataStorage
{
	private static final String TAG = "DataStorage";

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

	private String getFileName(String directoryFullPath) throws DataHandlerException, IOException
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
			if (latestFile != null && latestFile.length() > fileQuota)
			{
				moveFilesForUploadingToServer(directoryFullPath);
			}
			latestFile = new File(directoryFullPath + "/" + System.currentTimeMillis() + ".log");

		}
		return latestFile.getAbsolutePath();
	}

	private void moveFilesForUploadingToServer(String directoryFullPath) throws DataHandlerException, IOException
	{
		Log.d(TAG, "moveFilesForUploadingToServer() " + directoryFullPath);

		File directory = new File(directoryFullPath);
		File[] files = directory.listFiles();
		for (File file : files)
		{
			long fileQuota = (Long) DataHandlerConfig.getInstance().get(DataHandlerConfig.FILE_MAX_SIZE);
			if (file.length() > fileQuota)
			{
				Log.d(TAG, "gzip file " + file);
				File gzippedFile = gzipFile(file);

				String uploadDirFullPath = DataHandlerConfig.SERVER_UPLOAD_DIR + "/";
				File uploadDir = new File(uploadDirFullPath);
				if (!uploadDir.exists())
				{
					uploadDir.mkdirs();
				}

				String newFileFullPath = uploadDirFullPath + directory.getName() + "_" + gzippedFile.getName();
				Log.d(TAG, "moving file " + gzippedFile.getAbsolutePath() + " to " + newFileFullPath);
				gzippedFile.renameTo(new File(newFileFullPath));

				Log.d(TAG, "deleting file: " + file.getAbsolutePath());
				file.delete();
			}
		}
	}

	private File gzipFile(File inputFile) throws IOException
	{

		byte[] buffer = new byte[1024];

		File outputFile = new File(inputFile.getAbsolutePath() + ".gz");
		GZIPOutputStream gzipOS = new GZIPOutputStream(new FileOutputStream(outputFile));
		FileInputStream in = new FileInputStream(inputFile);

		int len;
		while ((len = in.read(buffer)) > 0)
		{
			gzipOS.write(buffer, 0, len);
		}

		in.close();

		gzipOS.finish();
		gzipOS.close();

		return outputFile;
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
