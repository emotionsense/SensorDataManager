package com.ubhave.datahandler.transfer.async;

import java.io.File;
import java.util.List;

import org.json.JSONObject;

public interface UploadVaultInterface
{
	public void writeData(final String dataName, final List<JSONObject> data) throws Exception;
	
	public void writeData(final String dataName, final String data);
	
	public File getUploadDirectory();
}
