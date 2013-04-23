package com.ubhave.datahandler.sync;

import java.util.HashMap;

import com.ubhave.datahandler.DataHandlerException;

public interface FileSyncInterface
{
	public int addRemoteToSyncList(final String url, final HashMap<String, String> queryParameters, final String filePath) throws DataHandlerException;
	public void removeFromSyncList(int id) throws DataHandlerException;
	public void syncUpdatedFiles();
}
