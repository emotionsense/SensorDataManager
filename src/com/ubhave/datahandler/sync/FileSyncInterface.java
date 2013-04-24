package com.ubhave.datahandler.sync;

import com.ubhave.datahandler.DataHandlerException;

public interface FileSyncInterface
{
	public int subscribeToRemoteFileUpdate(final String url, FileUpdatedListener listener) throws DataHandlerException;
	public int subscribeToRemoteFileUpdate(final SyncRequest request, FileUpdatedListener listener) throws DataHandlerException;
	public void unsubscribeFromRemoteFileUpdate(final int key) throws DataHandlerException;
	public void syncUpdatedFiles();
}
