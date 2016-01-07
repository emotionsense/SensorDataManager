package com.ubhave.datahandler.transfer.async;

import com.ubhave.datahandler.except.DataHandlerException;

import org.json.JSONObject;

import java.io.File;
import java.util.List;

public interface UploadVaultInterface
{
	void writeData(final String dataName, final List<JSONObject> data) throws Exception;
	
	boolean isUploadDirectory(final File directory) throws DataHandlerException;
	
	File getUploadDirectory() throws DataHandlerException;
}
