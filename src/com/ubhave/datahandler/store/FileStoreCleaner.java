package com.ubhave.datahandler.store;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

import android.util.Log;

import com.ubhave.datahandler.config.DataHandlerConfig;
import com.ubhave.datahandler.config.DataStorageConfig;
import com.ubhave.datahandler.config.DataStorageConstants;
import com.ubhave.datahandler.except.DataHandlerException;

public class FileStoreCleaner
{
	private static final String TAG = "LogFileDataStorage";
	
	private final Object fileTransferLock;
	private final DataHandlerConfig config;

	public FileStoreCleaner(final Object fileTransferLock)
	{
		this.config = DataHandlerConfig.getInstance();
		this.fileTransferLock = fileTransferLock;
	}
	
	public void moveDirectoryContentsForUpload(String directoryFullPath) throws DataHandlerException, IOException
	{
		if (DataHandlerConfig.shouldLog())
		{
			Log.d(TAG, "moveDirectoryContentsForUpload() " + directoryFullPath);
		}
		
		File directory = new File(directoryFullPath);
		if (directory != null && directory.exists())
		{
			File[] fileList = directory.listFiles();
			if (fileList != null)
			{
				for (File file : fileList)
				{
					if (isMediaFile(file.getName()) || isLogFileDueForUpload(file))
					{
						if (file.length() <= 0)
						{
							file.delete();
						}
						else
						{
							moveFileToUploadDir(file);
						}	
					}
				}
				removeDirectoryIfEmpty(directory);
			}
		}
	}
	
	private void removeDirectoryIfEmpty(final File directory)
	{
		File[] fileList = directory.listFiles();
		if (fileList != null && fileList.length == 0)
		{
			boolean deleted = directory.delete();
			if (DataHandlerConfig.shouldLog())
			{
				Log.d(TAG, "removeDirectoryIfEmpty() " + directory.getAbsolutePath()+" = "+deleted);
			}
		}
	}
	
	private File getUploadDirectory() throws DataHandlerException
	{
		String uploadDir = (String) config.get(DataStorageConfig.LOCAL_STORAGE_UPLOAD_DIRECTORY_PATH);
		File directory = new File(uploadDir);
		if (!directory.exists())
		{
			directory.mkdirs();
		}
		return directory;
	}
	
	private void moveFileToUploadDir(final File file)
	{
		new Thread()
		{
			public void run()
			{
				try
				{
					if (DataHandlerConfig.shouldLog())
					{
						Log.d(TAG, "gzip file " + file);
					}
					File gzippedFile = gzipFile(file);
					synchronized (fileTransferLock)
					{
						File directory = getUploadDirectory();
						gzippedFile.renameTo(new File(directory.getAbsolutePath() + "/" + gzippedFile.getName()));	
					}
					if (DataHandlerConfig.shouldLog())
					{
						Log.d(TAG, "moved file " + gzippedFile.getAbsolutePath() + " to server upload dir");
						Log.d(TAG, "deleting file: " + file.getAbsolutePath());
					}
					file.delete();
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}.start();
	}
	
	private boolean isMediaFile(final String fileName)
	{
		return fileName.contains(DataStorageConstants.ZIP_FILE_SUFFIX) || fileName.contains(DataStorageConstants.AUDIO_FILE_SUFFIX);
		// TODO add photos/camera sensor
	}
	
	private long getDurationLimit()
	{
		try
		{
			return (Long) config.get(DataStorageConfig.FILE_LIFE_MILLIS);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return DataStorageConfig.DEFAULT_FILE_LIFE_MILLIS;
		}
	}
	
	private boolean isLogFileDueForUpload(File file)
	{
		try
		{
			long durationLimit = getDurationLimit();
			if (file != null)
			{
				String fileName = file.getName();
				if (fileName.contains(DataStorageConstants.LOG_FILE_SUFFIX))
				{
					String timeStr = fileName.substring(0, fileName.indexOf(DataStorageConstants.LOG_FILE_SUFFIX));
					long fileTimestamp = Long.parseLong(timeStr);
					long currTime = System.currentTimeMillis();
					if ((currTime - fileTimestamp) > durationLimit)
					{
						return true;
					}
				}
			}
			return false;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return false;
		}
	}

	private File gzipFile(final File inputFile) throws IOException, DataHandlerException
	{
		byte[] buffer = new byte[1024];

		String parentFullPath = inputFile.getParent();
		File parentFile = new File(parentFullPath);
		String parentDirName = parentFile.getName();

		String gzipFileName = parentFullPath + "/" + getDeviceIdentifier() + "_" + parentDirName + "_"
				+ inputFile.getName() + DataStorageConstants.ZIP_FILE_SUFFIX;

		File outputFile = new File(gzipFileName);
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

	private String getDeviceIdentifier() throws DataHandlerException
	{
		try
		{
			return (String) config.get(DataStorageConfig.UNIQUE_DEVICE_ID);
		}
		catch (DataHandlerException e)
		{
			e.printStackTrace();
			throw new DataHandlerException(DataHandlerException.UNKNOWN_CONFIG);
		}
	}
}
