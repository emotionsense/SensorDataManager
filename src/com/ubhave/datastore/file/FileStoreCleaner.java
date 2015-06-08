package com.ubhave.datastore.file;

import java.io.File;
import java.util.List;

import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.ubhave.datahandler.config.DataHandlerConfig;
import com.ubhave.datahandler.config.DataStorageConfig;
import com.ubhave.datahandler.except.DataHandlerException;
import com.ubhave.datahandler.transfer.async.UploadVault;
import com.ubhave.datahandler.transfer.async.UploadVaultInterface;

public class FileStoreCleaner extends FileStoreReader
{
	private final static String TAG = "LogFileDataStorage";
	private final DataHandlerConfig config;
	private final UploadVaultInterface uploadVault;
	private final FileVault fileStatus;
	private final FileStoreReader fileReader;

	public FileStoreCleaner(final Context context, final FileVault vault)
	{
		super(vault);
		this.config = DataHandlerConfig.getInstance();
		this.fileStatus = vault;
		this.fileReader = new FileStoreReader(fileStatus);
		this.uploadVault = new UploadVault(context, vault.getPassword());
	}

	public boolean moveDataForUpload()
	{
		try
		{
			int counter = 0;
			final File rootDirectory = new File((String) config.get(DataStorageConfig.LOCAL_STORAGE_ROOT_NAME));
			final File[] dataDirectories = rootDirectory.listFiles();
			for (File directory : dataDirectories)
			{
				if (directory.isDirectory() && !uploadVault.isUploadDirectory(directory))
				{
					if (DataHandlerConfig.shouldLog())
					{
						Log.d(TAG, "Move for upload: " + directory.getName());
					}
					try
					{
						counter += moveDirectoryForUpload(directory);
					}
					catch (Exception e)
					{
						if (DataHandlerConfig.shouldLog())
						{
							Log.d(TAG, "ERROR moving: " + directory.getName());
							e.printStackTrace();
						}
					}
					
				}
			}
			if (DataHandlerConfig.shouldLog())
			{
				Log.d(TAG, "Moved " + counter + " directories.");
			}
			return counter != 0;
		}
		catch (DataHandlerException e)
		{
			e.printStackTrace();
			return false;
		}
	}
	
	public int moveDirectoryForUpload(final File directory) throws Exception
	{
		int dataFiles = 0;
		if (directory != null && directory.exists())
		{
			String directoryName = directory.getName();
			File[] fileList = directory.listFiles();
			if (fileList != null)
			{
				for (File file : fileList)
				{
					dataFiles += moveFileForUpload(directoryName, file);
				}	
			}
		}
		return dataFiles;
	}
	
	private int moveFileForUpload(final String directoryName, final File file) throws Exception
	{
		int result = 0;
		if (fileStatus.isDueForUpload(file))
		{
			synchronized (FileVault.getLock(directoryName))
			{
				if (file.length() != 0)
				{
					if (DataHandlerConfig.shouldLog())
					{
						Log.d(TAG, "Read: "+file.getName());
					}
					List<JSONObject> entries = fileReader.readFile(directoryName, file);
					if (!entries.isEmpty())
					{
						uploadVault.writeData(directoryName, entries);
						result = 1;
					}
				}
				file.delete();
			}
		}
		return result;
	}
}
