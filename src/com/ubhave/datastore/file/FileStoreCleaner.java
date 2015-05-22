package com.ubhave.datastore.file;

import java.io.File;

import android.util.Log;

import com.ubhave.datahandler.config.DataHandlerConfig;
import com.ubhave.datahandler.config.DataStorageConfig;
import com.ubhave.datahandler.except.DataHandlerException;
import com.ubhave.datastore.file.clean.DirectoryCleaner;
import com.ubhave.datastore.file.clean.EncryptedDirectoryCleaner;
import com.ubhave.datastore.file.clean.UnencryptedDirectoryCleaner;

public class FileStoreCleaner extends FileStoreAbstractReader
{
	private final static String TAG = "LogFileDataStorage";
	private final DataHandlerConfig config;
	private final DirectoryCleaner directoryCleaner;

	public FileStoreCleaner(final Object fileTransferLock, final FileVault vault)
	{
		super(vault);
		this.config = DataHandlerConfig.getInstance();
		if (vault.isEncrypted())
		{
			this.directoryCleaner = new EncryptedDirectoryCleaner(vault);
		}
		else
		{
			this.directoryCleaner = new UnencryptedDirectoryCleaner(fileTransferLock);
		}
	}

	public String moveDataForUpload()
	{
		try
		{
			int counter = 0;
			final String uploadDirectoryPath = config.getLocalUploadDirectoryPath();
			final File rootDirectory = new File((String) config.get(DataStorageConfig.LOCAL_STORAGE_ROOT_NAME));
			final File[] dataDirectories = rootDirectory.listFiles();
			for (File directory : dataDirectories)
			{
				if (directory.isDirectory())
				{
					String directoryName = directory.getName();
					if (directoryName != null && !uploadDirectoryPath.contains(directoryName))
					{
						if (moveDirectory(directory))
						{
							counter++;
						}
					}
				}
			}
			if (DataHandlerConfig.shouldLog())
			{
				Log.d(TAG, "Moved " + counter + " directories.");
			}

			if (counter == 0)
			{
				return null;
			}
			else
			{
				return uploadDirectoryPath;
			}
		}
		catch (DataHandlerException e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	public boolean moveDirectory(final File directory)
	{
		synchronized (FileVault.getLock(directory.getName()))
		{
			try
			{
				if (DataHandlerConfig.shouldLog())
				{
					Log.d(TAG, "moveDirectoryContentsForUpload(" + directory.getName() + ").");
				}
				directoryCleaner.moveDirectoryContentsForUpload(directory);
				return true;
			}
			catch (Exception e)
			{
				if (DataHandlerConfig.shouldLog())
				{
					Log.d(TAG, "Failed to move data in directory: "+directory.getName());
				}
				e.printStackTrace();
				return false;
			}
		}
	}
}
