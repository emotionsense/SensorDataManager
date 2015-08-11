package com.ubhave.datastore.file;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.ubhave.datahandler.config.DataHandlerConfig;
import com.ubhave.datahandler.except.DataHandlerException;
import com.ubhave.datahandler.transfer.async.UploadVault;
import com.ubhave.datahandler.transfer.async.UploadVaultInterface;

public class FileStoreCleaner extends FileStoreReader
{
	private final static String TAG = "LogFileDataStorage";
	private final static int MIN_ENTRIES = 25;
	
	private final UploadVaultInterface uploadVault;
	private final FileVault fileStatus;
	private final FileStoreReader fileReader;

	public FileStoreCleaner(final Context context, final FileVault vault)
	{
		super(vault);
		this.fileStatus = vault;
		this.fileReader = new FileStoreReader(fileStatus);
		this.uploadVault = UploadVault.getInstance(context, vault.getPassword());
	}

	public boolean moveDataForUpload()
	{
		try
		{
			int counter = 0;
			final File root = vault.getLocalDirectory();
			final File[] dataDirectories = root.listFiles();
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
		int dataWrites = 0;
		if (directory != null && directory.exists())
		{
			String directoryName = directory.getName();
			File[] fileList = directory.listFiles();
			if (fileList != null)
			{
				List<JSONObject> entries = new ArrayList<JSONObject>();
				for (File file : fileList)
				{
					List<JSONObject> fileEntries = getDataToUpload(directoryName, file);
					if (fileEntries != null)
					{
						entries.addAll(fileEntries);
						if (entries.size() >= MIN_ENTRIES)
						{
							dataWrites ++;
							uploadVault.writeData(directoryName, entries);
							entries.clear();
						}
					}
				}
				if (!entries.isEmpty())
				{
					dataWrites ++;
					uploadVault.writeData(directoryName, entries);
					entries.clear();
				}
				if (directory.listFiles().length == 0)
				{
					directory.delete();
				}
			}
		}
		else if (DataHandlerConfig.shouldLog())
		{
			Log.d(TAG, "Directory is null or doesn't exist.");
		}
		return dataWrites;
	}
	
	private List<JSONObject> getDataToUpload(final String directoryName, final File file) throws Exception
	{
		List<JSONObject> entries = null;
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
					entries = fileReader.readFile(directoryName, file);
				}
				file.delete();
			}
		}
		else if (DataHandlerConfig.shouldLog())
		{
			Log.d(TAG, "Not due for upload: "+file.getName());
		}
		return entries;
	}
}
