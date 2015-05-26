package com.ubhave.datastore.file;

import java.io.File;

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

	public FileStoreCleaner(final Object fileTransferLock, final FileVault vault)
	{
		super(vault);
		this.config = DataHandlerConfig.getInstance();
		this.fileStatus = new FileVault();
		this.fileReader = new FileStoreReader(fileStatus);
		this.uploadVault = new UploadVault();
	}

	public String moveDataForUpload()
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
					counter += moveDirectoryForUpload(directory);
				}
			}
			if (DataHandlerConfig.shouldLog())
			{
				Log.d(TAG, "Moved " + counter + " directories.");
			}

			// TODO implement
			return null;
//			if (counter == 0)
//			{
//				return null;
//			}
//			else
//			{
//				return uploadDirectoryPath;
//			}
		}
		catch (DataHandlerException e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	public int moveDirectoryForUpload(final File directory) throws DataHandlerException
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
	
	private int moveFileForUpload(final String dataName, final File file) throws DataHandlerException
	{
		if (fileStatus.isDueForUpload(file))
		{
			StringBuilder data = new StringBuilder();
			if (file.length() != 0)
			{
				if (DataHandlerConfig.shouldLog())
				{
					Log.d(TAG, "Read: "+file.getName());
				}
				String fileContent = fileReader.readFile(dataName, file);
				data.append(fileContent);
				if (data.length() != 0)
				{
					uploadVault.writeData(dataName, data.toString());
					return 1;
				}
			}
			file.delete();
		}
		return 0;
	}
}
