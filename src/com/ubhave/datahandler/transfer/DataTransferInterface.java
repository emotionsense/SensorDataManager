package com.ubhave.datahandler.transfer;

import com.ubhave.datahandler.DataHandlerException;

public interface DataTransferInterface
{
	/*
	 * Transferring stored data
	 */
	public void attemptDataUpload();
	
	/*
	 * Immediately posting data
	 * Note: these are unimplemented
	 */
	public void postData(final String data, final String url) throws DataHandlerException;
	public void postError(final String error, final String url) throws DataHandlerException;
	public void postExtra(final String tag, final String data, final String url) throws DataHandlerException;
}
