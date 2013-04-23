package com.ubhave.datahandler.sync;

import java.util.HashMap;

import android.content.Context;

import com.ubhave.datahandler.DataHandlerException;

public class FileSynchronizer implements FileSyncInterface
{
	private final Context context;
	
	public FileSynchronizer(Context context)
	{
		this.context = context;
	}
	
	@Override
	public int addRemoteToSyncList(final String url, final HashMap<String, String> queryParameters, final String filePath) throws DataHandlerException
	{
		throw new DataHandlerException(DataHandlerException.UNIMPLEMENTED);
	}
	
	@Override
	public void removeFromSyncList(int id) throws DataHandlerException
	{
		throw new DataHandlerException(DataHandlerException.UNIMPLEMENTED);
	}
	
	@Override
	public void syncUpdatedFiles()
	{
		
	}
}
