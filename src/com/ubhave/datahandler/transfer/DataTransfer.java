package com.ubhave.datahandler.transfer;

import java.io.File;

import android.content.Context;
import android.util.Log;

import com.ubhave.datahandler.config.DataHandlerConfig;
import com.ubhave.datahandler.except.DataHandlerException;
import com.ubhave.datahandler.transfer.async.UploadVault;
import com.ubhave.datahandler.transfer.async.UploadVaultInterface;

public class DataTransfer implements DataTransferInterface
{
	private final static String TAG = "DataTransfer";
	private final UploadVaultInterface uploadVault;

	public DataTransfer(final Context context, final String dataPassword)
	{
		this.uploadVault = UploadVault.getInstance(context, dataPassword);
	}

	@Override
	public void uploadData(final DataUploadCallback[] callback) throws DataHandlerException
	{
		File directory = uploadVault.getUploadDirectory();
		if (directory == null)
		{
			if (DataHandlerConfig.shouldLog())
			{
				Log.d(TAG, "Upload directory is null.");
			}
			return;
		}
		new FilesPostThread(directory, callback).start();
	}
}
