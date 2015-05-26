package com.ubhave.datastore.file;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.util.Log;

import com.ubhave.datahandler.config.DataHandlerConfig;
import com.ubhave.datahandler.config.DataStorageConfig;
import com.ubhave.datahandler.config.DataStorageConstants;
import com.ubhave.datahandler.except.DataHandlerException;

public class FileStoreWriter
{
	private static final String TAG = "LogFileDataStorage";

	private final Context context;
	private final DataHandlerConfig config;
	private final FileVault fileVault;
	private final FileStoreCleaner cleaner;
	
	public FileStoreWriter(final Context context, final FileVault vault, final FileStoreCleaner cleaner)
	{
		this.config = DataHandlerConfig.getInstance();
		this.context = context;
		this.fileVault = vault;
		this.cleaner = cleaner;
	}
	
	private boolean canWriteToExternalStorage()
	{
		String writePermission = Manifest.permission.WRITE_EXTERNAL_STORAGE;
		String writeableState = Environment.getExternalStorageState();
		return context.checkCallingOrSelfPermission(writePermission) == PackageManager.PERMISSION_GRANTED
				&& Environment.MEDIA_MOUNTED.equals(writeableState);
	}

	private File getLocalDirectory() throws DataHandlerException
	{
		if (canWriteToExternalStorage())
		{
			String rootName = (String) config.get(DataStorageConfig.LOCAL_STORAGE_ROOT_NAME);
			return getDirectory(Environment.getExternalStorageDirectory(), rootName);
		}
		else
		{
			throw new DataHandlerException(DataHandlerException.IO_EXCEPTION);
		}
	}
	
	public void writeData(final String directoryName, String data) throws DataHandlerException
	{
		File root = getLocalDirectory();
		synchronized (FileVault.getLock(directoryName))
		{
			final File directory = getDirectory(root, directoryName);
			try
			{
				File dataFile;
				if (!fileVault.isEncrypted())
				{
					dataFile = getLastestFile(directory);
					data += "\n";
				}
				else
				{
					dataFile = createNewFile(directory);
				}
				
				OutputStream cos = fileVault.openForWriting(dataFile);
				cos.write(data.getBytes());
				cos.flush();
				cos.close();
			}
			catch (Exception e)
			{
				e.printStackTrace();
				throw new DataHandlerException(DataHandlerException.IO_EXCEPTION);
			}
		}
	}
	
	private File getDirectory(final File root, final String directoryName)
	{
		final File directory = new File(root, directoryName);
		if (!directory.exists())
		{
			boolean directoryCreated = directory.mkdirs();
			if (DataHandlerConfig.shouldLog())
			{
				Log.d(TAG, "Created ["+directoryCreated+"]: " + directory.getAbsolutePath());
			}
		}
		return directory;
	}

	private File getLastestFile(final File directory) throws DataHandlerException, IOException
	{
		File latestFile = null;
		final File[] files = directory.listFiles();
		if (files != null)
		{
			long latestUpdate = Long.MIN_VALUE;
			for (File file : files)
			{
				if (file.isFile() && file.getName().endsWith(DataStorageConstants.JSON_FILE_SUFFIX))
				{
					long update = file.lastModified();
					if (update > latestUpdate)
					{
						latestUpdate = update;
						latestFile = file;
					}
				}
			}
		}
		if (latestFile == null)
		{
			return createNewFile(directory);
		}
		else if (fileVault.isDueForUpload(latestFile))
		{
			try
			{
				cleaner.moveDirectoryForUpload(directory);
			}
			catch (Exception e)
			{}
			return createNewFile(directory);
		}
		else
		{
			return latestFile;
		}
	}
	
	private File createNewFile(final File directory) throws IOException
	{
		File file = new File(directory, System.currentTimeMillis() + DataStorageConstants.JSON_FILE_SUFFIX);
		while (file.exists())
		{
			file = new File(directory, System.currentTimeMillis() + DataStorageConstants.JSON_FILE_SUFFIX);
		}
		
		boolean fileCreated = file.createNewFile();
		if (DataHandlerConfig.shouldLog())
		{
			Log.d(TAG, "Created ["+fileCreated+"]: " + file.getAbsolutePath());
		}
		return file;
	}
}
