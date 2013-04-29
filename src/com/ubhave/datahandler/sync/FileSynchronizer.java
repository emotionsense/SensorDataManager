package com.ubhave.datahandler.sync;

import java.util.Random;

import android.content.Context;
import android.util.SparseArray;

import com.ubhave.datahandler.except.DataHandlerException;
import com.ubhave.datahandler.except.FileSyncException;

public class FileSynchronizer implements FileSyncInterface
{
	private final Context context;
	private final Random random;
	
	private final SparseArray<SyncRequest> fileSyncRequests;
	
	public FileSynchronizer(Context context)
	{
		this.context = context;
		random = new Random(System.currentTimeMillis());
		fileSyncRequests = new SparseArray<SyncRequest>();
	}
	
	private int getRandomKey() throws DataHandlerException
	{
		int key = random.nextInt();
		int conflicts = 0;
		while (fileSyncRequests.get(key) != null)
		{
			key = random.nextInt();
			conflicts++;
			if (conflicts == 1000)
			{
				throw new FileSyncException(FileSyncException.KEY_ALLOCATION_CONFLICT);
			}
		}
		return key;
	}
	
	@Override
	public int subscribeToRemoteFileUpdate(final String url, final String fileTarget, FileUpdatedListener listener) throws DataHandlerException
	{
		if (listener == null)
		{
			throw new FileSyncException(FileSyncException.NO_LISTENER);
		}
		
		SyncRequest request = new SyncRequest(context, url, fileTarget);
		request.setListener(listener);
		return subscribeToRemoteFileUpdate(request, listener);
	}
	
	@Override
	public int subscribeToRemoteFileUpdate(final SyncRequest request, FileUpdatedListener listener) throws DataHandlerException
	{
		if (listener == null)
		{
			throw new FileSyncException(FileSyncException.NO_LISTENER);
		}
		
		request.setListener(listener);
		for (int i=0; i<fileSyncRequests.size(); i++)
		{
			SyncRequest r = fileSyncRequests.valueAt(i);
			if (r.equals(request))
			{
				throw new FileSyncException(FileSyncException.REQUEST_ALREADY_EXISTS);
			}
		}
		
		int key = getRandomKey();
		fileSyncRequests.put(key, request);
		request.start();
		
		return key;
	}
	
	@Override
	public void unsubscribeFromRemoteFileUpdate(final int key) throws DataHandlerException
	{
		SyncRequest request = fileSyncRequests.get(key);
		if (request != null)
		{
			fileSyncRequests.remove(key);
			request.stop();
		}
		else
		{
			throw new FileSyncException(FileSyncException.KEY_NOT_FOUND);
		}
	}
	
	@Override
	public void syncUpdatedFiles()
	{
		for (int i=0; i<fileSyncRequests.size(); i++)
		{
			fileSyncRequests.valueAt(i).alarmTriggered();
		}	
	}
}
