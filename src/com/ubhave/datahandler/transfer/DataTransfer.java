package com.ubhave.datahandler.transfer;

import com.ubhave.datahandler.DataHandlerException;

public class DataTransfer
{

	public void postData(final String data, final String url) throws DataHandlerException
	{
		if (url == null)
		{
			throw new DataHandlerException(DataHandlerException.NO_URL_TARGET);
		}
		else
		{
			// TODO
		}
	}
	
	public void postError(final String error, final String url) throws DataHandlerException
	{
		if (url == null)
		{
			throw new DataHandlerException(DataHandlerException.NO_URL_TARGET);
		}
		else
		{
			// TODO
		}
	}

	public void postExtra(final String tag, final String data, final String url) throws DataHandlerException
	{
		if (url == null)
		{
			throw new DataHandlerException(DataHandlerException.NO_URL_TARGET);
		}
		else
		{
			// TODO
		}
	}
}
