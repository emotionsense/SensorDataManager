/* **************************************************
 Copyright (c) 2015, University of Cambridge
 Neal Lathia, neal.lathia@cl.cam.ac.uk
 Kiran Rachuri, kiran.rachuri@cl.cam.ac.uk

This application was developed as part of the EPSRC Ubhave (Ubiquitous and
Social Computing for Positive Behaviour Change) Project. For more
information, please visit http://www.emotionsense.org

Permission to use, copy, modify, and/or distribute this software for any
purpose with or without fee is hereby granted, provided that the above
copyright notice and this permission notice appear in all copies.

THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR
IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 ************************************************** */

package com.ubhave.datahandler.transfer;

import android.util.Log;

import com.ubhave.datahandler.config.DataHandlerConfig;
import com.ubhave.datahandler.config.DataStorageConstants;
import com.ubhave.datahandler.except.DataHandlerException;

import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;

import java.io.File;

public class FilesPostThread extends AbstractPostThread
{
	private final DataUploadCallback[] callbacks;
	private final File directory;

	public FilesPostThread(final File directory, final DataUploadCallback[] callbacks) throws DataHandlerException
	{
		super();
		this.callbacks = callbacks;
		this.directory = directory;
	}

	@Override
	public void run()
	{
		try
		{
            if (directory != null)
            {
                if (DataHandlerConfig.shouldLog())
                {
                    Log.d(DataTransfer.TAG, "Attempting upload from: " + directory.getName());
                }

                File[] files = directory.listFiles();
                if (files != null)
                {
                    if (DataHandlerConfig.shouldLog())
                    {
                        Log.d(DataTransfer.TAG, "Attempting upload " + files.length + " files.");
                    }

                    for (final File file : files)
                    {
                        if (file.isFile() && file.getName().contains(DataStorageConstants.ZIP_FILE_SUFFIX))
                        {
                            if (DataHandlerConfig.shouldLog())
                            {
                                Log.d(DataTransfer.TAG, "Param: "+ postKey +" (" + file.getName()+")");
                            }
                            MultipartEntity multipartEntity = new MultipartEntity();
                            multipartEntity.addPart(postKey, new FileBody(file));
                            post(multipartEntity);
                            file.delete();
                        }
                        else if (DataHandlerConfig.shouldLog())
                        {
                            Log.d(DataTransfer.TAG, "Skip: " + file.getName());
                        }
                    }
                    if (directory.listFiles().length == 0)
                    {
                        directory.delete();
                    }

                    Log.d(DataTransfer.TAG, "Data upload succeeded.");
                }
                else if (DataHandlerConfig.shouldLog())
                {
                    Log.d(DataTransfer.TAG, "Attempting file list is null.");
                }
            }
            else if (DataHandlerConfig.shouldLog())
            {
                Log.d(DataTransfer.TAG, "Upload directory is null.");
            }
            notify(true);
		}
		catch (Exception e)
		{
			if (DataHandlerConfig.shouldLog())
			{
				Log.d(DataTransfer.TAG, "Data post failed.");
			}
            e.printStackTrace();
            notify(false);
		}
	}

    private void notify(final boolean success)
    {
        for (DataUploadCallback callback : callbacks)
        {
            if (success)
            {
                callback.onDataUploaded();
            }
            else
            {
                callback.onDataUploadFailed();
            }
        }
    }
}
