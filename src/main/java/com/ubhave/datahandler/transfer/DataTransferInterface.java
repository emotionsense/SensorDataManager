package com.ubhave.datahandler.transfer;

import com.ubhave.datahandler.except.DataHandlerException;

public interface DataTransferInterface
{
	/*
	 * Transferring stored data
	 */
	public void uploadData(final DataUploadCallback[] callback) throws DataHandlerException;
	
}
