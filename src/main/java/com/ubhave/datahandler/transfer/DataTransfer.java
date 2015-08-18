package com.ubhave.datahandler.transfer;

import android.content.Context;

import com.ubhave.datahandler.except.DataHandlerException;
import com.ubhave.datahandler.transfer.async.UploadVault;
import com.ubhave.datahandler.transfer.async.UploadVaultInterface;

import java.io.File;

public class DataTransfer implements DataTransferInterface
{
	public final static String TAG = "DataTransfer";
	private final UploadVaultInterface uploadVault;

	public DataTransfer(final Context context, final String dataPassword)
	{
		this.uploadVault = UploadVault.getInstance(context, dataPassword);
	}

	@Override
	public void uploadData(final DataUploadCallback[] callbacks) throws DataHandlerException
	{
		File directory = uploadVault.getUploadDirectory();
		new FilesPostThread(directory, callbacks).start();
	}
}
