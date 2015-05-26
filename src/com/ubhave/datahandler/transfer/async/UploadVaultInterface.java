package com.ubhave.datahandler.transfer.async;

import java.io.File;
import java.util.List;

import org.json.JSONObject;

import com.ubhave.datahandler.except.DataHandlerException;

public interface UploadVaultInterface
{
	public void writeData(final String dataName, final List<JSONObject> data) throws Exception;
	
	public boolean isUploadDirectory(final File directory) throws DataHandlerException;
	
	public File getUploadDirectory() throws DataHandlerException;
}
