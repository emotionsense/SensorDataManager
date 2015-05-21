package com.ubhave.datastore.file.clean;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

import android.util.Log;

import com.ubhave.datahandler.config.DataHandlerConfig;
import com.ubhave.datahandler.config.DataStorageConfig;
import com.ubhave.datahandler.config.DataStorageConstants;
import com.ubhave.datahandler.except.DataHandlerException;
import com.ubhave.datastore.file.FileStoreAbstractReader;
import com.ubhave.datastore.file.FileVault;

public class UnencryptedCleaner
{
	private final static String TAG = "LogFileDataStorage";
	
	private final Object fileTransferLock;
	private final DataHandlerConfig config;

	public UnencryptedCleaner(final Object fileTransferLock)
	{
		this.config = DataHandlerConfig.getInstance();
		this.fileTransferLock = fileTransferLock;
	}
	
	public void moveDirectoryContentsForUpload(final String directoryPath) throws DataHandlerException, IOException
	{
		if (DataHandlerConfig.shouldLog())
		{
			Log.d(TAG, "moveDirectoryContentsForUpload() " + directoryPath);
		}
		
		File directory = new File(directoryPath);
		if (directory != null && directory.exists())
		{
			File[] fileList = directory.listFiles();
			if (fileList != null)
			{
				for (File file : fileList)
				{
					if (isLogFileDueForUpload(file))
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
			}
		}
	}
	
	private void moveFileToUploadDir(final File file)
	{
		new Thread()
		{
			private void removeDirectoryIfEmpty(final File directory)
			{
				if (directory != null)
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
			}
			
			public void run()
			{
				try
				{
					if (DataHandlerConfig.shouldLog())
					{
						Log.d(TAG, "gzip file " + file);
					}
					final File uploadDirectory = getUploadDirectory();
					synchronized (fileTransferLock)
					{
						try
						{
							gzipFile(file, uploadDirectory);
							if (DataHandlerConfig.shouldLog())
							{
								String abs = file.getAbsolutePath();
								Log.d(TAG, "moved file " + abs + " to server upload dir");
								Log.d(TAG, "deleting file: " + abs);
							}
							File parentDirectory = file.getParentFile();
							file.delete();
							removeDirectoryIfEmpty(parentDirectory);
						}
						catch (FileNotFoundException e)
						{}
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}.start();
	}
	
	private long getDurationLimit()
	{
		try
		{
			return (Long) config.get(DataStorageConfig.DATA_LIFE_MILLIS);
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

	private void gzipFile(final File inputFile, final File uploadDirectory) throws IOException, DataHandlerException
	{
		DataHandlerConfig config = DataHandlerConfig.getInstance();
		FileInputStream in = new FileInputStream(inputFile);
		byte[] buffer = new byte[1024];
		File sourceDirectory = new File(inputFile.getParent());
		String gzipFileName = config.getIdentifier() + "_"
						+ sourceDirectory.getName() + "_"
						+ inputFile.getName()
						+ DataStorageConstants.ZIP_FILE_SUFFIX;
		
		int len;
		File outputFile = new File(uploadDirectory, gzipFileName);
		GZIPOutputStream gzipOS = new GZIPOutputStream(new FileOutputStream(outputFile));
		while ((len = in.read(buffer)) > 0)
		{
			gzipOS.write(buffer, 0, len);
		}
		in.close();
		gzipOS.finish();
		gzipOS.close();
	}
}
