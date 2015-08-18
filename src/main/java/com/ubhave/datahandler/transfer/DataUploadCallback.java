package com.ubhave.datahandler.transfer;

public interface DataUploadCallback
{
	/*
	 * Data has been uploaded
	 */
	void onDataUploaded();
	
	/*
	 * Upload has failed
	 */
	void onDataUploadFailed();
}
