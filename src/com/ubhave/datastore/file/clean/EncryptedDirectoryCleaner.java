package com.ubhave.datastore.file.clean;

import java.io.File;
import java.io.IOException;

import android.util.Log;

import com.ubhave.datahandler.config.DataHandlerConfig;
import com.ubhave.datahandler.except.DataHandlerException;
import com.ubhave.datastore.file.FileStoreAbstractReader;
import com.ubhave.datastore.file.FileVault;

public class EncryptedDirectoryCleaner extends FileStoreAbstractReader implements DirectoryCleaner
{
	private final static String TAG = "LogFileDataStorage";
	private final DataFileStatus fileStatus;

	public EncryptedDirectoryCleaner(final FileVault vault)
	{
		super(vault);
		this.fileStatus = new DataFileStatus();
	}
	
	@Override
	public void moveDirectoryContentsForUpload(final File directory) throws DataHandlerException, IOException
	{
		if (directory != null && directory.exists())
		{
			if (DataHandlerConfig.shouldLog())
			{
				Log.d(TAG, "moveDirectoryContentsForUpload() " + directory.getName());
			}
			File[] fileList = directory.listFiles();
			if (fileList != null)
			{
				StringBuilder data = new StringBuilder();
				for (File file : fileList)
				{
					if (fileStatus.isDueForUpload(file))
					{
						if (file.length() <= 0)
						{
							file.delete();
						}
						else
						{
							// TODO read contents
						}	
					}
				}
				// TODO move contents to upload vault
			}
		}
	}
}
