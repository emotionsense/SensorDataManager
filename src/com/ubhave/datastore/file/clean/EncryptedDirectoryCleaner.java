package com.ubhave.datastore.file.clean;

import java.io.File;
import java.io.IOException;

import android.util.Log;

import com.ubhave.datahandler.config.DataHandlerConfig;
import com.ubhave.datahandler.except.DataHandlerException;
import com.ubhave.datahandler.transfer.async.UploadVault;
import com.ubhave.datahandler.transfer.async.UploadVaultInterface;
import com.ubhave.datastore.file.FileStoreAbstractReader;
import com.ubhave.datastore.file.FileVault;

public class EncryptedDirectoryCleaner extends FileStoreAbstractReader implements DirectoryCleaner
{
	private final static String TAG = "LogFileDataStorage";
	private final DataFileStatus fileStatus;
	private final UploadVaultInterface uploadVault;

	public EncryptedDirectoryCleaner(final FileVault vault)
	{
		super(vault);
		this.fileStatus = new DataFileStatus();
		this.uploadVault = new UploadVault();
	}
	
	@Override
	public int moveDirectoryContentsForUpload(final File directory) throws DataHandlerException, IOException
	{
		int dataFiles = 0;
		if (directory != null && directory.exists())
		{
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
							if (DataHandlerConfig.shouldLog())
							{
								Log.d(TAG, "Read: "+file.getName());
							}
							String fileContent = readFile(directory.getName(), file);
							data.append(fileContent);
							dataFiles++;
						}	
					}
				}
				if (data.length() != 0)
				{
					uploadVault.writeData(directory.getName(), data.toString());
				}
			}
		}
		return dataFiles;
	}
}
