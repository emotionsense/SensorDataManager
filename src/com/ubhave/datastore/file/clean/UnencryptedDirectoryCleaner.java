package com.ubhave.datastore.file.clean;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import android.util.Log;

import com.ubhave.datahandler.config.DataHandlerConfig;
import com.ubhave.datahandler.except.DataHandlerException;

public class UnencryptedDirectoryCleaner implements DirectoryCleaner
{
	private final static String TAG = "LogFileDataStorage";
	private final Object fileTransferLock;
	private final DataFileStatus fileStatus;

	public UnencryptedDirectoryCleaner(final Object fileTransferLock)
	{
		this.fileTransferLock = fileTransferLock;
		this.fileStatus = new DataFileStatus();
	}
	
	@Override
	public int moveDirectoryContentsForUpload(final File directory) throws DataHandlerException, IOException
	{
		int dataFiles = 0;
		File[] fileList = directory.listFiles();
		if (fileList != null)
		{
			ArrayList<Thread> threads = new ArrayList<Thread>();
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
						Thread fileThread = new GZipFileThread(file);
						fileThread.start();
						threads.add(fileThread);
					}	
				}
			}
			
			dataFiles = threads.size();
			while (!threads.isEmpty())
			{
				try
				{
					Thread fileThread = threads.remove(0);
					fileThread.join();
				}
				catch (InterruptedException e)
				{}
			}
			
			fileList = directory.listFiles();
			if (fileList != null && fileList.length == 0)
			{
				boolean deleted = directory.delete();
				if (DataHandlerConfig.shouldLog())
				{
					Log.d(TAG, "removeDirectoryIfEmpty() " + directory.getAbsolutePath()+" = "+deleted);
				}
			}
		}
		return dataFiles;
	}
}
