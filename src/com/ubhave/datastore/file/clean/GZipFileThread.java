package com.ubhave.datastore.file.clean;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

import android.util.Log;

import com.ubhave.datahandler.config.DataHandlerConfig;
import com.ubhave.datahandler.config.DataStorageConstants;
import com.ubhave.datahandler.except.DataHandlerException;

public class GZipFileThread extends Thread
{
	private final static String TAG = "GZipFileThread";
	private final File file;
	
	public GZipFileThread(final File file)
	{
		this.file = file;
	}
	
	public void run()
	{
		try
		{
			if (DataHandlerConfig.shouldLog())
			{
				Log.d(TAG, "gzip file " + file);
			}
			
			// TODO add upload directory
			final File uploadDirectory = new File("");
//			final File uploadDirectory = getUploadDirectory();
//			synchronized (fileTransferLock)
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
					file.delete();
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
