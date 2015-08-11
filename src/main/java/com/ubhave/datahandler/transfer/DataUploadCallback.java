package com.ubhave.datahandler.transfer;

public interface DataUploadCallback
{
	/*
	 * Data has been uploaded
	 */
	public void onDataUploaded();
	
	/*
	 * Upload has failed
	 */
	public void onDataUploadFailed();
}
