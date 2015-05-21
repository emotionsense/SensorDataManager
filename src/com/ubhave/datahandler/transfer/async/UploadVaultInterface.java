package com.ubhave.datahandler.transfer.async;

import java.util.List;

import org.json.JSONObject;

import com.ubhave.datahandler.except.DataHandlerException;

public interface UploadVaultInterface
{
	public void writeData(final String dataName, final List<JSONObject> data) throws DataHandlerException;
}
