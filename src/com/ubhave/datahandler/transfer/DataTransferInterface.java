package com.ubhave.datahandler.transfer;

import com.ubhave.datahandler.except.DataHandlerException;

public interface DataTransferInterface
{
	/*
	 * Transferring stored data
	 */
	public void attemptDataUpload();
	public void uploadData() throws DataHandlerException;
	
	/*
	 * Immediately posting data
	 * Note: these are unimplemented
	 */
	public void postData(final String data) throws DataHandlerException;
	public void postError(final String error) throws DataHandlerException;
	public void postExtra(final String tag, final String data) throws DataHandlerException;
}
